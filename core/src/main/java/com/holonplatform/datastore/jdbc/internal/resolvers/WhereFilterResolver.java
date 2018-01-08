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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jdbc.JdbcWhereFilter;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * {@link JdbcWhereFilter} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 100)
public enum WhereFilterResolver implements ExpressionResolver<JdbcWhereFilter, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends JdbcWhereFilter> getExpressionType() {
		return JdbcWhereFilter.class;
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
	public Optional<SQLToken> resolve(JdbcWhereFilter expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext context) throws InvalidExpressionException {

		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		// validate
		expression.validate();

		String sql = expression.getSQL();
		// parameters
		if (!expression.getParameters().isEmpty()) {

			for (int i = 0; i < expression.getParameters().size(); i++) {
				final Object parameter = expression.getParameters().get(i);
				int idx = sql.indexOf('?');
				if (idx < 0) {
					throw new InvalidExpressionException("Cannot replace parameter [" + parameter + "] in sql ["
							+ expression.getSQL() + "]: no placeholder found at index [" + (i + 1) + "]");
				}
				String named = jdbcContext.addNamedParameter(ParameterValue.create(parameter.getClass(), parameter));
				sql = sql.replaceFirst("\\?", named);
			}
		}

		// get SQL
		return Optional.of(SQLToken.create(sql));
	}

}
