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
package com.holonplatform.datastore.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.expressions.SQLFunction;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultLimitHandler;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultSQLValueDeserializer;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultStatementConfigurator;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * Represents a dialect of SQL implemented by a particular database.
 *
 * @since 5.0.0
 */
public interface JdbcDialect extends Serializable {

	/**
	 * Dialect initialization hook at parent datastore initialization.
	 * @param datastore Parent datastore
	 * @throws SQLException An error occurred during dialect initialization
	 */
	void init(JdbcDatastore datastore) throws SQLException;

	/**
	 * Resolve given <code>function</code> into a dialect-specific {@link SQLFunction}.
	 * @param function The function to resolve (not null)
	 * @return A dialect-specific function resolution, or empty to fallback to the default function resolution, if
	 *         available
	 */
	default Optional<SQLFunction> resolveFunction(QueryFunction<?> function) {
		return Optional.empty();
	}

	/**
	 * Get whether to use the <code>OUTER</code> keyword in join serialization.
	 * @return <code>true</code> to use the <code>OUTER</code> keyword in join serialization
	 */
	default boolean useOuterInJoins() {
		return false;
	}

	/**
	 * Get whether alias is supported in the DELETE clause.
	 * @return <code>true</code> if alias is supported in the DELETE clause
	 */
	default boolean deleteStatementAliasSupported() {
		return true;
	}

	/**
	 * Get whether the DELETE clause target must be specified.
	 * @return <code>true</code> if the DELETE clause target must be specified
	 */
	default boolean deleteStatementTargetRequired() {
		return false;
	}

	/**
	 * Get the actual table name for given table name (for example, applying case transformations)
	 * @param tableName Table name
	 * @return The actual table name
	 */
	default String getTableName(String tableName) {
		return tableName;
	}

	/**
	 * Get the actual column name for given column name (for example, applying case transformations)
	 * @param columnName Column name
	 * @return The actual column name
	 */
	default String getColumnName(String columnName) {
		return columnName;
	}

	/**
	 * Get the {@link SQLValueDeserializer} to use to deserialize SQL query results.
	 * @return The SQLValueDeserializer (not null)
	 */
	default SQLValueDeserializer getValueDeserializer() {
		return SQLValueDeserializer.getDefault();
	}

	/**
	 * Get the {@link StatementConfigurator} to use to configure JDBC statements.
	 * @return The StatementConfigurator (not null)
	 */
	StatementConfigurator getStatementConfigurator();

	/**
	 * Get the dialect {@link SQLPathProcessor}.
	 * @return Optional SQLPathProcessor
	 */
	default Optional<SQLPathProcessor> getPathProcessor() {
		return Optional.empty();
	}

	/**
	 * Get the dialect {@link SQLParameterProcessor}.
	 * @return Optional SQLParameterProcessor
	 */
	default Optional<SQLParameterProcessor> getParameterProcessor() {
		return Optional.empty();
	}

	/**
	 * Get the dialect {@link StatementParameterHandler}.
	 * @return Optional StatementParameterHandler
	 */
	default Optional<StatementParameterHandler> getStatementParameterHandler() {
		return Optional.empty();
	}

	/**
	 * Retrieves whether this database supports specifying a <code>LIKE</code> escape clause.
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 */
	boolean supportsLikeEscapeClause();

	/**
	 * Retrieves whether auto-generated keys can be retrieved after a statement has been executed.
	 * @return <code>true</code> if auto-generated keys can be retrieved after a statement has executed;
	 *         <code>false</code> otherwise
	 */
	boolean supportsGetGeneratedKeys();

	/**
	 * Retrieves whether a generated key will always be returned if the column name(s) or index(es) specified for the
	 * auto generated key column(s) are valid and the statement succeeds. The key that is returned may or may not be
	 * based on the column(s) for the auto generated key.
	 * @return <code>true</code> if so; <code>false</code> otherwise
	 */
	boolean generatedKeyAlwaysReturned();

	/**
	 * Retrieves whether auto-generated keys can be retrieved by name after a statement has been executed.
	 * @return <code>true</code> if auto-generated keys can be retrieved by name after a statement has executed
	 */
	default boolean supportGetGeneratedKeyByName() {
		return true;
	}

	/**
	 * Get the <em>primary key</em> paths for given <code>tableName</code>, if available.
	 * @param tableName Table name
	 * @param connection Connection to use
	 * @return Table primary key paths, or empty if not available
	 * @throws SQLException Error during primary key retrieving
	 */
	default Optional<Path<?>[]> getPrimaryKey(String tableName, Connection connection) throws SQLException {
		return JdbcDatastoreUtils.getPrimaryKey(this, tableName, connection);
	}

