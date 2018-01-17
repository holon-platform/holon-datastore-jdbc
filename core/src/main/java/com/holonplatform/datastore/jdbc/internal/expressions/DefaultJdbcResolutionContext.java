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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;

/**
 * Default {@link JdbcResolutionContext} implementation.
 *
 * @since 5.0.0
 */
public class DefaultJdbcResolutionContext extends AbstractJdbcResolutionContext {

	/*
	 * Expression resolvers
	 */
	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Named parameters
	 */
	private final Map<String, SQLParameterDefinition> namedParameters = new HashMap<>();

	/**
	 * Atomic context sequence provider
	 */
	private final AtomicInteger sequenceProvider = new AtomicInteger(0);

	/**
	 * Constructor
	 * @param expressionResolverHandler Expression resolver handler (not null)
	 * @param dialect Dialect (not null)
	 * @param aliasMode Alias mode
	 */
	public DefaultJdbcResolutionContext(JdbcStatementExecutionContext context, AliasMode aliasMode) {
		super(null, 0, aliasMode);
		ObjectUtils.argumentNotNull(context, "Context must be not null");
		this.dialect = context.getDialect();

		// inherit resolvers
		addExpressionResolvers(context.getExpressionResolvers());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#childContext(com.holonplatform.
	 * datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode)
	 */
	@Override
	public JdbcResolutionContext childContext(AliasMode aliasMode) {
		int nextSequence = sequenceProvider.incrementAndGet();
		return new SubContext(this, nextSequence, sequenceProvider, aliasMode);
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

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.JdbcResolutionContext#getDialect()
	 */
	@Override
	public JdbcDialect getDialect() {
		return dialect;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#addNamedParameter(com.holonplatform.
	 * datastore.jdbc.expressions.SQLParameterDefinition)
	 */
	@Override
	public String addNamedParameter(SQLParameterDefinition parameter) {
		ObjectUtils.argumentNotNull(parameter, "Parameter must be not null");
		// dialect parameter processor
		SQLParameterDefinition processed = getDialect().getParameterProcessor().processParameter(parameter);
		// generate parameter name
		final String name = generateAndAddParameter(processed);
		// check serializer function
		return processed.getParameterSerializer().map(serializer -> serializer.apply(name)).orElse(name);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getNamedParameters()
	 */
	@Override
	public Map<String, SQLParameterDefinition> getNamedParameters() {
		return Collections.unmodifiableMap(namedParameters);
	}

	/**
	 * Add a named parameter with given value using a generated parameter name.
	 * @param parameter Parameter definition
	 * @return Generated parameter name
	 */
	protected String generateAndAddParameter(SQLParameterDefinition parameter) {
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

	/**
	 * Sub resolution context representation.
	 */
	class SubContext extends AbstractJdbcResolutionContext {

		/**
		 * Sequence provider of the parent context
		 */
		private final AtomicInteger parentSequenceProvider;

		/**
		 * Constructor
		 * @param parent Parent context (not null)
		 * @param sequence Context sequence
		 * @param sequenceProvider Sequence provider
		 * @param aliasMode Alias mode
		 */
		public SubContext(JdbcResolutionContext parent, int sequence, AtomicInteger sequenceProvider,
				AliasMode aliasMode) {
			super(parent, sequence, aliasMode);
			ObjectUtils.argumentNotNull(parent, "Parent context must be not null");
			this.parentSequenceProvider = sequenceProvider;
		}

		/**
		 * Get the parent context
		 * @return The parent context
		 * @throws IllegalStateException If the parent context is not available
		 */
		protected JdbcResolutionContext parent() {
			return getParent().orElseThrow(() -> new IllegalStateException("Missing parent context"));
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getDialect()
		 */
		@Override
		public JdbcDialect getDialect() {
			return parent().getDialect();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#addNamedParameter(com.
		 * holonplatform.datastore.jdbc.expressions.SQLParameterDefinition)
		 */
		@Override
		public String addNamedParameter(SQLParameterDefinition parameter) {
			return parent().addNamedParameter(parameter);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getNamedParameters()
		 */
		@Override
		public Map<String, SQLParameterDefinition> getNamedParameters() {
			return parent().getNamedParameters();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#resolve(com.holonplatform.core.
		 * Expression, java.lang.Class, com.holonplatform.core.ExpressionResolver.ResolutionContext)
		 */
		@Override
		public <E extends Expression, R extends Expression> Optional<R> resolve(E expression, Class<R> resolutionType,
				ResolutionContext context) throws InvalidExpressionException {
			return parent().resolve(expression, resolutionType, context);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.holonplatform.
		 * core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void addExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			parent().addExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.
		 * holonplatform.core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void removeExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			parent().removeExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler#getExpressionResolvers()
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public Iterable<ExpressionResolver> getExpressionResolvers() {
			return parent().getExpressionResolvers();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#childContext(com.holonplatform.
		 * datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode)
		 */
		@Override
		public JdbcResolutionContext childContext(AliasMode aliasMode) {
			int nextSequence = parentSequenceProvider.incrementAndGet();
			return new SubContext(this, nextSequence, parentSequenceProvider, aliasMode);
		}
	}

}
