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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jdbc.composer.ConnectionProvider;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * Primary key resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public class PrimaryKeyResolver implements ExpressionResolver<DataTarget, SQLPrimaryKey> {

	private static final long serialVersionUID = -1693018694034935286L;

	private final ConnectionProvider connectionProvider;
	private final Map<String, SQLPrimaryKey> primaryKeysCache;

	public PrimaryKeyResolver(ConnectionProvider connectionProvider, Map<String, SQLPrimaryKey> primaryKeysCache) {
		super();
		this.connectionProvider = connectionProvider;
		this.primaryKeysCache = primaryKeysCache;
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
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLPrimaryKey> resolve(DataTarget expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// try to retrieve the primary key from database
		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		final DataTarget<?> target = jdbcContext.resolve(expression, DataTarget.class, jdbcContext).orElse(expression);

		return getPrimaryKeyFromDatabaseMetadata(jdbcContext, target.getName());
	}

	private Optional<SQLPrimaryKey> getPrimaryKeyFromDatabaseMetadata(JdbcResolutionContext context, String tableName)
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
								JdbcDatastoreUtils.getColumnType(databaseMetaData, databaseTable, columnName));
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
