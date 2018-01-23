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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;
import com.holonplatform.datastore.jdbc.composer.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterValue;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;

/**
 * Default {@link SQLCompositionContext} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLCompositionContext implements SQLCompositionContext {

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
	 * Named parameters
	 */
	private final Map<String, SQLParameter<?>> namedParameters = new HashMap<>();

	/**
	 * Default constructor.
	 * @param context SQL context (not null)
	 * @param aliasMode Alias handling mode
	 */
	public DefaultSQLCompositionContext(SQLContext context) {
		this(context, null);
	}

	/**
	 * Constructor with parent composition context.
	 * @param context SQL context (not null)
	 * @param aliasMode Alias handling mode
	 * @param parent Parent composition context
	 */
	public DefaultSQLCompositionContext(SQLContext context, SQLCompositionContext parent) {
		super();
		ObjectUtils.argumentNotNull(context, "SQLContext must be not null");
		this.context = context;
		this.parent = parent;
		// inherit resolvers
		addExpressionResolvers(context.getExpressionResolvers());
	}

	/**
	 * Get the SQL context.
	 * @return the SQL context
	 */
	protected SQLContext getContext() {
		return context;
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
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#addNamedParameter(com.holonplatform.datastore.
	 * jdbc.composer.expression.SQLParameter)
	 */
	@Override
	public <T> String addNamedParameter(SQLParameter<T> parameter) {
		ObjectUtils.argumentNotNull(parameter, "Parameter must be not null");
		return generateAndAddParameter(parameter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#getNamedParameters()
	 */
	@Override
	public Map<String, SQLParameter<?>> getNamedParameters() {
		return Collections.unmodifiableMap(namedParameters);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#prepareStatement(java.lang.String)
	 */
	@Override
	public SQLStatement prepareStatement(String sql) {
		ObjectUtils.argumentNotNull(sql, "SQL statement must be not null");

		final StringBuilder sb = new StringBuilder();
		final List<SQLParameterValue<?>> parameters = new ArrayList<>(getNamedParameters().size());

		// check named parameters
		final Map<String, SQLParameter<?>> namedParameters = getNamedParameters();

		final char[] chars = sql.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == ':' && (chars.length - i) >= 7) {
				String namedParameterPlaceholder = String.valueOf(Arrays.copyOfRange(chars, i, i + 7));

				SQLParameter<?> parameter = namedParameters.get(namedParameterPlaceholder);
				if (parameter == null) {
					throw new SQLStatementPreparationException("The named parameter " + namedParameterPlaceholder
							+ " at index " + i + " was not found in SQL composition context");
				}

				// resolve parameter
				SQLParameterValue<?> parameterValue = resolve(parameter, SQLParameterValue.class).orElseThrow(
						() -> new InvalidExpressionException("Failed to resolve parameter [" + parameter + "]"));

				// replace parameter
				sb.append(parameterValue.getSql());
				parameters.add(parameterValue);

				i = i + 6;
				continue;
			}
			sb.append(chars[i]);
		}

		return SQLStatement.create(sb.toString(), parameters.toArray(new SQLParameterValue[parameters.size()]));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#childContext(com.holonplatform.datastore.jdbc.
	 * composer.SQLCompositionContext.AliasMode)
	 */
	@Override
	public SQLCompositionContext childContext() {
		return new DefaultSQLCompositionContext(this, this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLCompositionContext#childQueryContext(com.holonplatform.core.
	 * datastore.relational.RelationalTarget,
	 * com.holonplatform.datastore.jdbc.composer.SQLQueryCompositionContext.AliasMode)
	 */
	@Override
	public SQLQueryCompositionContext childQueryContext(RelationalTarget<?> rootTarget, AliasMode aliasMode) {
		return SQLQueryCompositionContext.create(this, rootTarget, aliasMode, this);
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

	/**
	 * Add a named parameter with given value using a generated parameter name.
	 * @param parameter Parameter definition
	 * @return Generated parameter name
	 */
	protected String generateAndAddParameter(SQLParameter<?> parameter) {
		synchronized (namedParameters) {
			// generate name
			final String name = generateParameterName(namedParameters.size() + 1);
			// add parameter
			namedParameters.put(name, parameter);
			// return the generated name
			return name;
		}
	}

	/**
	 * Generate a named parameter name. By default, the pattern <code>:[001]</code> is used.
	 * @param index Parameter index
	 * @return Parameter name
	 */
	protected String generateParameterName(int index) {
		return ":[" + String.format("%04d", index) + "]";
	}

}
