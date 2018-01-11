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
package com.holonplatform.datastore.jdbc.internal;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.context.PreparedSql;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;

/**
 * JDBC datastore {@link BulkUpdate} implementation.
 * 
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
public class JdbcBulkUpdate extends AbstractBulkOperation<BulkUpdate, JdbcStatementExecutionContext>
		implements BulkUpdate {

	/**
	 * Path values to update
	 */
	private final Map<Path<?>, QueryExpression<?>> values = new HashMap<>();

	/**
	 * Constructor.
	 * @param executionContext Execution context
	 * @param target Operation data target
	 * @param traceEnabled Whether tracing is enabled
	 */
	public JdbcBulkUpdate(JdbcStatementExecutionContext executionContext, DataTarget<?> target, boolean traceEnabled) {
		super(executionContext, target, traceEnabled);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.BulkClause#set(com.holonplatform.core.Path,
	 * com.holonplatform.core.query.QueryExpression)
	 */
	@Override
	public <T> BulkUpdate set(Path<T> path, QueryExpression<? super T> expression) {
		ObjectUtils.argumentNotNull(path, "Path must be not null");
		ObjectUtils.argumentNotNull(expression, "Expression must be not null");
		values.put(path, expression);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.BulkClause#setNull(com.holonplatform.core.Path)
	 */
	@Override
	public BulkUpdate setNull(Path path) {
		ObjectUtils.argumentNotNull(path, "Path must be not null");
		values.put(path, ConstantExpression.nullValue());
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryFilter.QueryFilterSupport#filter(com.holonplatform.core.query.QueryFilter)
	 */
	@Override
	public BulkUpdate filter(QueryFilter filter) {
		addFilter(filter);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {
		if (values.isEmpty()) {
			throw new DataAccessException("No values to update");
		}

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getExecutionContext().getDialect(),
				AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.UPDATE, getTarget());
			values.forEach((p, v) -> builder.withValue(p, v));
			getFilter().ifPresent(f -> builder.withFilter(f));

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure update operation", e);
		}

		// prepare SQL
		final PreparedSql preparedSql = getExecutionContext().prepareSql(sql, context);
		trace(preparedSql.getSql());

		// execute
		return getExecutionContext().withConnection(c -> {

			try (PreparedStatement stmt = getExecutionContext().createStatement(c, preparedSql)) {
				int count = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.UPDATE).affectedCount(count).build();
			}
			
		});
	}

}
