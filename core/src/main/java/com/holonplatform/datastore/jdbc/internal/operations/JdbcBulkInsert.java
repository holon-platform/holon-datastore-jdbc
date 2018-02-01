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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.Provider;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsertOperation;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcExecutionContext;
import com.holonplatform.datastore.jdbc.internal.DialectPathMatcher;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;

/**
 * JDBC datastore {@link BulkInsert} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkInsert extends AbstractBulkInsertOperation<BulkInsert> implements BulkInsert {

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

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	private final JdbcExecutionContext executionContext;

	private PropertyBox singleValue;

	private final PathMatcher generatedKeysPathMatcher;

	public JdbcBulkInsert(JdbcExecutionContext executionContext) {
		super();
		this.executionContext = executionContext;
		this.generatedKeysPathMatcher = new DialectPathMatcher(executionContext.getDialect());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.BulkInsert#singleValue(com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public BulkInsert singleValue(PropertyBox propertyBox) {
		this.singleValue = propertyBox;
		return add(propertyBox);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsertOperation#getActualOperation()
	 */
	@Override
	protected BulkInsert getActualOperation() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OperationResult execute() {

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(executionContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// resolve
		final SQLStatement statement = context.resolveOrFail(getConfiguration(), SQLStatement.class);

		// trace
		executionContext.trace(statement.getSql());

		// check multi value
		if (getConfiguration().getValues().size() == 1) {

			// get primary key for generated keys parsing
			Optional<Path<?>[]> pk = getPrimaryKeys(context, getConfiguration().getTarget());
			// pk names
			final Path<?>[] keys;
			final String[] pkNames;

			if (pk.isPresent() && pk.get().length > 0) {
				keys = pk.get();
				pkNames = new String[keys.length];
				for (int i = 0; i < keys.length; i++) {
					pkNames[i] = executionContext.getDialect().getColumnName(keys[i].getName());
				}
			} else {
				keys = null;
				pkNames = null;
			}

			final boolean isBringBackGeneratedIds = (singleValue != null)
					&& getConfiguration().hasWriteOption(DefaultWriteOption.BRING_BACK_GENERATED_IDS);

			return executionContext.withConnection(c -> {

				try (PreparedStatement stmt = createInsertStatement(c, executionContext, statement, pkNames)) {

					// execute
					int count = stmt.executeUpdate();

					OperationResult.Builder result = OperationResult.builder().type(OperationType.INSERT)
							.affectedCount(count);

					// generated keys
					if (keys != null) {

						final PathPropertyBoxAdapter adapter = isBringBackGeneratedIds ? PathPropertyBoxAdapter
								.builder(singleValue).pathMatcher(generatedKeysPathMatcher).build() : null;

						// get generated keys
						try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
							if (generatedKeys.next()) {

								final Provider<Connection> connectionProvider = Provider.create(c);

								final int columns = generatedKeys.getMetaData().getColumnCount();
								for (int i = 0; i < keys.length; i++) {
									if (i < columns) {
										final Object keyValue = generatedKeys.getObject(i + 1);
										final Path keyPath = keys[i];
										result.withInsertedKey(keyPath, keyValue);
										if (adapter != null && keyValue != null) {
											// set in propertybox
											adapter.getProperty(keyPath).ifPresent(property -> {
												TypedExpression valueExpression = (property instanceof QueryExpression)
														? (QueryExpression) property
														: ConstantExpression.create(keyValue);
												try {
													adapter.setValue(keyPath,
															executionContext.getValueDeserializer().deserialize(
																	connectionProvider, valueExpression, keyValue));
												} catch (SQLException e) {
													// TODO
													throw new RuntimeException(e);
												}
											});

										}
									}
								}
							}
						} catch (SQLException e) {
							LOGGER.warn("Failed to retrieve generated keys", e);
						}
					}

					return result.build();
				}

			});
		} else {

			final Path<?>[] operationPaths = getConfiguration().getOperationPaths()
					.orElseThrow(() -> new InvalidExpressionException("Missing operation value paths"));

			return executionContext.withConnection(c -> {

				final String sql = statement.getSql();

				try (PreparedStatement stmt = c.prepareStatement(sql)) {

					for (Map<Path<?>, TypedExpression<?>> value : getConfiguration().getValues()) {
						// resolve parameter values
						List<SQLParameter> parameters = new ArrayList<>();

						for (Path<?> path : operationPaths) {
							TypedExpression<?> pathExpression = value.get(path);
							if (pathExpression != null) {
								// TODO
								parameters
										.add(SQLParameter.create(((ConstantExpression) pathExpression).getModelValue(),
												((ConstantExpression) pathExpression).getModelType(),
												pathExpression.getTemporalType().orElse(null)));
							} else {
								parameters.add(SQLParameter.create(null, Void.class));
							}
						}

						executionContext.getStatementConfigurator().configureStatement(executionContext, stmt,
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
					return OperationResult.builder().type(OperationType.INSERT).affectedCount(count).build();
				}
			});

		}
	}

	private static Optional<Path<?>[]> getPrimaryKeys(SQLCompositionContext context, DataTarget<?> target) {
		return context.getDialect().supportsGetGeneratedKeys()
				? context.resolve(target, SQLPrimaryKey.class, context).map(k -> k.getPaths()) : Optional.empty();
	}

	/**
	 * Create a {@link PreparedStatement} for an INSERT operation configuring generated keys.
	 * @param connection Connection
	 * @param dialect Dialect
	 * @param sql SQL statement
	 * @param pkNames Optional primary key column names
	 * @return Configured statement
	 * @throws SQLException If an error occurred
	 */
	@SuppressWarnings("resource")
	private static PreparedStatement createInsertStatement(Connection connection, JdbcExecutionContext context,
			SQLStatement statement, String[] pkNames) throws SQLException {
		PreparedStatement stmt;
		if (context.getDialect().supportsGetGeneratedKeys()) {
			if (context.getDialect().supportGetGeneratedKeyByName() && pkNames != null && pkNames.length > 0) {
				stmt = connection.prepareStatement(statement.getSql(), pkNames);
			} else {
				stmt = connection.prepareStatement(statement.getSql(), Statement.RETURN_GENERATED_KEYS);
			}
		} else {
			stmt = connection.prepareStatement(statement.getSql());
		}

		// configure parameters
		context.getStatementConfigurator().configureStatement(context, stmt, statement);

		return stmt;
	}

}
