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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryExecution;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.DefaultSQLQueryClauses;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQuery;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link QueryExecution} to {@link SQLQuery} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryExecutionResolver implements SQLContextExpressionResolver<QueryExecution, SQLQuery> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryExecution> getExpressionType() {
		return QueryExecution.class;
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
	@SuppressWarnings("unchecked")
	@Override
	public Optional<SQLQuery> resolve(QueryExecution expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final QueryConfiguration configuration = expression.getConfiguration();

		// build query clauses
		final DefaultSQLQueryClauses clauses = new DefaultSQLQueryClauses();

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

		// select
		final SQLProjection<?> projection = queryContext.resolveOrFail(expression.getProjection(), SQLProjection.class);
		// add clause
		clauses.setSelect(projection.getSelection().stream()
				.map(s -> s + projection.getSelectionAlias(s).map(a -> " AS " + a).orElse(""))
				.collect(Collectors.joining(", ")));

		// serialize query clauses to SQL
		String sql = queryContext.resolveOrFail(clauses, SQLExpression.class).getValue();

		// prepare SQL
		SQLStatement preparedSQL = context.prepareStatement(sql);
		preparedSQL.validate();

		// limit and offset
		if (configuration.getLimit().isPresent()) {
			sql = queryContext.getDialect().getLimitHandler()
					.orElseThrow(() -> new InvalidExpressionException(
							"The dialect [" + queryContext.getDialect().getClass().getName()
									+ "] does not supports query limit/offset"))
					.limitResults(clauses, sql, configuration.getLimit().orElse(0),
							configuration.getOffset().orElse(0));
		}

		// build SQLQuery
		return Optional.of(SQLQuery.create(preparedSQL.getSql(), ((SQLProjection) projection).getType(),
				((SQLProjection) projection).getConverter(), preparedSQL.getParameters()));
	}

}
