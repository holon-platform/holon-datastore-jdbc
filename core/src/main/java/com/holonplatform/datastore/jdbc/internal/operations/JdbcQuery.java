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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.query.QueryAdapterQuery;
import com.holonplatform.core.internal.query.QueryDefinition;
import com.holonplatform.core.internal.query.lock.LockAcquisitionException;
import com.holonplatform.core.internal.query.lock.LockQueryAdapterQuery;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryAdapter;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.core.query.SelectAllProjection;
import com.holonplatform.core.query.lock.LockQuery;
import com.holonplatform.core.query.lock.LockQueryAdapter;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQuery;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.support.ResultSetSQLResult;

/**
 * JDBC {@link QueryAdapter}.
 *
 * @since 5.0.0
 */
public class JdbcQuery implements LockQueryAdapter<QueryConfiguration> {

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

	// LockQuery Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, LockQuery> LOCK_FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, LockQuery>() {

		@Override
		public Class<? extends LockQuery> getCommodityType() {
			return LockQuery.class;
		}

		@Override
		public LockQuery createCommodity(JdbcDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new LockQueryAdapterQuery<>(new JdbcQuery(context), QueryDefinition.create());
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcQuery(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.QueryAdapter#stream(com.holonplatform.core.query.QueryOperation)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> Stream<R> stream(QueryOperation<QueryConfiguration, R> queryOperation) throws DataAccessException {

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(queryOperation.getConfiguration().getExpressionResolvers());

		// resolve to SQLQuery
		final SQLQuery query = context.resolveOrFail(queryOperation, SQLQuery.class);

		// check converter
		final SQLResultConverter<R> converter = (SQLResultConverter<R>) query.getResultConverter();
		if (!TypeUtils.isAssignable(converter.getConversionType(), queryOperation.getProjection().getType())) {
			throw new DataAccessException("The query results converter type [" + converter.getConversionType()
					+ "] is not compatible with the query projection type [" + queryOperation.getProjection().getType()
					+ "]");
		}

		// trace
		operationContext.trace(query.getSql());

		// execute
		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = operationContext.prepareStatement(query, c)) {
				final SQLExecutionContext ctx = SQLExecutionContext.create(operationContext, c);

				try (ResultSet resultSet = stmt.executeQuery()) {
					final List<R> rows = new ArrayList<>();
					while (resultSet.next()) {
						rows.add(converter.convert(ctx, ResultSetSQLResult.of(resultSet)));
					}
					return rows.stream();
				} catch (SQLException e) {
					// translate SQLException using dialect
					throw operationContext.getDialect().translateException(e);
				}
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.query.lock.LockQueryAdapter#tryLock(com.holonplatform.core.query.QueryConfiguration)
	 */
	@Override
	public boolean tryLock(QueryConfiguration queryConfiguration) {

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(queryConfiguration.getExpressionResolvers());

		final QueryOperation<?, ?> queryOperation = QueryOperation.create(queryConfiguration,
				SelectAllProjection.create());

		// resolve to SQLQuery
		final SQLQuery query = context.resolveOrFail(queryOperation, SQLQuery.class);

		// trace
		operationContext.trace(query.getSql());

		// execute
		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = operationContext.prepareStatement(query, c)) {
				stmt.executeQuery();
			} catch (SQLException e) {
				// translate SQLException using dialect
				DataAccessException dae = operationContext.getDialect().translateException(e);
				// check lock acquistion exception
				if (LockAcquisitionException.class.isAssignableFrom(dae.getClass())) {
					return false;
				}
				throw e;
			}

			return true;

		});
	}

}
