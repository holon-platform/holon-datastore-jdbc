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
package com.holonplatform.datastore.jdbc.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.internal.query.QueryAdapterQuery;
import com.holonplatform.core.internal.query.QueryDefinition;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryAdapter;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.core.query.QueryResults.QueryExecutionException;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.context.PreparedSql;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcQueryComposition;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;

/**
 * JDBC {@link QueryAdapter}.
 *
 * @since 5.0.0
 */
public class JdbcQuery implements QueryAdapter<QueryConfiguration> {

	// Commodity factory
	@SuppressWarnings("serial")
	static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Query> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Query>() {

		@Override
		public Class<? extends Query> getCommodityType() {
			return Query.class;
		}

		@Override
		public Query createCommodity(JdbcDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new QueryAdapterQuery<>(new JdbcQuery(context), QueryDefinition.create());
		}
	};

	/**
	 * Execution context
	 */
	private final JdbcStatementExecutionContext executionContext;

	/**
	 * Constructor
	 * @param executionContext Execution context
	 */
	public JdbcQuery(JdbcStatementExecutionContext executionContext) {
		super();
		ObjectUtils.argumentNotNull(executionContext, "Execution context must be not null");
		this.executionContext = executionContext;
	}

	/**
	 * Get the execution context.
	 * @return the execution context
	 */
	protected JdbcStatementExecutionContext getExecutionContext() {
		return executionContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryAdapter#stream(com.holonplatform.core.query.QueryOperation)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> Stream<R> stream(QueryOperation<QueryConfiguration, R> queryOperation) throws QueryExecutionException {

		// context
		final JdbcResolutionContext context = JdbcResolutionContext.create(getExecutionContext(), AliasMode.AUTO);

		// add query specific resolvers
		context.addExpressionResolvers(queryOperation.getConfiguration().getExpressionResolvers());

		final JdbcQueryComposition<R> query;
		final PreparedSql preparedSql;
		try {

			// resolve query
			query = context.resolve(queryOperation, JdbcQueryComposition.class, context)
					.orElseThrow(() -> new QueryExecutionException("Failed to resolve query"));

			query.validate();

			// prepare SQL
			preparedSql = getExecutionContext().prepareSql(query.serialize(), context);
			getExecutionContext().trace(preparedSql.getSql());

		} catch (Exception e) {
			throw new QueryExecutionException("Failed to build query", e);
		}

		// execute
		return getExecutionContext().withConnection(c -> {

			try (PreparedStatement statement = getExecutionContext().createStatement(c, preparedSql)) {
				// convert results
				try (ResultSet resultSet = statement.executeQuery()) {
					List<R> rows = new ArrayList<>();
					while (resultSet.next()) {
						rows.add(query.getProjection().getConverter().convert(c, resultSet));
					}
					return rows.stream();
				}

			}
		});
	}

}
