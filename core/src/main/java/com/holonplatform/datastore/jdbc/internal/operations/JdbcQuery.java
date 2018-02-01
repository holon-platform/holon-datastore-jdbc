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
package com.holonplatform.datastore.jdbc.internal.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.holonplatform.core.Provider;
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
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQuery;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcExecutionContext;

/**
 * JDBC {@link QueryAdapter}.
 *
 * @since 5.0.0
 */
public class JdbcQuery implements QueryAdapter<QueryConfiguration> {

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Query> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Query>() {

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
	private final JdbcExecutionContext executionContext;

	/**
	 * Constructor
	 * @param executionContext Execution context
	 */
	public JdbcQuery(JdbcExecutionContext executionContext) {
		super();
		ObjectUtils.argumentNotNull(executionContext, "Execution context must be not null");
		this.executionContext = executionContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryAdapter#stream(com.holonplatform.core.query.QueryOperation)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> Stream<R> stream(QueryOperation<QueryConfiguration, R> queryOperation) throws QueryExecutionException {

		/// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(executionContext);
		context.addExpressionResolvers(queryOperation.getConfiguration().getExpressionResolvers());

		// resolve to SQLQuery
		final SQLQuery<R> query = context.resolveOrFail(queryOperation, SQLQuery.class);
		
		// trace
		executionContext.trace(query.getSql());

		// execute
		return executionContext.withConnection(c -> {

			try (PreparedStatement stmt = executionContext.prepareStatement(query, c)) {
				final SQLResultConverter<R> converter = query.getResultConverter();
				final Provider<Connection> connectionProvider = Provider.create(c);

				try (ResultSet resultSet = stmt.executeQuery()) {
					List<R> rows = new ArrayList<>();
					while (resultSet.next()) {
						rows.add(converter.convert(context, connectionProvider, ResultSetSQLResult.of(resultSet)));
					}
					return rows.stream();
				}
			}

		});

	}

}
