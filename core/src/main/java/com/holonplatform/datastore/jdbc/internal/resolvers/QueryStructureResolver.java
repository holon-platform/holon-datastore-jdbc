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
package com.holonplatform.datastore.jdbc.internal.resolvers;

import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.QueryStructure;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultJdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause;
import com.holonplatform.datastore.jdbc.internal.expressions.ProjectionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * {@link QueryStructure} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
public enum QueryStructureResolver implements ExpressionResolver<QueryStructure, JdbcQueryComposition> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryStructure> getExpressionType() {
		return QueryStructure.class;
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
	public Optional<JdbcQueryComposition> resolve(QueryStructure expression, ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JdbcResolutionContext context = JdbcResolutionContext.checkContext(resolutionContext);
		final ResolutionQueryClause previous = context.getResolutionQueryClause().orElse(null);

		// build query composition

		final DefaultJdbcQueryComposition query = new DefaultJdbcQueryComposition(context.getDialect());

		final QueryConfiguration configuration = expression.getConfiguration();

		// limit and offset
		configuration.getLimit().ifPresent(l -> query.setLimit(l));
		configuration.getOffset().ifPresent(o -> query.setOffset(o));

		// from
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.FROM);

			// relational target
			RelationalTarget<?> target = JdbcDatastoreUtils.resolveExpression(context,
					configuration.getTarget().orElseThrow(() -> new QueryBuildException("Missing query target")),
					RelationalTarget.class, context);

			context.setTarget(target);

			query.setFrom(JdbcDatastoreUtils.resolveExpression(context, target, SQLToken.class, context).getValue());
		} finally {
			context.setResolutionQueryClause(previous);
		}

		// where
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.WHERE);
			configuration.getFilter().ifPresent(f -> {
				query.setWhere(JdbcDatastoreUtils.resolveExpression(context, f, SQLToken.class, context).getValue());
			});
		} finally {
			context.setResolutionQueryClause(previous);
		}

		// group by
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.GROUPBY);
			configuration.getAggregation().ifPresent(a -> {
				query.setGroupBy(JdbcDatastoreUtils.resolveExpression(context, a, SQLToken.class, context).getValue());
			});
		} finally {
			context.setResolutionQueryClause(previous);
		}

		// order by
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.ORDERBY);
			configuration.getSort().ifPresent(s -> {
				query.setOrderBy(JdbcDatastoreUtils.resolveExpression(context, s, SQLToken.class, context).getValue());
			});
		} finally {
			context.setResolutionQueryClause(previous);
		}

		// select
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.SELECT);

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

		} finally {
			context.setResolutionQueryClause(previous);
		}

		return Optional.of(query);
	}
}
