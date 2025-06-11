/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jdbc.internal.resolvers;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jdbc.composer.ConnectionHandler;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * Primary key resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public class PrimaryKeyResolver implements SQLContextExpressionResolver<DataTarget, SQLPrimaryKey> {

	private static final long serialVersionUID = -1693018694034935286L;

	/**
	 * Max primary keys cache size
	 */
	private final static int MAX_PRIMARY_KEY_CACHE_SIZE = 5000;

	/**
	 * Primary keys cache
	 */
	@SuppressWarnings("serial")
	private final LinkedHashMap<String, SQLPrimaryKey> primaryKeysCache = new LinkedHashMap<String, SQLPrimaryKey>(16,
			0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, SQLPrimaryKey> eldest) {
			return size() > MAX_PRIMARY_KEY_CACHE_SIZE;
		}

	};

	private final ConnectionHandler connectionProvider;

	public PrimaryKeyResolver(ConnectionHandler connectionProvider) {
		super();
		this.connectionProvider = connectionProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends DataTarget> getExpressionType() {
		return DataTarget.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLPrimaryKey> getResolvedType() {
		return SQLPrimaryKey.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLPrimaryKey> resolve(DataTarget expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// try to retrieve the primary key from database
		final DataTarget<?> target = context.resolve(expression, DataTarget.class).orElse(expression);

		return getPrimaryKeyFromDatabaseMetadata(context, target.getName());
	}

	/**
	 * Get the table primary key from JDBC database metedata.
	 * @param context SQL context
	 * @param tableName The name of the table for which to obtain the primary key
	 * @return The table primary key as a {@link SQLPrimaryKey}, if available
	 * @throws InvalidExpressionException If an error occurred
	 */
	private Optional<SQLPrimaryKey> getPrimaryKeyFromDatabaseMetadata(SQLContext context, String tableName)
			throws InvalidExpressionException {
		if (tableName == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(primaryKeysCache.computeIfAbsent(tableName, table -> {
			final String databaseTable = context.getDialect().getTableName(tableName);

			return connectionProvider.withConnection(connection -> {

				List<OrderedPath> paths = new ArrayList<>();

				DatabaseMetaData databaseMetaData = connection.getMetaData();

				try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, databaseTable)) {
					while (resultSet.next()) {
						final String columnName = resultSet.getString("COLUMN_NAME");
						OrderedPath op = new OrderedPath();
						op.path = Path.of(columnName,
								getColumnType(context, databaseMetaData, databaseTable, columnName));
						op.sequence = resultSet.getShort("KEY_SEQ");
						paths.add(op);
					}
				}

				if (!paths.isEmpty()) {
					Collections.sort(paths);
					return SQLPrimaryKey.create(paths.stream().map(p -> p.path).collect(Collectors.toList())
							.toArray(new Path[paths.size()]));
				}

				return null;

			});
		}));

	}

	/**
	 * Get the Java type which corresponds to the JDBC type of the column with given <code>columnName</code> of the
	 * provided <code>tableName</code>.
	 * @param context SQL context
	 * @param databaseMetaData Database metadata
	 * @param tableName Table name
	 * @param columnName Column name
	 * @return The Java type which corresponds to the JDBC type of given column
	 * @throws SQLException If an error occurred or the Java type of given column cannot be resolved
	 */
	private static Class<?> getColumnType(SQLContext context, DatabaseMetaData databaseMetaData, String tableName,
			String columnName) throws SQLException {
		try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, columnName)) {
			if (rs.next()) {
				return context.getTypeConverter().getJavaType(context, SQLType.create(rs.getInt("DATA_TYPE")))
						.orElse(Object.class);
			}
		}
		throw new SQLException(
				"Failed to obtain the Java type for the column [" + columnName + "] of the table [" + tableName + "]");
	}

	/**
	 * Support class to order {@link Path}s using a sequence.
	 */
	private static class OrderedPath implements Comparable<OrderedPath> {

		Path path;
		short sequence;

		@Override
		public int compareTo(OrderedPath o) {
			return ((Short) sequence).compareTo(o.sequence);
		}

	}

}
