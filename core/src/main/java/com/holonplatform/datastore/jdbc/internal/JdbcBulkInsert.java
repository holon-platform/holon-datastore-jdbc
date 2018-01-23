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
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkInsertOperation;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.context.PreparedSql;
import com.holonplatform.datastore.jdbc.internal.context.SQLStatementConfigurator;
import com.holonplatform.datastore.jdbc.internal.context.StatementConfigurationException;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;

/**
 * JDBC datastore {@link BulkInsert} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkInsert extends AbstractBulkInsertOperation<BulkInsert> implements BulkInsert {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	private final JdbcStatementExecutionContext executionContext;

	private PropertyBox singleValue;

	private final PathMatcher generatedKeysPathMatcher;

	public JdbcBulkInsert(JdbcStatementExecutionContext executionContext) {
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

		final JdbcResolutionContext context = JdbcResolutionContext.create(executionContext, AliasMode.UNSUPPORTED);

		// add operation specific resolvers
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		final String sql = context.resolveExpression(getConfiguration(), SQLToken.class).getValue();

		// prepare SQL
		final PreparedSql preparedSql = executionContext.prepareSql(sql, context);
		executionContext.trace(preparedSql.getSql());

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

				try (PreparedStatement stmt = createInsertStatement(c, executionContext, preparedSql, pkNames)) {

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
												adapter.setValue(keyPath,
														executionContext.getDialect().getValueDeserializer()
																.deserializeValue(c, valueExpression, keyValue));
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

				try (PreparedStatement stmt = c.prepareStatement(sql)) {

					final SQLStatementConfigurator<PreparedStatement> configurator = executionContext
							.getStatementConfigurator();

					for (Map<Path<?>, TypedExpression<?>> value : getConfiguration().getValues()) {
						// resolve parameter values
						List<SQLParameterDefinition> parameterValues = new ArrayList<>();

						for (Path<?> path : operationPaths) {
							TypedExpression<?> pathExpression = value.get(path);
							if (pathExpression != null) {
								// TODO
								parameterValues.add(SQLParameterDefinition.create(
										((ConstantExpression) pathExpression).getModelValue(),
										((ConstantExpression) pathExpression).getModelType(),
										pathExpression.getTemporalType().orElse(null)));
							} else {
								parameterValues.add(SQLParameterDefinition.ofNull(path.getType()));
							}
						}

						configurator.configureStatement(stmt, sql, parameterValues);
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

	private static Optional<Path<?>[]> getPrimaryKeys(JdbcResolutionContext context, DataTarget<?> target) {
		return context.getDialect().supportsGetGeneratedKeys()
				? context.resolve(target, TablePrimaryKey.class, context).map(k -> k.getKeys()) : Optional.empty();
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
	private static PreparedStatement createInsertStatement(Connection connection, JdbcStatementExecutionContext context,
			PreparedSql preparedSql, String[] pkNames) throws SQLException {
		PreparedStatement stmt;
		if (context.getDialect().supportsGetGeneratedKeys()) {
			if (context.getDialect().supportGetGeneratedKeyByName() && pkNames != null && pkNames.length > 0) {
				stmt = connection.prepareStatement(preparedSql.getSql(), pkNames);
			} else {
				stmt = connection.prepareStatement(preparedSql.getSql(), Statement.RETURN_GENERATED_KEYS);
			}
		} else {
			stmt = connection.prepareStatement(preparedSql.getSql());
		}

		// configure parameters
		try {
			context.getStatementConfigurator().configureStatement(stmt, preparedSql.getSql(),
					preparedSql.getParameters());
		} catch (StatementConfigurationException e) {
			throw new SQLException(e);
		}

		return stmt;
	}

}
