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

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * {@link SQLParameter} expression resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum SQLParameterResolver implements SQLExpressionResolver<SQLParameter> {

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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(SQLParameter expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// serialize as named parameters
		return Optional
				.of(SQLExpression
						.create(isCollectionValue(expression.getValue())
								.map(collection -> collection.stream()
										// if collection, add values as parameters and return a ',' separated string
										.map(value -> context.addNamedParameter(
												SQLParameter.create(value, expression.getTemporalType().orElse(null))))
										.collect(Collectors.joining(",")))
								.orElse(context.addNamedParameter(expression))));
	}

	/**
	 * Check whether given value is a {@link Collection} value.
	 * @param value The value
	 * @return The {@link Collection} value if value is a {@link Collection} type or an array, an empty Optional
	 *         otherwise
	 */
	@SuppressWarnings("unchecked")
	private static Optional<Collection<Object>> isCollectionValue(Object value) {
		if (value != null) {
			// check Collection
			if (Collection.class.isAssignableFrom(value.getClass())) {
				return Optional.of((Collection<Object>) value);
			}
			// check array
			if (value.getClass().isArray()) {
				return Optional.of(Arrays.asList((Object[]) value));
			}
		}
		return Optional.empty();
	}

}
