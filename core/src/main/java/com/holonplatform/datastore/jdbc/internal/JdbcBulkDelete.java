/*
 * Copyright 2000-2016 Holon TDCN.
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
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.support.PreparedSql;

/**
 * JDBC datastore {@link BulkDelete} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkDelete extends AbstractBulkOperation<BulkDelete> implements BulkDelete {

	/**
	 * Constructor
	 * @param datastore Parent Datastore (not null)
	 * @param target Data target (not null)
	 * @param dialect JDBC dialect (not null)
	 * @param traceEnabled Whether tracing is enabled
	 */
	public JdbcBulkDelete(JdbcDatastore datastore, DataTarget<?> target, JdbcDialect dialect, boolean traceEnabled) {
		super(datastore, target, dialect, traceEnabled);
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

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(),
				getDialect().deleteStatementAliasSupported() ? AliasMode.AUTO : AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.DELETE, getTarget());
			getFilter().ifPresent(f -> builder.withFilter(f));

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure delete operation", e);
		}

		// execute
		return getDatastore().withConnection(c -> {

			final PreparedSql preparedSql = JdbcDatastoreUtils.prepareSql(sql, context);
			trace(preparedSql.getSql());

			try (PreparedStatement stmt = preparedSql.createStatement(c, getDialect())) {
				int count = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.DELETE).affectedCount(count).build();
			}
		});
	}

}