	/**
	 * Get the {@link LimitHandler}
	 * @return Optional limit handler
	 */
	default Optional<LimitHandler> getLimitHandler() {
		return Optional.of(DefaultLimitHandler.INSTANCE);
	}

	/**
	 * SQL query values deserializer
	 */
	@FunctionalInterface
	public interface SQLValueDeserializer extends Serializable {

		/**
		 * Deserialize the <code>value</code> associated to given <code>expression</code>, obtained as the result of a
		 * SQL query.
		 * @param <T> Expression type
		 * @param connection Current JDBC connection (the connection is managed by the Datastore and must not be closed)
		 * @param expression Query expression
		 * @param value Value to deserialize
		 * @return Deserialized value
		 */
		<T> T deserializeValue(Connection connection, QueryExpression<T> expression, Object value);

		/**
		 * Get the default SQLValueDeserializer.
		 * @return Default SQLValueDeserializer
		 */
		static SQLValueDeserializer getDefault() {
			return DefaultSQLValueDeserializer.INSTANCE;
		}

	}

	/**
	 * JDBC {@link PreparedStatement} configurator.
	 */
	@FunctionalInterface
	public interface StatementConfigurator {

		/**
		 * Configure given <code>statement</code>, setting any parameter value.
		 * @param connection Connection
		 * @param statement Statement to configure
		 * @param sql The precompiled SQL statement
		 * @param parameterValues Parameter values to set (if any), in the right sequence
		 * @throws StatementConfigurationException If an error occurred
		 */
		void configureStatement(Connection connection, PreparedStatement statement, String sql,
				List<ParameterValue> parameterValues) throws StatementConfigurationException;

		/**
		 * Create a new {@link StatementConfigurator} using given dialect.
		 * @param dialect JDBC dialect
		 * @return A new StatementConfigurator instance
		 */
		static StatementConfigurator create(JdbcDialect dialect) {
			return new DefaultStatementConfigurator(dialect);
		}

	}

	/**
	 * Handler to manage statement parameters setting.
	 */
	@FunctionalInterface
	public interface StatementParameterHandler {

		/**
		 * Set a statement parameter value.
		 * @param connection Connection
		 * @param statement Statement
		 * @param index Parameter index
		 * @param parameterValue Parameter value
		 * @return The setted parameter value if handled, an empty optional to fallback to standard behaviour
		 * @throws SQLException If an error occurred
		 */
		Optional<Object> setParameterValue(Connection connection, PreparedStatement statement, int index,
				ParameterValue parameterValue) throws SQLException;

	}

	/**
	 * Path processor for serialized path manipulation.
	 */
	@FunctionalInterface
	public interface SQLPathProcessor {

		/**
		 * Process given serialized path.
		 * @param serialized Serialized path
		 * @param path Path
		 * @param clause Resolution clause
		 * @return Processed path
		 */
		String processPath(String serialized, Path<?> path, ResolutionQueryClause clause);

	}

	/**
	 * Dialect resolution context.
	 */
	public interface DialectResolutionContext {

		/**
		 * Get the query clause to resolve with this context, if available.
		 * @return Optional resolution query clause
		 */
		Optional<ResolutionQueryClause> getResolutionQueryClause();

		/**
		 * Replace a named parameter definition.
		 * @param name Parameter name
		 * @param value Parameter definition
		 */
		void replaceParameter(String name, ParameterValue value);

	}

	/**
	 * Parameter processor for serialized parameters manipulation.
	 */
	@FunctionalInterface
	public interface SQLParameterProcessor {

		/**
		 * Process given serialized parameter.
		 * @param serialized Serialized parameter
		 * @param parameter Parameter value definition
		 * @param context Resolution context
		 * @return Processed parameter
		 */
		String processParameter(String serialized, ParameterValue parameter, DialectResolutionContext context);

	}

	/**
	 * Handler to apply limit/offset to SQL query.
	 */
	@FunctionalInterface
	public interface LimitHandler extends Serializable {

		/**
		 * Apply results limit to query.
		 * @param query Query definition
		 * @param serializedSql Serialized SQL query
		 * @param limit Limit to apply
		 * @param offset Offset to use
		 * @return Modified SQL query
		 */
		String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset);

	}

	/**
	 * Exception related to JDBC statement configuration errors.
	 */
	public class StatementConfigurationException extends Exception {

		private static final long serialVersionUID = -5669947102236544262L;

		/**
		 * Constructor
		 * @param message Error message
		 */
		public StatementConfigurationException(String message) {
			super(message);
		}

		/**
		 * Constructor
		 * @param message Error message
		 * @param cause The cause
		 */
		public StatementConfigurationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
