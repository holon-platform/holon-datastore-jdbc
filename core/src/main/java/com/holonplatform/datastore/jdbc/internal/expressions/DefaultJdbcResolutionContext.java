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
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * Default {@link JdbcResolutionContext} implementation.
 *
 * @since 5.0.0
 */
public class DefaultJdbcResolutionContext extends AbstractJdbcResolutionContext {

	/**
	 * Expression resolver handler
	 */
	private final ExpressionResolverHandler expressionResolverHandler;

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Generated paremeters name last sequence
	 */
	private int parameterSequence = 0;

	/**
	 * Named parameters
	 */
	private final Map<String, ParameterValue> namedParameters = new HashMap<>();

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
	public DefaultJdbcResolutionContext(ExpressionResolverHandler expressionResolverHandler, JdbcDialect dialect,
			AliasMode aliasMode) {
		super(null, 0, aliasMode);
		ObjectUtils.argumentNotNull(expressionResolverHandler, "ExpressionResolverHandler must be not null");
		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		this.expressionResolverHandler = expressionResolverHandler;
		this.dialect = dialect;
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
		return expressionResolverHandler.resolve(expression, resolutionType, context);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.JdbcResolutionContext#getDialect()
	 */
	@Override
	public JdbcDialect getDialect() {
		return dialect;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String addNamedParameter(ParameterValue value) {
		final String name = generateParameterName();
		namedParameters.put(name, value);
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, ParameterValue> getNamedParameters() {
		return Collections.unmodifiableMap(namedParameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replaceParameter(String name, ParameterValue value) {
		ObjectUtils.argumentNotNull(name, "Parameter name must be not null");
		ObjectUtils.argumentNotNull(value, "Parameter value must be not null");
		namedParameters.put(name, value);
	}

	/**
	 * Generate a named parameter name
	 * @return Parameter name using pattern :001
	 */
	protected String generateParameterName() {
		parameterSequence++;
		return ":[" + String.format("%04d", parameterSequence) + "]";
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
		 * holonplatform.datastore.jdbc.internal.support.ParameterValue)
		 */
		@Override
		public String addNamedParameter(ParameterValue value) {
			return parent().addNamedParameter(value);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext#getNamedParameters()
		 */
		@Override
		public Map<String, ParameterValue> getNamedParameters() {
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
		 * @see com.holonplatform.datastore.jdbc.JdbcDialect.DialectResolutionContext#replaceParameter(java.lang.String,
		 * com.holonplatform.datastore.jdbc.internal.support.ParameterValue)
		 */
		@Override
		public void replaceParameter(String name, ParameterValue value) {
			parent().replaceParameter(name, value);
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
