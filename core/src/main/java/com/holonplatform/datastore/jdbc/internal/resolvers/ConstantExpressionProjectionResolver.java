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
import com.holonplatform.core.query.ConstantExpressionProjection;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jdbc.internal.converters.QueryExpressionResultSetConverter;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.ProjectionContext;

/**
 * {@link ConstantExpressionProjection} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum ConstantExpressionProjectionResolver
		implements ExpressionResolver<ConstantExpressionProjection, ProjectionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(ConstantExpressionProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		// serialize as a literal value
		final String sql = SQLValueSerializer.serializeValue(expression.getValue(), null);
		
		final DefaultProjectionContext ctx = new DefaultProjectionContext<>(jdbcContext);
		String alias = ctx.addSelection(sql, false);
		ctx.setConverter(new QueryExpressionResultSetConverter(jdbcContext.getDialect(), expression, alias));

		return Optional.of(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends ConstantExpressionProjection> getExpressionType() {
		return ConstantExpressionProjection.class;
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
