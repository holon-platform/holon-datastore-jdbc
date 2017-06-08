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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.query.QueryAdapter;
import com.holonplatform.core.internal.query.QueryStructure;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.core.query.QueryResults.QueryExecutionException;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.support.PreparedSql;

/**
 * JDBC {@link QueryAdapter}.
 *
 * @since 5.0.0
 */
public class JdbcQueryAdapter implements QueryAdapter<QueryConfiguration> {

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JdbcDatastoreLogger.create();

	/**
	 * Parent datastore
	 */
	private final JdbcDatastore datastore;

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Whether query tracing is enabled
	 */
	private final boolean trace;

	/**
	 * Constructor
	 * @param datastore Parent datastore
	 * @param dialect JDBC dialect to use
	 * @param trace Whether tracing is enabled
	 */
	public JdbcQueryAdapter(JdbcDatastore datastore, JdbcDialect dialect, boolean trace) {
		super();
		ObjectUtils.argumentNotNull(datastore, "Datastore must be not null");
		this.datastore = datastore;
		this.dialect = dialect;
		this.trace = trace;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryAdapter#stream(com.holonplatform.core.query.QueryConfiguration,
	 * com.holonplatform.core.query.QueryProjection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> Stream<R> stream(QueryConfiguration configuration, QueryProjection<R> projection)
			throws QueryExecutionException {

		// context
		final JdbcResolutionContext context = JdbcResolutionContext.create(configuration, dialect, AliasMode.AUTO);

		final JdbcQueryComposition<R> query;
		final PreparedSql preparedSql;
		try {

			// resolve query
			query = context
					.resolve(QueryStructure.create(configuration, projection), JdbcQueryComposition.class, context)
					.orElseThrow(() -> new QueryBuildException("Failed to resolve query"));

			query.validate();

			// prepare SQL
			preparedSql = JdbcDatastoreUtils.prepareSql(query.serialize(), context);

			// trace
			if (trace) {
				LOGGER.info("(TRACE) SQL: [" + preparedSql.getSql() + "]");
			} else {
				LOGGER.debug(() -> "SQL: [" + preparedSql.getSql() + "]");
			}

		} catch (Exception e) {
			throw new QueryExecutionException("Failed to build query", e);
		}

		// execute
		return datastore.withConnection(c -> {

			try (PreparedStatement statement = preparedSql.createStatement(c, context.getDialect())) {
				// convert results
				try (ResultSet resultSet = statement.executeQuery()) {
					List<R> rows = new ArrayList<>();
					while (resultSet.next()) {
						rows.add(query.getProjection().getConverter().convert(resultSet));
					}
					return rows.stream();
				}

			}
		});
	}

}
