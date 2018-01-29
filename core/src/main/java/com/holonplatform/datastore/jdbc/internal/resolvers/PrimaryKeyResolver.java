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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.composer.ConnectionProvider;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;
import com.holonplatform.datastore.jdbc.internal.pk.PrimaryKeysCache;

/**
 * {@link TablePrimaryKey} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public class PrimaryKeyResolver implements ExpressionResolver<DataTarget, TablePrimaryKey> {

	private static final long serialVersionUID = -1693018694034935286L;

	private final ConnectionProvider connectionProvider;
	private final PrimaryKeysCache primaryKeysCache;

	public PrimaryKeyResolver(ConnectionProvider connectionProvider, PrimaryKeysCache primaryKeysCache) {
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
	public Class<? extends TablePrimaryKey> getResolvedType() {
		return TablePrimaryKey.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<TablePrimaryKey> resolve(DataTarget expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		String tableName = null;
		Optional<TablePrimaryKey> targetPrimaryKey = Optional.empty();

		// resolve target
		final DataTarget<?> target = context.resolve(expression, DataTarget.class, context).orElse(expression);
		target.validate();
		tableName = target.getName();

		if (targetPrimaryKey.isPresent()) {
			return targetPrimaryKey;
		}

		return retrieve(JdbcResolutionContext.checkContext(context), tableName);
	}

	private Optional<TablePrimaryKey> retrieve(JdbcResolutionContext context, String tableName)
			throws InvalidExpressionException {
		Optional<TablePrimaryKey> cached = primaryKeysCache.get(tableName);
		if (cached.isPresent()) {
			return cached;
		}

		try {
			Optional<Path<?>[]> pk = getPrimaryKeyFromDatabaseMetaData(context.getDialect(), tableName);
			if (pk.isPresent()) {
				TablePrimaryKey tablePrimaryKey = TablePrimaryKey.create(pk.get());
				primaryKeysCache.put(tableName, tablePrimaryKey);
				return Optional.of(tablePrimaryKey);
			}
		} catch (Exception e) {
			throw new InvalidExpressionException("Failed to retrieve primary key for table [" + tableName + "]", e);
		}

		return Optional.empty();
	}

	private Optional<Path<?>[]> getPrimaryKeyFromDatabaseMetaData(JdbcDialect dialect, String table) {
		ObjectUtils.argumentNotNull(table, "Table name must be not null");

		final String tableName = dialect.getTableName(table);

		return connectionProvider.withConnection(connection -> {

			List<OrderedPath> paths = new ArrayList<>();

			DatabaseMetaData databaseMetaData = connection.getMetaData();

			try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
				while (resultSet.next()) {
					final String columnName = resultSet.getString("COLUMN_NAME");
					OrderedPath op = new OrderedPath();
					op.path = Path.of(columnName,
							JdbcDatastoreUtils.getColumnType(databaseMetaData, tableName, columnName));
					op.sequence = resultSet.getShort("KEY_SEQ");
					paths.add(op);
				}
			}

			if (!paths.isEmpty()) {
				Collections.sort(paths);
				return Optional.of(
						paths.stream().map(p -> p.path).collect(Collectors.toList()).toArray(new Path[paths.size()]));
			}

			return Optional.empty();

		});
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
