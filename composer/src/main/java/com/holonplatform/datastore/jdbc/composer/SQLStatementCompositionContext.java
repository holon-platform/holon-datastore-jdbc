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
package com.holonplatform.datastore.jdbc.composer;

import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Aliasable;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLStatementCompositionContext;

/**
 * A {@link SQLCompositionContext} extension which supports statement alias generation and inspection.
 *
 * @since 5.1.0
 */
public interface SQLStatementCompositionContext extends SQLCompositionContext {

	/**
	 * Alias handling mode
	 */
	public enum AliasMode {

		/**
		 * Default behaviour: use an alias only if explicitly provided
		 */
		DEFAULT,

		/**
		 * Use the explicitly provided alias if available, generate an alias name otherwise
		 */
		AUTO,

		/**
		 * Never use an alias name, even if explicitly provided
		 */
		UNSUPPORTED;

	}

	/**
	 * Get the query root path (target) alias, if available and alias mode is not {@link AliasMode#UNSUPPORTED}.
	 * <p>
	 * The alias will be the explicit alias name if the root path is {@link Aliasable}, or an auto generated name if
	 * alias mode is {@link AliasMode#AUTO}.
	 * </p>
	 * @return the query root path alias, empty if not available
	 */
	Optional<String> getRootAlias();

	/**
	 * Get the given <code>path</code> alias, if available and alias mode is not {@link AliasMode#UNSUPPORTED}.
	 * <p>
	 * The alias will be the explicit alias name if the path is {@link Aliasable}, or an auto generated name if alias
	 * mode is {@link AliasMode#AUTO}.
	 * </p>
	 * @param path Path for which to get the alias (not null)
	 * @param useParentContext Whether to check in parent contexts if an alias is available when not available is
	 *        current context
	 * @return the path alias, empty if not available
	 */
	Optional<String> getAlias(Path<?> path, boolean useParentContext);

	/**
	 * Get the given <code>path</code> alias, falling back to query root alias if not available.
	 * @param path Path for which to get the alias (not null)
	 * @return the path alias, empty if not available
	 */
	default Optional<String> getAliasOrRoot(Path<?> path) {
		Optional<String> alias = getAlias(path, true);
		if (!alias.isPresent()) {
			return getRootAlias();
		}
		return alias;
	}

	// builders

	/**
	 * Create a new default {@link SQLStatementCompositionContext}.
	 * @param context SQL context to use (not null)
	 * @param rootTarget Root query target (not null)
	 * @param aliasMode Alias handling mode (not null)
	 * @return A new {@link SQLStatementCompositionContext}
	 */
	static SQLStatementCompositionContext create(SQLContext context, RelationalTarget<?> rootTarget, AliasMode aliasMode) {
		return new DefaultSQLStatementCompositionContext(context, rootTarget, aliasMode);
	}
	
	/**
	 * Create a new {@link SQLStatementCompositionContext} as a child of given {@link SQLCompositionContext}.
	 * @param parent Parent context (not null)
	 * @param rootTarget Root query target (not null)
	 * @param aliasMode Alias handling mode (not null)
	 * @return A new {@link SQLStatementCompositionContext}
	 */
	static SQLStatementCompositionContext asChild(SQLCompositionContext parent, RelationalTarget<?> rootTarget, AliasMode aliasMode) {
		return new DefaultSQLStatementCompositionContext(parent, rootTarget, aliasMode);
	}

}
