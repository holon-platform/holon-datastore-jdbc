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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.query.FunctionExpression.PathFunctionExpression;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.QueryFunction.Avg;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.core.query.QueryFunction.Max;
import com.holonplatform.core.query.QueryFunction.Min;
import com.holonplatform.core.query.QueryFunction.Sum;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction.DefaultFunction;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultSQLFunctions;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * JDBC {@link PathFunctionExpression} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PathFunctionResolver implements ExpressionResolver<PathFunctionExpression, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PathFunctionExpression> getExpressionType() {
		return PathFunctionExpression.class;
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
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(PathFunctionExpression expression, ResolutionContext context)
			throws InvalidExpressionException {

		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		// validate
		expression.validate();

		// resolve path
		String path = JdbcDatastoreUtils.resolveExpression(context, expression.getPath(), SQLToken.class, context)
				.getValue();

		// resolve function
		return getDefaultFunction(expression.getFunction())
				.map(f -> SQLToken.create(DefaultSQLFunctions.getSQLFunction(f.getName(), jdbcContext.getDialect())
						.orElseThrow(() -> new QueryBuildException("Missing JDBC function: " + f)).serialize(path)));
	}

	/**
	 * Get the {@link DefaultFunction} which corresponds to given {@link QueryFunction}, if available.
	 * @param function Query function
	 * @return Optional DefaultFunction
	 */
	private static Optional<DefaultFunction> getDefaultFunction(QueryFunction function) {
		if (Count.class.isAssignableFrom(function.getClass())) {
			return Optional.of(DefaultFunction.COUNT);
		}
		if (Avg.class.isAssignableFrom(function.getClass())) {
			return Optional.of(DefaultFunction.AVG);
		}
		if (Min.class.isAssignableFrom(function.getClass())) {
			return Optional.of(DefaultFunction.MIN);
		}
		if (Max.class.isAssignableFrom(function.getClass())) {
			return Optional.of(DefaultFunction.MAX);
		}
		if (Sum.class.isAssignableFrom(function.getClass())) {
			return Optional.of(DefaultFunction.SUM);
		}
		return Optional.empty();
	}

}
