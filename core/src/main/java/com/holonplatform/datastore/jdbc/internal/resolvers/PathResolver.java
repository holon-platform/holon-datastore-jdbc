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
import com.holonplatform.core.Path;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * {@link Path} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PathResolver implements ExpressionResolver<Path, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends Path> getExpressionType() {
		return Path.class;
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
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(Path expression, ResolutionContext context) throws InvalidExpressionException {

		final JdbcResolutionContext ctx = JdbcResolutionContext.checkContext(context);

		// intermediate resolution and validation
		Path<?> path = context.resolve(expression, Path.class, context).orElse(expression);
		path.validate();

		// get path name
		final String name = path.getName();
		// check alias (avoid parent alias for wildcard)
		final String actualName = ("*".equals(name)) ? name
				: ctx.getTargetAlias(path.getParent().orElse(null)).map(a -> a + "." + name).orElse(name);

		return Optional.of(SQLToken.create(ctx.getDialect().getPathProcessor()
				.map(p -> p.processPath(actualName, path, ctx.getResolutionQueryClause().orElse(null)))
				.orElse(actualName)));
	}

}
