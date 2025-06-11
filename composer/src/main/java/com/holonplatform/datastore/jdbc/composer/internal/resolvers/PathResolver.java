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

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.DataMappable;
import com.holonplatform.core.Path;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * {@link Path} expression resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PathResolver implements SQLExpressionResolver<Path> {

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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(Path expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// intermediate resolution
		final Path<?> path = context.resolve(expression, Path.class).orElse(expression);

		// get path name
		final String name = getPathName(path);

		// check parent alias
		Optional<String> alias = path.getParent()
				.flatMap(parent -> context.isStatementCompositionContext().flatMap(ctx -> ctx.getAliasOrRoot(parent)));
		if (!alias.isPresent()) {
			alias = context.isStatementCompositionContext().flatMap(ctx -> ctx.getAliasOrRoot(path));
		}

		// serialize path
		return Optional.of(SQLExpression.create(alias.map(a -> a + "." + name).orElse(name)));

	}

	/**
	 * Get the path data model name, using {@link DataMappable#getDataPath()} if available or returning the path name if
	 * not.
	 * @param path The path for which to obtain the data path name
	 * @return The data path name
	 */
	private static String getPathName(Path<?> path) {
		return path.getDataPath().orElse(path.getName());
	}

}
