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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultLimitHandler;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultSQLFunction;
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
	 * Get the {@link SQLFunction} bound to given symbolic name.
	 * @param name Function symbolic name
	 * @return Optional {@link SQLFunction} bound to given symbolic name. If null, a default function will be used, if
	 *         available
	 */
	default SQLFunction getFunction(String name) {
		return null;
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
		 * @param expression Query expression
		 * @param value Value to deserialize
		 * @return Deserialized value
		 */
		<T> T deserializeValue(QueryExpression<T> expression, Object value);

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
	 * SQL function representation.
	 */
	public interface SQLFunction extends Serializable {

		/**
		 * Default SQL function enumeration.
		 */
		public enum DefaultFunction {

			/**
			 * Results count
			 */
			COUNT("count"),

			/**
			 * Smallest value
			 */
			MIN("min"),

			/**
			 * Largest value
			 */
			MAX("max"),

			/**
			 * Average value
			 */
			AVG("avg"),

			/**
			 * Sum of values
			 */
			SUM("sum"),

			/**
			 * To lowercase
			 */
			LOWER("lower"),

			/**
			 * To uppercase
			 */
			UPPER("upper");

			private final String name;

			private DefaultFunction(String name) {
				this.name = name;
			}

			/**
			 * Get the function symbolic name
			 * @return the function name
			 */
			public String getName() {
				return name;
			}

		}

		/**
		 * Get the function name
		 * @return The function name
		 */
		String getName();

		/**
		 * Get whether parentheses are required if there are no arguments.
		 * @return <code>true</code> if parentheses are required if there are no arguments, <code>false</code> otherwise
		 */
		boolean hasParenthesesIfNoArguments();

		/**
		 * Serialize the function as SQL
		 * @param arguments Optional function arguments
		 * @return Serialized function
		 */
		String serialize(List<String> arguments);

		/**
		 * Serialize the function as SQL
		 * @param arguments Function arguments
		 * @return Serialized function
		 */
		default String serialize(String... arguments) {
			return serialize((arguments != null) ? Arrays.asList(arguments) : Collections.emptyList());
		}

		/**
		 * Create a {@link SQLFunction}.
		 * @param name Function name (not null)
		 * @param parenthesesIfNoArguments Whether parentheses are required if there are no arguments
		 * @return The {@link SQLFunction}
		 */
		static SQLFunction create(String name, boolean parenthesesIfNoArguments) {
			return new DefaultSQLFunction(name, parenthesesIfNoArguments);
		}

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
