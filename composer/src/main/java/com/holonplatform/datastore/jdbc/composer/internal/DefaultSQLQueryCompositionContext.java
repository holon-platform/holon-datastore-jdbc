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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Aliasable;
import com.holonplatform.core.datastore.relational.Aliasable.AliasablePath;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext;

/**
 * Default {@link SQLQueryCompositionContext} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLQueryCompositionContext extends DefaultSQLCompositionContext
		implements SQLQueryCompositionContext {

	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	/**
	 * Root data target
	 */
	private final RelationalTarget<?> rootTarget;

	/**
	 * Alias mode
	 */
	private final AliasMode aliasMode;

	/**
	 * Path - alias map
	 */
	private final Map<String, String> pathAlias = new HashMap<>();

	/**
	 * Generated alias count to avoid duplicates
	 */
	private Map<String, Integer> generatedAliasCount = new HashMap<>();

	/**
	 * Constructor.
	 * @param context SQL context (not null)
	 * @param rootTarget Root query target (not null)
	 * @param aliasMode Alias handling mode (not null)
	 * @param parent Parent composition context
	 */
	public DefaultSQLQueryCompositionContext(SQLContext context, RelationalTarget<?> rootTarget, AliasMode aliasMode) {
		this(context, rootTarget, aliasMode, null);
	}

	/**
	 * Constructor with parent composition context.
	 * @param context SQL context (not null)
	 * @param rootTarget Root query target (not null)
	 * @param aliasMode Alias handling mode (not null)
	 * @param parent Parent composition context
	 */
	public DefaultSQLQueryCompositionContext(SQLContext context, RelationalTarget<?> rootTarget, AliasMode aliasMode,
			SQLCompositionContext parent) {
		super(context, parent);
		ObjectUtils.argumentNotNull(rootTarget, "Root query target must be not null");
		ObjectUtils.argumentNotNull(aliasMode, "AliasMode must be not null");
		this.rootTarget = rootTarget;
		this.aliasMode = aliasMode;

		// target alias
		parsePathAlias(rootTarget);
		// check joins
		rootTarget.getJoins().forEach(j -> parsePathAlias(j));
	}

	/**
	 * Get the query root target.
	 * @return the root data target
	 */
	protected RelationalTarget<?> getRootTarget() {
		return rootTarget;
	}

	/**
	 * Get the alias handling mode.
	 * @return the alias mode
	 */
	protected AliasMode getAliasMode() {
		return aliasMode;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext#getContextSequence()
	 */
	@Override
	public int getContextSequence() {
		int sequence = -1;
		SQLCompositionContext ctx = this;
		while (ctx != null) {
			if (ctx instanceof SQLQueryCompositionContext) {
				sequence++;
			}
			ctx = ctx.getParent().orElse(null);
		}
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext#getRootAlias()
	 */
	@Override
	public Optional<String> getRootAlias() {
		return getPathAlias(getRootTarget());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext#getAlias(com.holonplatform.core.Path,
	 * boolean)
	 */
	@Override
	public Optional<String> getAlias(Path<?> path, boolean useParentContext) {
		// get alias
		Optional<String> alias = getPathAlias(path);
		if (!alias.isPresent() && useParentContext) {
			// check parent
			return getParent().filter(parent -> (parent instanceof SQLQueryCompositionContext))
					.flatMap(parent -> ((SQLQueryCompositionContext) parent).getAlias(path, true));
		}
		return alias;
	}

	/**
	 * Get the alias of given path, if available
	 * @param path Path for which to get the alias (not null)
	 * @return Optional path alias
	 */
	protected Optional<String> getPathAlias(Path<?> path) {
		ObjectUtils.argumentNotNull(path, "Path must be not null");
		return Optional.ofNullable(pathAlias.get(path.getName()));
	}

	/**
	 * Get or generate an alias name for given path, only if alias mode is not {@link AliasMode#UNSUPPORTED}. If alias
	 * mode is {@link AliasMode#AUTO}, the alias name will be auto-generated.
	 * @param path Path for which to generate the alias (not null)
	 * @param checkParentContext Whether to check the parent context to obtain the alias, if available
	 * @return The alias name, either explicit if path is {@link Aliasable} or generated if alias mode is
	 *         {@link AliasMode#AUTO}. Always returns <code>null</code> if alias mode is {@link AliasMode#UNSUPPORTED}
	 */
	protected String parsePathAlias(final AliasablePath<?, ?> path) {
		final String name = path.getName();
		// check alias is supported
		return (getAliasMode() != AliasMode.UNSUPPORTED)
				? pathAlias.computeIfAbsent(name, t -> path.getAlias().orElse(checkGenerateAlias(path))) : null;
	}

	/**
	 * Generate an alias name for given path only if the alias mode is {@link AliasMode#AUTO}.
	 * @param path Path for which to generate the alias
	 * @return the generated alias name, or <code>null</code> if alias mode is not {@link AliasMode#AUTO}
	 */
	private String checkGenerateAlias(Path<?> path) {
		if (getAliasMode() == AliasMode.AUTO) {
			return generateAlias(path);
		}
		return null;
	}

	/**
	 * Generate an alias name for given path.
	 * @param path Path for which to generate the alias (not null)
	 * @return the alias name
	 */
	protected String generateAlias(Path<?> path) {
		final StringBuilder sb = new StringBuilder();

		// prefix (max 4 chars)
		final String name = path.getName();

		String prefix = ((name.length() <= 4) ? name : name.substring(0, 4)).toLowerCase();
		char[] pa = prefix.toCharArray();

		// sanitize prefix chars
		char[] sanitized = new char[pa.length];
		for (int i = 0; i < pa.length; i++) {
			sanitized[i] = (ALIAS_CHARS.indexOf(pa[i]) > -1) ? pa[i] : '_';
		}

		sb.append(sanitized);

		String partialAlias = sb.toString();

		// check duplicates
		int duplicateCount = 0;
		if (generatedAliasCount.containsKey(partialAlias)) {
			duplicateCount = generatedAliasCount.get(partialAlias) + 1;
		}
		generatedAliasCount.put(partialAlias, duplicateCount);
		sb.append("_");
		sb.append(duplicateCount);
		sb.append("_");

		// append resolution context sequence
		sb.append(getContextSequence());

		return sb.toString();
	}

}
