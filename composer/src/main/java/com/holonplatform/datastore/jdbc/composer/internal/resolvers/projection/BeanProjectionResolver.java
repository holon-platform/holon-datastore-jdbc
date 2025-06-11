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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection;
import com.holonplatform.datastore.jdbc.composer.internal.converters.BeanSQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * Bean projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum BeanProjectionResolver implements SQLContextExpressionResolver<BeanProjection, SQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends BeanProjection> getExpressionType() {
		return BeanProjection.class;
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
	@SuppressWarnings("unchecked")
	@Override
	public Optional<SQLProjection> resolve(BeanProjection expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		final BeanProjection<?> beanProjection = expression;

		// validate
		expression.validate();

		// build projection
		final MutableSQLProjection<?> projection = SQLProjection.create(beanProjection.getBeanClass(), context);

		// get bean property set
		final BeanPropertySet<?> bps = BeanIntrospector.get().getPropertySet(beanProjection.getBeanClass());

		// resolve selection
		final Map<String, Path<?>> pathSelection = new LinkedHashMap<>();

		List<Path> selection = beanProjection.getSelection().map(s -> Arrays.asList(s))
				.orElse(bps.stream().collect(Collectors.toList()));
		for (Path<?> path : selection) {
			pathSelection.put(projection.addSelection(context.resolveOrFail(path, SQLExpression.class).getValue()),
					path);
		}

		// set converter
		projection.setConverter(new BeanSQLResultConverter(bps, pathSelection));

		return Optional.of(projection);
	}

}
