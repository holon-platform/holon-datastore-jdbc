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

import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultJdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.ProjectionContext;

/**
 * {@link QueryOperation} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
public enum QueryStructureResolver implements ExpressionResolver<QueryOperation, JdbcQueryComposition> {

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
	public Class<? extends JdbcQueryComposition> getResolvedType() {
		return JdbcQueryComposition.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JdbcQueryComposition> resolve(QueryOperation expression, ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JdbcResolutionContext context = JdbcResolutionContext.checkContext(resolutionContext);

		// build query composition

		final DefaultJdbcQueryComposition query = new DefaultJdbcQueryComposition(context.getDialect());

		final QueryConfiguration configuration = expression.getConfiguration();

		// limit and offset
		configuration.getLimit().ifPresent(l -> query.setLimit(l));
		configuration.getOffset().ifPresent(o -> query.setOffset(o));

		// from

		RelationalTarget<?> target = context.resolveExpression(
				configuration.getTarget().orElseThrow(() -> new InvalidExpressionException("Missing query target")),
				RelationalTarget.class);

		context.setTarget(target);

		query.setFrom(context.resolveExpression(target, SQLToken.class).getValue());

		// where
		configuration.getFilter().ifPresent(f -> {
			query.setWhere(context.resolveExpression(f, SQLToken.class).getValue());
		});

		// group by
		configuration.getAggregation().ifPresent(a -> {
			query.setGroupBy(context.resolveExpression(a, SQLToken.class).getValue());
		});

		// order by
		configuration.getSort().ifPresent(s -> {
			query.setOrderBy(context.resolveExpression(s, SQLToken.class).getValue());
		});

		// select
		final ProjectionContext<?> projectionContext = context
				.resolve(expression.getProjection(), ProjectionContext.class, context)
				.orElseThrow(() -> new InvalidExpressionException(
						"Failed to resolve projection [" + expression.getProjection() + "]"));
		projectionContext.validate();

		// check selection
		if (projectionContext.getSelection() == null || projectionContext.getSelection().isEmpty()) {
			throw new InvalidExpressionException("Null or empty query selection");
		}

		// select clause
		query.setSelect(projectionContext.getSelection().stream()
				.map(s -> s + projectionContext.getSelectionAlias(s).map(a -> " AS " + a).orElse(""))
				.collect(Collectors.joining(", ")));

		// projection context
		query.setProjection(projectionContext);

		return Optional.of(query);
	}
}
