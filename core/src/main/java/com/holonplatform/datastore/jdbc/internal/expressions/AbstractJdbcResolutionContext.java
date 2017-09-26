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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.relational.Aliasable.AliasablePath;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Base {@link JdbcResolutionContext} class.
 *
 * @since 5.0.0
 */
public abstract class AbstractJdbcResolutionContext implements JdbcResolutionContext {

	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	/**
	 * Optional parent context
	 */
	private final JdbcResolutionContext parent;

	/**
	 * Alias mode
	 */
	private final AliasMode aliasMode;

	/**
	 * Current resolution query clause
	 */
	private ResolutionQueryClause clause;

	/**
	 * Data target
	 */
	private DataTarget<?> target;

	/**
	 * Path aliases
	 */
	private final Map<String, String> pathAlias = new HashMap<>(4);

	/**
	 * Generated aliases
	 */
	private Map<String, Integer> generatedAlias = new HashMap<>();

	/**
	 * Resolution context sequence in hierarchy
	 */
	private final int sequence;

	/**
	 * Constructor
	 * @param parent Optional parent context
	 * @param sequence Context sequence in hierarchy
	 * @param aliasMode Alias mode
	 */
	public AbstractJdbcResolutionContext(JdbcResolutionContext parent, int sequence, AliasMode aliasMode) {
		super();
		this.parent = parent;
		this.aliasMode = (aliasMode != null) ? aliasMode : AliasMode.DEFAULT;
		this.sequence = sequence;
	}

	/**
	 * Get the overall context sequence.
	 * @return the context sequence
	 */
	@Override
	public int getSequence() {
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getParent()
	 */
	@Override
	public Optional<JdbcResolutionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect.DialectResolutionContext#getResolutionQueryClause()
	 */
	@Override
	public Optional<ResolutionQueryClause> getResolutionQueryClause() {
		return Optional.ofNullable(clause);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#setResolutionQueryClause(com.
	 * holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause)
	 */
	@Override
	public void setResolutionQueryClause(ResolutionQueryClause clause) {
		this.clause = clause;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getAliasMode()
	 */
	@Override
	public AliasMode getAliasMode() {
		return aliasMode;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getAlias(com.holonplatform.core.Path)
	 */
	@Override
	public Optional<String> getTargetAlias(Path<?> path) {
		if (path == null) {
			// root alias
			return (target != null) ? Optional.ofNullable(pathAlias.get(target.getName())) : Optional.empty();
		}
		String alias = pathAlias.get(path.fullName());
		// check parent
		if (alias == null && getParent().isPresent()) {
			JdbcResolutionContext ctx = getParent().get();
			while (ctx != null) {
				if (ctx.getAliasMode() != AliasMode.UNSUPPORTED) {
					Optional<String> parentAlias = ctx.getTargetAlias(path);
					if (parentAlias.isPresent()) {
						alias = parentAlias.get();
						break;
					}
				}
				ctx = ctx.getParent().orElse(null);
			}
		}
		return Optional.ofNullable(alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTarget(RelationalTarget<?> target) {
		ObjectUtils.argumentNotNull(target, "DataTarget must be not null");
		this.target = target;
		// alias
		getOrCreatePathAlias(target).ifPresent(a -> pathAlias.put(target.getName(), a));
		// check joins
		target.getJoins().forEach(j -> {
			getOrCreatePathAlias(j).ifPresent(a -> pathAlias.put(j.getName(), a));
		});
	}

	/**
	 * Get the alias for given path, generating an alias name if no alias is defined and alias mode is
	 * {@link AliasMode#AUTO}.
	 * @param path Path for which to obtain the alias
	 * @return Path alias
	 */
	private Optional<String> getOrCreatePathAlias(AliasablePath<?, ?> path) {
		if (AliasMode.UNSUPPORTED != getAliasMode()) {
			if (path != null) {
				if (path.getAlias().isPresent()) {
					return path.getAlias();
				}
				if (AliasMode.AUTO == getAliasMode()) {
					// generate alias
					return Optional.of(generateTargetAlias(path.getName()));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Generate an alias for given data target name.
	 * @param targetName Target name
	 * @return Alias name
	 */
	protected String generateTargetAlias(String targetName) {

		ObjectUtils.argumentNotNull(targetName, "Target name must be not null");

		StringBuilder sb = new StringBuilder();

		// prefix (max 4 chars)
		String prefix = ((targetName.length() <= 4) ? targetName : targetName.substring(0, 4)).toLowerCase();
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
		if (generatedAlias.containsKey(partialAlias)) {
			duplicateCount = generatedAlias.get(partialAlias) + 1;
		}
		generatedAlias.put(partialAlias, duplicateCount);
		sb.append("_");
		sb.append(duplicateCount);
		sb.append("_");

		// append resolution context sequence
		sb.append(getSequence());

		return sb.toString();
	}

}
