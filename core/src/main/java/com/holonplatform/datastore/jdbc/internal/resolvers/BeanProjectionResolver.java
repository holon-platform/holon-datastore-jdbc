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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.converters.BeanResultSetConverter;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.ProjectionContext;

/**
 * Bean projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum BeanProjectionResolver implements ExpressionResolver<BeanProjection, ProjectionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(BeanProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		final DefaultProjectionContext ctx = new DefaultProjectionContext<>(jdbcContext);
		final BeanPropertySet<?> bps = BeanIntrospector.get().getPropertySet(expression.getBeanClass());
		final Map<String, Path<?>> pathSelection = new LinkedHashMap<>();

		List<Path> selection = ((BeanProjection<?>) expression).getSelection().map(s -> Arrays.asList(s))
				.orElse(bps.stream().map(p -> (Path) p).collect(Collectors.toList()));
		for (Path<?> path : selection) {
			if (QueryExpression.class.isAssignableFrom(path.getClass())) {
				final String alias = ctx.addSelection(jdbcContext
						.resolveExpression((QueryExpression<?>) path, SQLToken.class).getValue());
				pathSelection.put(alias, path);
			}
		}

		ctx.setConverter(new BeanResultSetConverter<>(jdbcContext.getDialect(), bps, pathSelection));

		return Optional.of(ctx);
	}

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
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

}
