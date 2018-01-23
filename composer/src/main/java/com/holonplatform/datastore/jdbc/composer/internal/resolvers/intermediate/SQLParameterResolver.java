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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.NullExpression;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterValue;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link SQLParameter} expression resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum SQLParameterResolver implements SQLContextExpressionResolver<SQLParameter, SQLParameterValue> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends SQLParameter> getExpressionType() {
		return SQLParameter.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLParameterValue> getResolvedType() {
		return SQLParameterValue.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLParameterValue> resolve(SQLParameter expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// Null expression
		if (expression.getExpression() instanceof NullExpression) {
			final NullExpression<?> nullExpression = (NullExpression<?>) expression.getExpression();
			return Optional.of(SQLParameterValue.create(nullExpression.getModelValue(), nullExpression.getModelType()));
		}
		// ConstantExpression
		if (expression.getExpression() instanceof ConstantExpression) {
			final ConstantExpression<?> constant = (ConstantExpression<?>) expression.getExpression();
			return Optional.of(SQLParameterValue.create(constant.getModelValue(), constant.getModelType(),
					constant.getTemporalType().orElse(null)));
		}

		return Optional.empty();
	}

}
