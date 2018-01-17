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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.expressions.SQLFunction;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * {@link QueryFunction} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryFunctionResolver implements ExpressionResolver<QueryFunction, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryFunction> getExpressionType() {
		return QueryFunction.class;
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
	public Optional<SQLToken> resolve(QueryFunction expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		// resolve to a SQLFunction
		SQLFunction function = jdbcContext.resolveExpression(expression, SQLFunction.class);
		// validate function
		function.validate();

		// resolve arguments
		List<String> arguments = new LinkedList<>();
		if (expression.getExpressionArguments() != null) {
			for (QueryExpression<?> argument : ((QueryFunction<?, ?>) expression).getExpressionArguments()) {
				// resolve argument
				arguments.add(jdbcContext.resolveExpression(argument, SQLToken.class).getValue());
			}
		}

		// serialize function
		String value = function.serialize(arguments);

		return Optional.ofNullable(SQLToken.create(value));
	}

}
