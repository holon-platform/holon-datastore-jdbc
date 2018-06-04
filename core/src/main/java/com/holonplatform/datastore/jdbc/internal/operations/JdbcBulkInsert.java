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
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsert;
import com.holonplatform.core.internal.datastore.operation.common.InsertOperationConfiguration;
import com.holonplatform.core.property.PathPropertySetAdapter;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
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

		// property set
		final PropertySet<?> propertySet = getConfiguration().getPropertySet()
				.orElseThrow(() -> new InvalidExpressionException("Missing bulk insert operation property set"));

		final PathPropertySetAdapter adapter = PathPropertySetAdapter.create(propertySet);

		final List<Property<?>> properties = new ArrayList<>(propertySet.size());
		final Map<Path<?>, TypedExpression<?>> values = new LinkedHashMap<>(propertySet.size());
		adapter.propertyPaths().forEach(propertyPath -> {
			properties.add(propertyPath.getProperty());
			values.put(propertyPath.getPath(), SQLParameterPlaceholder.create(propertyPath.getProperty().getType()));
		});
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

				for (PropertyBox value : getConfiguration().getValues()) {
					// resolve parameter values
					List<SQLParameter> parameters = new ArrayList<>();
					for (Property<?> p : properties) {
						@SuppressWarnings("unchecked")
						Property<Object> property = (Property<Object>) p;
						if (value.containsValue(property)) {
							parameters.add(SQLParameter.create(property.getModelValue(value.getValue(property)),
									property.getModelType(), property.getTemporalType().orElse(null)));
						} else {
							parameters.add(SQLParameter.create(null, property.getType()));
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
