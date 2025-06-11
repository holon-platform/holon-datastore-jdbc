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
package com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate;

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQuery;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link QueryOperation} to {@link SQLQuery} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryResolver implements SQLContextExpressionResolver<QueryOperation, SQLQuery> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryOperation> getExpressionType() {
		return QueryOperation.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLQuery> getResolvedType() {
		return SQLQuery.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLQuery> resolve(QueryOperation expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// resolve as SQLQueryClauses
		final SQLQueryDefinition clauses = context.resolveOrFail(expression, SQLQueryDefinition.class);

		// serialize query clauses to SQL
		String sql = context.resolveOrFail(clauses, SQLExpression.class).getValue();

		// prepare SQL
		SQLStatement preparedSQL = context.prepareStatement(sql);
		preparedSQL.validate();

		// prepared SQL
		sql = preparedSQL.getSql();

		// limit and offset
		if (expression.getConfiguration().getLimit().isPresent()) {
			sql = context.getDialect().getLimitHandler()
					.orElseThrow(() -> new InvalidExpressionException("The dialect ["
							+ context.getDialect().getClass().getName() + "] does not supports query limit/offset"))
					.limitResults(clauses, sql, expression.getConfiguration().getLimit().orElse(0),
							expression.getConfiguration().getOffset().orElse(0));
		}

		// build SQLQuery
		return Optional.of(SQLQuery.create(sql,
				clauses.getResultConverter()
						.orElseThrow(() -> new InvalidExpressionException("Missing query results converter")),
				preparedSQL.getParameters()));
	}

}
