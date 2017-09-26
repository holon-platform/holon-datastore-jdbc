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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.query.QueryAggregation;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * {@link QueryAggregation} expression resolver.
 *
 * @since 5.0.0
 */
public enum QueryAggregationResolver implements ExpressionResolver<QueryAggregation, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryAggregation> getExpressionType() {
		return QueryAggregation.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLToken> getResolvedType() {
		return SQLToken.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(QueryAggregation expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext context) throws InvalidExpressionException {

		// validate
		expression.validate();

		final StringBuilder sb = new StringBuilder();

		// group by
		List<String> groupBys = new ArrayList<>(expression.getAggregationPaths().length);
		for (Path<?> path : expression.getAggregationPaths()) {
			groupBys.add(JdbcDatastoreUtils.resolveExpression(context, path, SQLToken.class, context).getValue());
		}
		sb.append(groupBys.stream().collect(Collectors.joining(",")));
		// having
		expression.getAggregationFilter().ifPresent(f -> {
			sb.append(" HAVING ");
			sb.append(JdbcDatastoreUtils.resolveExpression(context, f, SQLToken.class, context).getValue());
		});

		return Optional.of(SQLToken.create(sb.toString()));
	}
}
