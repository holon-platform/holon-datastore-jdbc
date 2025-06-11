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
package com.holonplatform.datastore.jdbc.composer.internal.resolvers;

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.WhereFilter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * {@link WhereFilter} resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum SQLWhereFilterResolver implements SQLExpressionResolver<WhereFilter> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends WhereFilter> getExpressionType() {
		return WhereFilter.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(WhereFilter expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		String sql = expression.getSQL();

		// parameters
		if (expression.getParameters() != null) {
			for (int i = 0; i < expression.getParameters().size(); i++) {
				final Object parameter = expression.getParameters().get(i);
				int idx = sql.indexOf('?');
				if (idx < 0) {
					throw new InvalidExpressionException("Cannot replace parameter [" + parameter + "] in sql ["
							+ expression.getSQL() + "]: no placeholder found at index [" + (i + 1) + "]");
				}
				sql = sql.replaceFirst("\\?",
						context.addNamedParameter(SQLParameter.create(parameter, parameter.getClass())));
			}
		}

		// get SQL
		return Optional.of(SQLExpression.create(sql));

	}

}
