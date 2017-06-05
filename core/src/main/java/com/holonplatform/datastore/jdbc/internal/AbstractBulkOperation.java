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
package com.holonplatform.datastore.jdbc.internal;

import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.ExpressionResolverRegistry;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;

/**
 * Abstract JDBC bulk operation.
 * 
 * @param <C> Concrete operation type
 *
 * @since 5.0.0
 */
public abstract class AbstractBulkOperation<C extends ExpressionResolverBuilder<C>>
		implements ExpressionResolverBuilder<C>, ExpressionResolverHandler {

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JdbcDatastoreLogger.create();

	/**
	 * Expression resolvers
	 */
	private final ExpressionResolverRegistry expressionResolverRegistry = ExpressionResolverRegistry.create();

	/**
	 * Datastore
	 */
	private final JdbcDatastore datastore;

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Whether tracing is enabled
	 */
	private final boolean traceEnabled;

	/**
	 * Data target
	 */
	private final DataTarget<?> target;

	/**
	 * Optional filter
	 */
	private QueryFilter filter;

	/**
	 * Constructor
	 * @param datastore Parent Datastore (not null)
	 * @param target Data target (not null)
	 * @param dialect JDBC dialect (not null)
	 * @param traceEnabled Whether tracing is enabled
	 */
	@SuppressWarnings("unchecked")
	public AbstractBulkOperation(JdbcDatastore datastore, DataTarget<?> target, JdbcDialect dialect,
			boolean traceEnabled) {
		super();
		ObjectUtils.argumentNotNull(datastore, "Datastore must be not null");
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		this.datastore = datastore;
		this.target = target;
		this.dialect = dialect;
		this.traceEnabled = traceEnabled;

		// inherit resolvers
		datastore.getExpressionResolvers().forEach(r -> expressionResolverRegistry.addExpressionResolver(r));
	}

	/**
	 * Get the {@link ExpressionResolverRegistry}
	 * @return the expression resolver registry
	 */
	protected ExpressionResolverRegistry getExpressionResolverRegistry() {
		return expressionResolverRegistry;
	}

	/**
	 * Get the parent JDBC datastore
	 * @return parent JDBC datastore
	 */
	protected JdbcDatastore getDatastore() {
		return datastore;
	}

	/**
	 * Get the JDBC dialect
	 * @return the dialect
	 */
	protected JdbcDialect getDialect() {
		return dialect;
	}

	/**
	 * Get whether tracing is enabled
	 * @return whether tracing is enabled
	 */
	protected boolean isTraceEnabled() {
		return traceEnabled;
	}

	/**
	 * Get the {@link DataTarget}.
	 * @return the data target
	 */
	protected DataTarget<?> getTarget() {
		return target;
	}

	/**
	 * Add a {@link QueryFilter} to current filters.
	 * @param filter The filter to add
	 */
	protected void addFilter(QueryFilter filter) {
		if (filter != null) {
			if (this.filter == null) {
				this.filter = filter;
			} else {
				this.filter = this.filter.and(filter);
			}
		}
	}

	/**
	 * Get the filter
	 * @return Optional filter
	 */
	protected Optional<QueryFilter> getFilter() {
		return Optional.ofNullable(filter);
	}

	/**
	 * Trace given SQL if trace is enabled.
	 * @param sql SQL to trace
	 */
	protected void trace(String sql) {
		if (isTraceEnabled()) {
			LOGGER.info("(TRACE) SQL: [" + sql + "]");
		} else {
			LOGGER.debug(() -> "SQL: [" + sql + "]");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder#withExpressionResolver(com.holonplatform.core
	 * .ExpressionResolver)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Expression, R extends Expression> C withExpressionResolver(
			ExpressionResolver<E, R> expressionResolver) {
		getExpressionResolverRegistry().addExpressionResolver(expressionResolver);
		return (C) this;
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
		return getExpressionResolverRegistry().resolve(expression, resolutionType, context);
	}

}
