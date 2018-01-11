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

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.context.PreparedSql;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;

/**
 * JDBC datastore {@link BulkDelete} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkDelete extends AbstractBulkOperation<BulkDelete, JdbcStatementExecutionContext>
		implements BulkDelete {

	/**
	 * Constructor.
	 * @param executionContext Execution context
	 * @param target Operation data target
	 * @param traceEnabled Whether tracing is enabled
	 */
	public JdbcBulkDelete(JdbcStatementExecutionContext executionContext, DataTarget<?> target, boolean traceEnabled) {
		super(executionContext, target, traceEnabled);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryFilter.QueryFilterSupport#filter(com.holonplatform.core.query.QueryFilter)
	 */
	@Override
	public BulkDelete filter(QueryFilter filter) {
		addFilter(filter);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getExecutionContext().getDialect(),
				getExecutionContext().getDialect().deleteStatementAliasSupported() ? AliasMode.AUTO
						: AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.DELETE, getTarget());
			getFilter().ifPresent(f -> builder.withFilter(f));

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure delete operation", e);
		}

		// prepare SQL
		final PreparedSql preparedSql = getExecutionContext().prepareSql(sql, context);
		trace(preparedSql.getSql());

		// execute
		return getExecutionContext().withConnection(c -> {

			try (PreparedStatement stmt = getExecutionContext().createStatement(c, preparedSql)) {
				int count = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.DELETE).affectedCount(count).build();
			}

		});
	}

}
