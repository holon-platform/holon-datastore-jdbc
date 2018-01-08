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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurator;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * JDBC datastore {@link BulkInsert} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkInsert extends AbstractBulkOperation<BulkInsert> implements BulkInsert {

	/**
	 * Property set to use
	 */
	private final PropertySet<?> propertySet;

	/**
	 * Values to insert
	 */
	private final List<PropertyBox> values = new ArrayList<>();

	/**
	 * Constructor
	 * @param datastore Parent Datastore (not null)
	 * @param target Data target (not null)
	 * @param dialect JDBC dialect (not null)
	 * @param traceEnabled Whether tracing is enabled
	 * @param propertySet Property set to use (not null)
	 */
	public JdbcBulkInsert(JdbcDatastore datastore, DataTarget<?> target, JdbcDialect dialect, boolean traceEnabled,
			PropertySet<?> propertySet) {
		super(datastore, target, dialect, traceEnabled);

		ObjectUtils.argumentNotNull(propertySet, "PropertySet must be not null");
		this.propertySet = propertySet;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.BulkInsert#add(com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public BulkInsert add(PropertyBox propertyBox) {
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox to insert must be not null");
		values.add(propertyBox);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OperationResult execute() {
		if (values.isEmpty()) {
			throw new DataAccessException("No values to insert");
		}

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(), AliasMode.UNSUPPORTED);

		final List<Property> pathProperties = propertySet.stream()
				.filter(property -> Path.class.isAssignableFrom(property.getClass())).collect(Collectors.toList());

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.INSERT, getTarget());
			// valid Paths with not null value
			pathProperties.forEach(p -> {
				builder.withValue((Path<?>) p, ConstantExpression.create("?"));
			});

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure insert operation", e);
		}

		trace(sql);

		return getDatastore().withConnection(c -> {

			try (PreparedStatement stmt = c.prepareStatement(sql)) {

				final StatementConfigurator configurator = context.getDialect().getStatementConfigurator();

				for (PropertyBox value : values) {
					// resolve parameter values
					List<ParameterValue> parameterValues = new ArrayList<>();
					pathProperties.forEach(p -> {
						parameterValues.add(getParameterValue(p, value.getValue(p)));
					});
					configurator.configureStatement(c, stmt, sql, parameterValues);
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
				return OperationResult.builder().type(OperationType.INSERT).affectedCount(count).build();
			}
		});
	}

	/**
	 * Get the {@link ParameterValue} which represents the value of given property, converting the value to the model
	 * value type is a {@link PropertyValueConverter} is bound to given property.
	 * @param property Property
	 * @param value Property value
	 * @return Parameter value definition
	 */
	private static ParameterValue getParameterValue(Property<Object> property, Object value) {
		return property.getConverter()
				.map(c -> ParameterValue.create(c.getModelType(), c.toModel(value, property),
						property.getConfiguration().getTemporalType().orElse(null)))
				.orElse(ParameterValue.create(property.getType(), value,
						property.getConfiguration().getTemporalType().orElse(null)));
	}

}
