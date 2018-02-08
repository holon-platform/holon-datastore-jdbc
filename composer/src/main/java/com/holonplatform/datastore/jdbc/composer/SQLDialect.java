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
import java.sql.Types;
import java.util.Optional;

import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.composer.dialect.DB2Dialect;
import com.holonplatform.datastore.jdbc.composer.dialect.DefaultDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.DerbyDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.H2Dialect;
import com.holonplatform.datastore.jdbc.composer.dialect.HANADialect;
import com.holonplatform.datastore.jdbc.composer.dialect.HSQLDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.InformixDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.MariaDBDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.MySQLDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.OracleDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.PostgreSQLDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.SQLServerDialect;
import com.holonplatform.datastore.jdbc.composer.dialect.SQLiteDialect;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DefaultLimitHandler;
import com.holonplatform.jdbc.DatabasePlatform;

/**
 * Represents a dialect of SQL implemented by a particular database.
 *
 * @since 5.0.0
 */
public interface SQLDialect extends Serializable {

	/**
	 * Dialect initialization hook at parent datastore initialization.
	 * @param context Dialect context
	 * @throws SQLException An error occurred during dialect initialization
	 */
	void init(SQLDialectContext context) throws SQLException;

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
	 * Get whether alias is supported in the UPDATE clause.
	 * @return <code>true</code> if alias is supported in the UPDATE clause
	 */
	default boolean updateStatementAliasSupported() {
		return true;
	}

	/**
	 * Get whether a FROM clause is supported in the UPDATE clause.
	 * @return <code>true</code> if a FROM clause is supported in the UPDATE clause
	 */
	default boolean updateStatementFromSupported() {
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
	 * Get whether given SQL type is supported by this dialect.
	 * @param sqlType The SQL type id
	 * @return <code>true</code> if type is supported, <code>false</code> otherwise
	 * @see Types
	 */
	default boolean supportsSqlType(int sqlType) {
		return true;
	}

	/**
	 * Get the Java type which corresponds to given SQL type for this dialect.
	 * @param sqlType The sql type (not null)
	 * @return Optional Java type, empty to use default
	 */
	default Optional<Class<?>> getJavaType(SQLType sqlType) {
		return Optional.empty();
	}

	/**
	 * Get the SQL type which corresponds to given Java type for this dialect.
	 * @param javaType The sql type (not null)
	 * @return Optional SQL type, empty to use default
	 */
	default Optional<SQLType> getSqlType(Class<?> javaType) {
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
		String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset);

	}

	/**
	 * Detect a suitable {@link SQLDialect} to use with given database platform, if available.
	 * @param database Database platform
	 * @return Optional {@link SQLDialect} for given database platform
	 */
	static Optional<SQLDialect> detect(DatabasePlatform database) {
		if (database != null) {
			switch (database) {
			case DB2:
				return Optional.of(new DB2Dialect());
			case DB2_AS400:
				return Optional.of(new DB2Dialect());
			case DERBY:
				return Optional.of(new DerbyDialect());
			case H2:
				return Optional.of(new H2Dialect());
			case HANA:
				return Optional.of(new HANADialect());
			case HSQL:
				return Optional.of(new HSQLDialect());
			case INFORMIX:
				return Optional.of(new InformixDialect());
			case MARIADB:
				return Optional.of(new MariaDBDialect());
			case MYSQL:
				return Optional.of(new MySQLDialect());
			case ORACLE:
				return Optional.of(new OracleDialect());
			case POSTGRESQL:
				return Optional.of(new PostgreSQLDialect());
			case SQLITE:
				return Optional.of(new SQLiteDialect());
			case SQL_SERVER:
				return Optional.of(new SQLServerDialect());
			case NONE:
				return Optional.of(new DefaultDialect());
			default:
				break;
			}
		}
		return Optional.empty();
	}

}
