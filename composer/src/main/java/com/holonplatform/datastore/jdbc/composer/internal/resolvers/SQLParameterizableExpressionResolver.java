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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.CollectionConstantExpression;
import com.holonplatform.core.ConstantConverterExpression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.NullExpression;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterizableExpression;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * {@link SQLParameterizableExpression} resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum SQLParameterizableExpressionResolver implements SQLExpressionResolver<SQLParameterizableExpression> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends SQLParameterizableExpression> getExpressionType() {
		return SQLParameterizableExpression.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(SQLParameterizableExpression parameterizableExpression,
			SQLCompositionContext context) throws InvalidExpressionException {

		// validate
		parameterizableExpression.validate();

		final TypedExpression<?> expression = parameterizableExpression.getExpression();

		String namedParameter = null;

		// Null expression
		if (expression instanceof NullExpression) {
			expression.validate();
			final NullExpression<?> nullExpression = (NullExpression<?>) expression;
			namedParameter = context.addNamedParameter(
					SQLParameter.create(nullExpression.getModelValue(), nullExpression.getModelType()));
		}

		// CollectionExpression
		else if (expression instanceof CollectionConstantExpression) {
			expression.validate();
			final CollectionConstantExpression<?> collection = (CollectionConstantExpression<?>) expression;
			Collection<?> values = collection.getModelValue();
			if (values == null) {
				namedParameter = context.addNamedParameter(SQLParameter.create(null, collection.getModelType()));
			} else {
				namedParameter = values.stream()
						.map(value -> context.addNamedParameter(SQLParameter.create(value, collection.getModelType())))
						.collect(Collectors.joining(","));
			}
		}

		// ConstantExpression
		else if (expression instanceof ConstantConverterExpression) {
			expression.validate();
			final ConstantConverterExpression<?, ?> constant = (ConstantConverterExpression<?, ?>) expression;
			namedParameter = context.addNamedParameter(SQLParameter.create(constant.getModelValue(),
					constant.getModelType(), constant.getTemporalType().orElse(null)));
		}

		if (namedParameter != null) {
			// resolved as parameter(s)
			return Optional.of(SQLExpression.create(namedParameter));
		}

		// resolve as expression
		return context.resolve(expression, SQLExpression.class);
	}

}
