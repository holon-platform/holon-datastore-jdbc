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
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.operation.InsertOperation;
import com.holonplatform.core.datastore.operation.InsertOperationConfiguration;
import com.holonplatform.core.internal.datastore.operation.AbstractInsertOperation;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.support.DialectPathMatcher;

/**
 * JDBC {@link InsertOperation}.
 *
 * @since 5.1.0
 */
public class JdbcInsert extends AbstractInsertOperation {

	private static final long serialVersionUID = -3547948214277724242L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, InsertOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, InsertOperation>() {

		@Override
		public Class<? extends InsertOperation> getCommodityType() {
			return InsertOperation.class;
		}

		@Override
		public InsertOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcInsert(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcInsert(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// create operation configuration
		final InsertOperationConfiguration configuration = InsertOperationConfiguration.builder()
				.target(getConfiguration().getTarget()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers())
				.values(getConfiguration().getValueExpressions(false)).build();

		// resolve
		final SQLStatement statement = context.resolveOrFail(configuration, SQLStatement.class);

		// trace
		operationContext.trace(statement.getSql());

		// resolve primary key
		final Optional<SQLPrimaryKey> primaryKey = context.resolve(getConfiguration(), SQLPrimaryKey.class, context);

		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = operationContext.prepareInsertStatement(statement, c,
					primaryKey.orElse(null))) {

				// execute
				int count = stmt.executeUpdate();

				OperationResult.Builder result = OperationResult.builder().type(OperationType.INSERT)
						.affectedCount(count);

				// generated keys
				if (primaryKey.isPresent()) {
					final Path<?>[] keyPaths = primaryKey.get().getPaths();

					final boolean isBringBackGeneratedIds = getConfiguration()
							.hasWriteOption(DefaultWriteOption.BRING_BACK_GENERATED_IDS);

					final PathPropertyBoxAdapter adapter = isBringBackGeneratedIds
							? PathPropertyBoxAdapter.builder(getConfiguration().getValue())
									.pathMatcher(new DialectPathMatcher(operationContext.getDialect())).build()
							: null;

					// read generated keys
					try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							// some generated key returned
							final SQLExecutionContext ctx = SQLExecutionContext.create(operationContext, c);
							for (int i = 0; i < generatedKeys.getMetaData().getColumnCount(); i++) {
								final Object keyValue = generatedKeys.getObject(i + 1);
								if (i < keyPaths.length) {
									// set in operation result
									result.withInsertedKey(keyPaths[i], keyValue);
									if (isBringBackGeneratedIds) {
										// set back in operation value
										setBackGeneratedKey(ctx, adapter, keyPaths[i], keyValue);
									}
								}
							}
						}
					}
				}

				return result.build();
			}

		});

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setBackGeneratedKey(SQLExecutionContext context, PathPropertyBoxAdapter adapter, Path keyPath,
			Object keyValue) throws SQLException {
		if (keyValue != null) {
			Optional<Property> property = adapter.getProperty(keyPath);
			if (property.isPresent()) {
				final TypedExpression valueExpression = (property.get() instanceof TypedExpression)
						? (TypedExpression) property.get() : ConstantExpression.create(keyValue);
				adapter.setValue(keyPath,
						operationContext.getValueDeserializer().deserialize(context, valueExpression, keyValue));
			}
		}
	}

}
