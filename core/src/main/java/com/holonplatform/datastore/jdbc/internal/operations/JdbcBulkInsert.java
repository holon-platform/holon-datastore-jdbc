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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.datastore.operation.commons.InsertOperationConfiguration;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsert;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterPlaceholder;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;

/**
 * JDBC datastore {@link BulkInsert} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkInsert extends AbstractBulkInsert {

	private static final long serialVersionUID = 1L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkInsert> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkInsert>() {

		@Override
		public Class<? extends BulkInsert> getCommodityType() {
			return BulkInsert.class;
		}

		@Override
		public BulkInsert createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcBulkInsert(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcBulkInsert(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// configuration builder
		final InsertOperationConfiguration.Builder configuration = InsertOperationConfiguration.builder()
				.target(getConfiguration().getTarget()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers());

		// operation paths
		final Path<?>[] operationPaths = getConfiguration().getOperationPaths()
				.orElseThrow(() -> new InvalidExpressionException("Missing bulk insert operation paths"));

		Map<Path<?>, TypedExpression<?>> values = new LinkedHashMap<>(operationPaths.length);
		for (Path<?> path : operationPaths) {
			values.put(path, SQLParameterPlaceholder.create(path.getType()));
		}
		configuration.values(values);

		// resolve
		final SQLStatement statement = context.resolveOrFail(configuration.build(), SQLStatement.class);

		// get prepared sql
		final String sql = statement.getSql();

		// trace
		operationContext.trace(sql);

		// execute
		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = c.prepareStatement(sql)) {

				for (Map<Path<?>, ConstantExpression<?>> value : getConfiguration().getValues()) {
					// resolve parameter values
					List<SQLParameter> parameters = new ArrayList<>();
					for (Path<?> path : operationPaths) {
						ConstantExpression<?> pathExpression = value.get(path);
						if (pathExpression != null) {
							parameters.add(SQLParameter.create(pathExpression.getModelValue(),
									pathExpression.getModelType(), pathExpression.getTemporalType().orElse(null)));
						} else {
							parameters.add(SQLParameter.create(null, path.getType()));
						}
					}

					// configure statement
					operationContext.getStatementConfigurator().configureStatement(operationContext, stmt,
							SQLStatement.create(sql, parameters.toArray(new SQLParameter<?>[parameters.size()])));

					// add batch
					stmt.addBatch();
				}

				// execute batch insert
				int[] results = stmt.executeBatch();
				long count = 0;
				if (results != null) {
					for (int result : results) {
						if (result >= 0 || result == Statement.SUCCESS_NO_INFO) {
							count++;
						}
					}
				}

				// return result
				return OperationResult.builder().type(OperationType.INSERT).affectedCount(count).build();
			}
		});
	}

}
