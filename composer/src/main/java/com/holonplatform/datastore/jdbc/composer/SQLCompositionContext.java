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

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLCompositionContext;

/**
 * SQL compostion context.
 * <p>
 * Supports {@link ExpressionResolver}s to resolve SQL expressions and extends {@link ResolutionContext}.
 * </p>
 * <p>
 * Supports named parameters definitions, which can be added using {@link #addNamedParameter(SQLParameter)}. The named
 * parameters can be normalized as SQL statement parameters through the {@link #prepareStatement(String)} method.
 * </p>
 * <p>
 * SQL compostion contexts are hierarchical and provides methods to get the parent context and to create children using
 * {@link #childContext()}.
 * </p>
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
	 * Get the SQL named parameters handler.
	 * @return the SQL named parameters handler
	 */
	SQLContextParametersHandler getNamedParametersHandler();

	/**
	 * Convenience method to add a named parameter using current {@link SQLContextParametersHandler}.
	 * @param <T> Parameter expression type
	 * @param parameter Parameter definition (not null)
	 * @return The generated parameter name
	 */
	default <T> String addNamedParameter(SQLParameter<T> parameter) {
		return getNamedParametersHandler().addNamedParameter(parameter);
	}

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
	 * Checks whether this context is a {@link SQLStatementCompositionContext}.
	 * @return If this context is a {@link SQLStatementCompositionContext} returns the context itself as
	 *         {@link SQLStatementCompositionContext}, otherwise returns an empty Optional
	 */
	default Optional<SQLStatementCompositionContext> isStatementCompositionContext() {
		return Optional.ofNullable(
				(this instanceof SQLStatementCompositionContext) ? (SQLStatementCompositionContext) this : null);
	}

	/**
	 * Create a new default {@link SQLCompositionContext}.
	 * @param context SQL context to use (not null)
	 * @return A new {@link SQLCompositionContext}
	 */
	static SQLCompositionContext create(SQLContext context) {
		return new DefaultSQLCompositionContext(context);
	}

	// Utils

	/**
	 * Get the given <code>context</code> hierarchy sequence, where <code>0</code> is the sequence number of the root
	 * context.
	 * @param context The context for which to obtain the sequence (not null)
	 * @param contextType The context type to take into account to calculate the sequence
	 * @return Context sequence
	 */
	public static int getContextSequence(SQLCompositionContext context,
			Class<? extends SQLCompositionContext> contextType) {
		ObjectUtils.argumentNotNull(context, "Context must be not null");
		final Class<?> type = (contextType != null) ? contextType : context.getClass();
		int sequence = -1;
		SQLCompositionContext ctx = context;
		while (ctx != null) {
			if (type.isAssignableFrom(ctx.getClass())) {
				sequence++;
			}
			ctx = ctx.getParent().orElse(null);
		}
		return sequence;
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
