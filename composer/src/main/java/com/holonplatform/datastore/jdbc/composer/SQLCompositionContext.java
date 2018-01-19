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

import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLCompositionContext;

/**
 * TODO
 *
 * @since 5.1.0
 */
public interface SQLCompositionContext extends SQLContext, ResolutionContext, ExpressionResolverSupport {

	/**
	 * Get the parent context, if available.
	 * @return Optional parent context
	 */
	Optional<SQLCompositionContext> getParent();

	/**
	 * Declare a named parameter and add it to this context.
	 * @param parameter Parameter definition (not null)
	 * @return Generated parameter SQL
	 */
	String addNamedParameter(SQLParameter parameter);

	/**
	 * Get the context named parameters.
	 * @return A map of parameters name and {@link SQLParameter}, empty if none
	 */
	Map<String, SQLParameter> getNamedParameters();

	/**
	 * Prepare given SQL statement, replacing named parameters with the default <code>?</code> parameter placeholder.
	 * @param sql The SQL statement to prepare (not null)
	 * @return An {@link SQLStatement} which provides the prepared SQL and the statement parameter definition in the
	 *         right order according to the parameters placeholder index
	 * @throws SQLStatementPreparationException If an error occurred
	 */
	SQLStatement prepareStatement(String sql);

	/**
	 * Try to resolve given <code>expression</code> using current context resolvers to obtain a
	 * <code>resolutionType</code> type expression.
	 * <p>
	 * The resolved expression is validate using {@link Expression#validate()} before returning it to caller.
	 * </p>
	 * @param <E> Expression type
	 * @param <R> Resolution type
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @return Resolved expression
	 */
	default <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType)
			throws InvalidExpressionException {
		// resolve
		return resolve(expression, resolutionType, this).map(e -> {
			// validate
			e.validate();
			return e;
		});
	}

	/**
	 * Resolve given <code>expression</code> using current context resolvers to obtain a <code>resolutionType</code>
	 * type expression. If no {@link ExpressionResolver} is available to resolve given expression, an
	 * {@link InvalidExpressionException} is thrown.
	 * <p>
	 * The resolved expression is validate using {@link Expression#validate()} before returning it to caller.
	 * </p>
	 * @param <E> Expression type
	 * @param <R> Resolution type
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @return Resolved expression
	 * @throws InvalidExpressionException If an error occurred during resolution, or if no {@link ExpressionResolver} is
	 *         available to resolve given expression or if expression validation failed
	 */
	default <E extends Expression, R extends Expression> R resolveOrFail(E expression, Class<R> resolutionType) {
		return resolve(expression, resolutionType)
				.orElseThrow(() -> new InvalidExpressionException("Failed to resolve expression [" + expression + "]"));
	}

	// builders

	/**
	 * Create a new {@link SQLCompositionContext} as child of this context. This context will be setted as parent of the
	 * new context.
	 * @return A new {@link SQLCompositionContext} with this context as parent
	 */
	SQLCompositionContext childContext();

	/**
	 * Create a new {@link SQLQueryCompositionContext} as child of this context. This context will be setted as parent
	 * of the new context.
	 * @param rootTarget Root query target (not null)
	 * @param aliasMode Alias handling mode (not null)
	 * @return @return A new {@link SQLQueryCompositionContext} with this context as parent
	 */
	SQLQueryCompositionContext childQueryContext(RelationalTarget<?> rootTarget, AliasMode aliasMode);

	/**
	 * Checks whether this context is a {@link SQLQueryCompositionContext}.
	 * @return If this context is a {@link SQLQueryCompositionContext} returns the context itself as
	 *         {@link SQLQueryCompositionContext}, otherwise returns an empty Optional
	 */
	default Optional<SQLQueryCompositionContext> isQueryCompositionContext() {
		return Optional
				.ofNullable((this instanceof SQLQueryCompositionContext) ? (SQLQueryCompositionContext) this : null);
	}

	/**
	 * Create a new default {@link SQLCompositionContext}.
	 * @param context SQL context to use (not null)
	 * @return A new {@link SQLCompositionContext}
	 */
	static SQLCompositionContext create(SQLContext context) {
		return new DefaultSQLCompositionContext(context);
	}

	// Exceptions

	/**
	 * Runtime exception related to SQL statements preparation errors.
	 */
	public class SQLStatementPreparationException extends RuntimeException {

		private static final long serialVersionUID = -3053162143629153499L;

		/**
		 * Constructor.
		 * @param message Error message
		 */
		public SQLStatementPreparationException(String message) {
			super(message);
		}

		/**
		 * Constructor.
		 * @param message Error message
		 * @param cause The {@link Throwable} which caused this exception
		 */
		public SQLStatementPreparationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
