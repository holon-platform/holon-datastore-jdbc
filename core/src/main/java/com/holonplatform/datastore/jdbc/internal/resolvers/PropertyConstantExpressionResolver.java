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
import com.holonplatform.core.query.PropertyConstantExpression;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.LiteralValue;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * {@link PropertyConstantExpression} resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 10)
public enum PropertyConstantExpressionResolver implements ExpressionResolver<PropertyConstantExpression, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PropertyConstantExpression> getExpressionType() {
		return PropertyConstantExpression.class;
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
	public Optional<SQLToken> resolve(PropertyConstantExpression expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve
		return context.resolve(
				LiteralValue.create(expression.getModelValue(), expression.getModelType(),
						expression.getProperty().getConfiguration().getTemporalType()
								.orElse(JdbcDatastoreUtils.getTemporalType(expression, true).orElse(null))),
				SQLToken.class, context);
	}

}
