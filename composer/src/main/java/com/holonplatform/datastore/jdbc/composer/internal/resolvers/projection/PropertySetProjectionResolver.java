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
package com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.PropertySetProjection;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection;
import com.holonplatform.datastore.jdbc.composer.internal.converters.PropertyBoxSQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * Property set projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum PropertySetProjectionResolver
		implements SQLContextExpressionResolver<PropertySetProjection, SQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PropertySetProjection> getExpressionType() {
		return PropertySetProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLProjection> getResolvedType() {
		return SQLProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLProjection> resolve(PropertySetProjection expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// build projection
		MutableSQLProjection<PropertyBox> projection = SQLProjection.create(expression.getType(), context);

		// resolve selection
		final Map<String, Property<?>> selectionProperties = new LinkedHashMap<>();
		final Map<String, TypedExpression<?>> selectionExpressions = new LinkedHashMap<>();

		for (Property<?> property : expression.getPropertySet()) {
			if (QueryProjection.class.isAssignableFrom(property.getClass())) {
				final String alias = projection
						.addSelection(context.resolveOrFail(property, SQLExpression.class).getValue());
				selectionProperties.put(alias, property);
				selectionExpressions.put(alias, property);
			}
		}

		// set converter
		projection.setConverter(new PropertyBoxSQLResultConverter(expression.getPropertySet(), selectionProperties,
				selectionExpressions));

		return Optional.of(projection);
	}

}
