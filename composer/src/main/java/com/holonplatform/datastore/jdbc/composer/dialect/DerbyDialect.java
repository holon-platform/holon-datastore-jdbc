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
package com.holonplatform.datastore.jdbc.composer.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.QueryFunction.Avg;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.SQLComposerLogger;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DialectFunctionsRegistry;

/**
 * Derby {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class DerbyDialect implements SQLDialect {

	private static final long serialVersionUID = 2181521615336045432L;

	private final static Logger LOGGER = SQLComposerLogger.create();

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	private static final DerbyLimitHandler LIMIT_HANDLER = new DerbyLimitHandler();

	private boolean supportsGeneratedKeys = true;
	private boolean generatedKeyAlwaysReturned = false;
	private boolean supportsLikeEscapeClause;

	private Integer databaseMajorVersion = null;
	private Integer databaseMinorVersion = null;

	public DerbyDialect() {
		super();
		this.functions.registerFunction(Avg.class, new AvgFunction());
		this.functions.registerFunction(Year.class, SQLFunction.create("YEAR"));
		this.functions.registerFunction(Month.class, SQLFunction.create("MONTH"));
		this.functions.registerFunction(Day.class, SQLFunction.create("DAY"));
		this.functions.registerFunction(Hour.class, SQLFunction.create("HOUR"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#init(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext)
	 */
	@Override
	public void init(SQLDialectContext context) throws SQLException {
		DatabaseMetaData databaseMetaData = context.getOrRetrieveDatabaseMetaData().orElse(null);
		if (databaseMetaData != null) {
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
			databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();
		}
		if (databaseMajorVersion != null && databaseMinorVersion != null) {
			LOGGER.info("Detected Derby database version: " + databaseMajorVersion + "." + databaseMinorVersion + ".x");
		} else {
			LOGGER.warn("Failed to detect Derby database version - a limit handler will not be available");
		}
	}

	private boolean isTenDotFiveOrNewer() {
		if (databaseMajorVersion != null && databaseMinorVersion != null) {
			return databaseMajorVersion >= 10 && databaseMinorVersion >= 5;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#resolveFunction(com.holonplatform.core.query.QueryFunction)
	 */
	@Override
	public Optional<SQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return functions.getFunction(function);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#supportsLikeEscapeClause()
	 */
	@Override
	public boolean supportsLikeEscapeClause() {
		return supportsLikeEscapeClause;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#supportsGetGeneratedKeys()
	 */
	@Override
	public boolean supportsGetGeneratedKeys() {
		return supportsGeneratedKeys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() {
		return generatedKeyAlwaysReturned;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#supportGetGeneratedKeyByName()
	 */
	@Override
	public boolean supportGetGeneratedKeyByName() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getTableName(java.lang.String)
	 */
	@Override
	public String getTableName(String tableName) {
		return (tableName != null) ? tableName.toUpperCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getColumnName(java.lang.String)
	 */
	@Override
	public String getColumnName(String columnName) {
		return (columnName != null) ? columnName.toUpperCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return isTenDotFiveOrNewer() ? Optional.of(LIMIT_HANDLER) : Optional.empty();
	}

	// -------

	private static final class AvgFunction implements SQLFunction {

		@Override
		public String serialize(List<String> arguments) {
			final StringBuilder sb = new StringBuilder();
			sb.append("avg");
			sb.append("(");
			sb.append("cast(");
			sb.append(arguments.get(0));
			sb.append(" as double)");
			sb.append(")");
			return sb.toString();
		}

		@Override
		public void validate() throws InvalidExpressionException {
		}

	}

	@SuppressWarnings("serial")
	private static final class DerbyLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

}
