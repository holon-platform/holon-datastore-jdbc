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

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLLiteral;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * {@link SQLLiteral} expression resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum SQLLiteralResolver implements SQLExpressionResolver<SQLLiteral> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends SQLLiteral> getExpressionType() {
		return SQLLiteral.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(SQLLiteral expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check value
		final Object value = expression.getValue();

		if (value == null) {
			return Optional.of(SQLExpression.create("NULL"));
		}

		final String serialized;
		if (Collection.class.isAssignableFrom(value.getClass())) {
			serialized = ((Collection<?>) value).stream()
					.map(element -> context.getValueSerializer().serialize(element,
							((SQLLiteral<?>) expression).getTemporalType().orElse(null)))
					.collect(Collectors.joining(","));
		} else {
			serialized = context.getValueSerializer().serialize(value,
					((SQLLiteral<?>) expression).getTemporalType().orElse(null));
		}

		// serialize
		return Optional.of(SQLExpression.create(serialized));
	}

}
