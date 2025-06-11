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
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.lock.LockQueryAdapterQuery;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link QueryOperation} to {@link SQLQueryDefinition} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryOperationClausesResolver implements SQLContextExpressionResolver<QueryOperation, SQLQueryDefinition> {

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
	public Class<? extends SQLQueryDefinition> getResolvedType() {
		return SQLQueryDefinition.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLQueryDefinition> resolve(QueryOperation expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final QueryConfiguration configuration = expression.getConfiguration();

		// build query clauses
		final DefaultSQLQueryDefinition clauses = new DefaultSQLQueryDefinition();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(
				configuration.getTarget().orElseThrow(() -> new InvalidExpressionException("Missing query target")),
				RelationalTarget.class);

		// build a statement context
		final SQLStatementCompositionContext queryContext = SQLStatementCompositionContext.asChild(context, target,
				AliasMode.AUTO);

		// from
		clauses.setFrom(queryContext.resolveOrFail(target, SQLExpression.class).getValue());

		// where
		configuration.getFilter().ifPresent(f -> {
			// add clause
			clauses.setWhere(queryContext.resolveOrFail(f, SQLExpression.class).getValue());
		});

		// group by
		configuration.getAggregation().ifPresent(a -> {
			// add clause
			clauses.setGroupBy(queryContext.resolveOrFail(a, SQLExpression.class).getValue());
		});

		// order by
		configuration.getSort().ifPresent(s -> {
			// add clause
			clauses.setOrderBy(queryContext.resolveOrFail(s, SQLExpression.class).getValue());
		});

		// distinct
		clauses.setDistinct(configuration.isDistinct());

		// lock
		configuration.getParameter(LockQueryAdapterQuery.LOCK_MODE).ifPresent(lockMode -> {
			clauses.setLockMode(lockMode);
			clauses.setLockTimeout(configuration.getParameter(LockQueryAdapterQuery.LOCK_TIMEOUT).orElse(null));
		});

		final SQLProjection<?> projection = queryContext.resolveOrFail(expression.getProjection(), SQLProjection.class);
		// add clause
		clauses.setSelect(projection.getSelection().stream()
				.map(s -> s + projection.getSelectionAlias(s).map(a -> " AS " + a).orElse(""))
				.collect(Collectors.joining(", ")));
		// set converter
		projection.getConverter().ifPresent(rc -> {
			clauses.setResultConverter(rc);
		});

		return Optional.of(clauses);
	}

}
