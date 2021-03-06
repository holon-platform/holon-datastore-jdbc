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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLContextParametersHandler;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;
import com.holonplatform.datastore.jdbc.composer.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;

/**
 * Default {@link SQLCompositionContext} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLCompositionContext implements SQLCompositionContext {

	private final static Logger LOGGER = SQLComposerLogger.create();

	/**
	 * Expression resolvers
	 */
	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	/**
	 * SQL context
	 */
	private final SQLContext context;

	/**
	 * Optional parent context
	 */
	private final SQLCompositionContext parent;

	/**
	 * Named parameters handler
	 */
	private final SQLContextParametersHandler namedParametersHandler;

	/**
	 * Context hierarchy sequence
	 */
	private final int contextSequence;

	/**
	 * Default constructor.
	 * @param context SQL context (not null)
	 */
	public DefaultSQLCompositionContext(SQLContext context) {
		super();
		ObjectUtils.argumentNotNull(context, "SQL context must be not null");
		this.context = context;
		this.parent = null;
		this.contextSequence = 0;
		this.namedParametersHandler = SQLContextParametersHandler.create();
		// inherit resolvers
		addExpressionResolvers(context.getExpressionResolvers());
	}

	/**
	 * Constructor with parent composition context.
	 * @param parent Parent context (not null)
	 */
	public DefaultSQLCompositionContext(SQLCompositionContext parent) {
		super();
		ObjectUtils.argumentNotNull(parent, "Parent context must be not null");
		this.context = parent;
		this.parent = parent;
		this.contextSequence = SQLCompositionContext.getContextSequence(parent, SQLCompositionContext.class) + 1;
		this.namedParametersHandler = parent.getNamedParametersHandler();
		// inherit resolvers
		addExpressionResolvers(parent.getExpressionResolvers());
	}

	/**
	 * Get the SQL context.
	 * @return the SQL context
	 */
	protected SQLContext getContext() {
		return context;
	}

	/**
	 * Get the context hierarchy sequence.
	 * @return the context sequence
	 */
	protected int getContextSequence() {
		return contextSequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#getParent()
	 */
	@Override
	public Optional<SQLCompositionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#getDialect()
	 */
	@Override
	public SQLDialect getDialect() {
		return getContext().getDialect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#getValueSerializer()
	 */
	@Override
	public SQLValueSerializer getValueSerializer() {
		return getContext().getValueSerializer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#getValueDeserializer()
	 */
	@Override
	public SQLValueDeserializer getValueDeserializer() {
		return getContext().getValueDeserializer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String sql) {
		getContext().trace(sql);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#getNamedParametersHandler()
	 */
	@Override
	public SQLContextParametersHandler getNamedParametersHandler() {
		return namedParametersHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#prepareStatement(java.lang.String)
	 */
	@Override
	public SQLStatement prepareStatement(String sql) {
		ObjectUtils.argumentNotNull(sql, "SQL statement must be not null");

		LOGGER.debug(() -> "Prepare statement: " + sql);

		final StringBuilder sb = new StringBuilder();

		// check named parameters
		final Map<String, SQLParameter<?>> namedParameters = getNamedParametersHandler().getNamedParameters();

		final List<SQLParameter<?>> parameters = new ArrayList<>(namedParameters.size());

		final char[] chars = sql.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == ':' && (chars.length - i) >= 7) {
				String namedParameterPlaceholder = String.valueOf(Arrays.copyOfRange(chars, i, i + 7));

				SQLParameter<?> parameter = namedParameters.get(namedParameterPlaceholder);
				if (parameter == null) {
					throw new SQLStatementPreparationException("The named parameter " + namedParameterPlaceholder
							+ " at index " + i + " was not found in SQL composition context");
				}

				LOGGER.debug(() -> "Resolve parameter for placeholder " + namedParameterPlaceholder);

				// intermediate parameter resolution
				final SQLParameter<?> actualParameter = resolve(parameter, SQLParameter.class).orElse(parameter);

				// resolve parameter as SQL
				final SQLExpression parameterExpression = resolve(actualParameter, SQLExpression.class).orElseThrow(
						() -> new InvalidExpressionException("Failed to resolve parameter [" + actualParameter + "]"));

				LOGGER.debug(() -> "Resolved parameter for placeholder " + namedParameterPlaceholder + " as "
						+ parameterExpression.getValue());

				// replace parameter
				sb.append(parameterExpression.getValue());
				parameters.add(actualParameter);

				i = i + 6;
				continue;
			}
			sb.append(chars[i]);
		}

		return SQLStatement.create(sb.toString(), parameters.toArray(new SQLParameter[parameters.size()]));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#childContext(com.holonplatform.datastore.jdbc.
	 * composer.SQLCompositionContext.AliasMode)
	 */
	@Override
	public SQLCompositionContext childContext() {
		return new DefaultSQLCompositionContext(this);
	}

	// Expression resolvers

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#getExpressionResolvers()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<ExpressionResolver> getExpressionResolvers() {
		return expressionResolverRegistry.getExpressionResolvers();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#resolve(com.holonplatform.core.Expression,
	 * java.lang.Class, com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType,
			ResolutionContext context) throws InvalidExpressionException {
		return expressionResolverRegistry.resolve(expression, resolutionType, context);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.holonplatform.core.
	 * ExpressionResolver)
	 */
	@Override
	public <E extends Expression, R extends Expression> void addExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		expressionResolverRegistry.addExpressionResolver(expressionResolver);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.holonplatform.
	 * core.ExpressionResolver)
	 */
	@Override
	public <E extends Expression, R extends Expression> void removeExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		expressionResolverRegistry.removeExpressionResolver(expressionResolver);
	}

}
