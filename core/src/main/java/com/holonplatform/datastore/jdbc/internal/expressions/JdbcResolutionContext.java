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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.DialectResolutionContext;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * JDBC {@link ResolutionContext}.
 *
 * @since 5.0.0
 */
public interface JdbcResolutionContext extends ResolutionContext, DialectResolutionContext {

	/**
	 * Current resolution query clause enumeration
	 */
	public enum ResolutionQueryClause {

		SELECT, FROM, WHERE, ORDERBY, GROUPBY, SET;

	}

	/**
	 * Data target alias handling mode
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
	 * Get the context sequence number
	 * @return The context sequence number, <code>0</code> for the main context (with no parent)
	 */
	int getSequence();

	/**
	 * Get the optional parent context
	 * @return Optional parent context
	 */
	Optional<JdbcResolutionContext> getParent();

	/**
	 * Get the {@link JdbcDialect} to use.
	 * @return The JDBC dialect to use
	 */
	JdbcDialect getDialect();

	/**
	 * Get the data targets alias handling mode
	 * @return Alias handling mode
	 */
	AliasMode getAliasMode();

	/**
	 * Get the optional alias associated to given data target path
	 * @param path Path to get the alias for, <code>null</code> for the the root data target alias, if available
	 * @return Optional path alias
	 */
	Optional<String> getTargetAlias(Path<?> path);

	/**
	 * Add a named parameter to context
	 * @param value Parameter value
	 * @return Generated parameter name
	 */
	String addNamedParameter(ParameterValue value);

	/**
	 * Get the context named parameters.
	 * @return Map of named parameters with name - value associations
	 */
	Map<String, ParameterValue> getNamedParameters();

	/**
	 * Set the query clause to resolve with this context.
	 * @param clause The resolution query clause
	 */
	void setResolutionQueryClause(ResolutionQueryClause clause);

	/**
	 * Set the resolution context data target
	 * @param target The data target to set
	 */
	void setTarget(RelationalTarget<?> target);

	/**
	 * Create a new {@link JdbcResolutionContext} as child of this context
	 * @param aliasMode The {@link AliasMode} to use
	 * @return A new {@link JdbcResolutionContext} with this context as parent
	 */
	JdbcResolutionContext childContext(AliasMode aliasMode);

	/**
	 * Create a new {@link JdbcResolutionContext}.
	 * @param expressionResolverHandler Expression resolver handler (not null)
	 * @param dialect Dialect (not null)
	 * @param aliasMode Alias handling mode
	 * @return A new {@link JdbcResolutionContext} instance
	 */
	static JdbcResolutionContext create(ExpressionResolverHandler expressionResolverHandler, JdbcDialect dialect,
			AliasMode aliasMode) {
		return new DefaultJdbcResolutionContext(expressionResolverHandler, dialect, aliasMode);
	}

	/**
	 * Check the given context is a {@link JdbcResolutionContext}.
	 * @param context Context to check (not null)
	 * @return The JdbcResolutionContext
	 * @throws InvalidExpressionException If given context is not a JdbcResolutionContext
	 */
	static JdbcResolutionContext checkContext(ResolutionContext context) {
		ObjectUtils.argumentNotNull(context, "Null ResolutionContext");
		if (!JdbcResolutionContext.class.isAssignableFrom(context.getClass())) {
			throw new InvalidExpressionException("Invalid ResolutionContext type: expected ["
					+ JdbcResolutionContext.class.getName() + "], got [" + context.getClass().getName() + "]");
		}
		return (JdbcResolutionContext) context;
	}

}
