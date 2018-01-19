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
package com.holonplatform.datastore.jdbc.composer;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryClauses;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DefaultLimitHandler;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DefaultSQLProcessedParameter;

/**
 * Represents a dialect of SQL implemented by a particular database.
 *
 * @since 5.0.0
 */
public interface SQLDialect extends Serializable {

	/**
	 * Dialect initialization hook at parent datastore initialization.
	 * @param context Execution context
	 * @throws SQLException An error occurred during dialect initialization
	 */
	void init(SQLExecutionContext context) throws SQLException;

	/**
	 * Resolve given <code>function</code> into a dialect-specific {@link SQLFunction}.
	 * @param function The function to resolve (not null)
	 * @return A dialect-specific function resolution, or empty to fallback to the default function resolution, if
	 *         available
	 */
	default Optional<SQLFunction> resolveFunction(QueryFunction<?, ?> function) {
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
	 * Get the dialect {@link SQLParameterProcessor}.
	 * @return Optional SQLParameterProcessor
	 */
	default Optional<SQLParameterProcessor> getParameterProcessor() {
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
	 * Get the {@link LimitHandler}
	 * @return Optional limit handler
	 */
	default Optional<LimitHandler> getLimitHandler() {
		return Optional.of(DefaultLimitHandler.INSTANCE);
	}

	// ------- types

	/**
	 * Represents a {@link SQLParameter} processed by a {@link SQLParameterProcessor}.
	 */
	public interface SQLProcessedParameter {

		/**
		 * Get the processed SQL parameter
		 * @return the SQL parameter
		 */
		SQLParameter getParameter();

		/**
		 * Get the function to apply when the parameter is serialized in the SQL statement.
		 * @return Optional function to apply when the parameter is serialized in the SQL statement
		 */
		Optional<Function<String, String>> getParameterSerializer();

		/**
		 * Create a new {@link SQLProcessedParameter}.
		 * @param parameter Processed parameter (not null)
		 * @return A new {@link SQLProcessedParameter}
		 */
		static SQLProcessedParameter create(SQLParameter parameter) {
			return new DefaultSQLProcessedParameter(parameter);
		}

		/**
		 * Create a new {@link SQLProcessedParameter}.
		 * @param parameter Processed parameter (not null)
		 * @param parameterSerializer The function to apply when the parameter is serialized in the SQL statement
		 * @return A new {@link SQLProcessedParameter}
		 */
		static SQLProcessedParameter create(SQLParameter parameter, Function<String, String> parameterSerializer) {
			return new DefaultSQLProcessedParameter(parameter, parameterSerializer);
		}

	}

	/**
	 * Parameter processor for SQL query parameters manipulation.
	 */
	@FunctionalInterface
	public interface SQLParameterProcessor {

		/**
		 * Process given parameter definition.
		 * @param context Context
		 * @param parameter Parameter definition
		 * @return Processed parameter definition (not null)
		 */
		SQLProcessedParameter processParameter(SQLContext context, SQLParameter parameter);

	}

	/**
	 * Handler to apply limit/offset to SQL query.
	 */
	@FunctionalInterface
	public interface LimitHandler extends Serializable {

		/**
		 * Apply results limit to query.
		 * @param query SQL Query definition
		 * @param serializedSql Serialized SQL query
		 * @param limit Limit to apply
		 * @param offset Offset to use
		 * @return Modified SQL query
		 */
		String limitResults(SQLQueryClauses query, String serializedSql, int limit, int offset);

	}

}
