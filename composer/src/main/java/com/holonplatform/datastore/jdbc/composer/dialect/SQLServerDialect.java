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
import com.holonplatform.core.query.TemporalFunction.CurrentDate;
import com.holonplatform.core.query.TemporalFunction.CurrentLocalDate;
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
 * MSSQL {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class SQLServerDialect implements SQLDialect {

	private static final long serialVersionUID = -3585193712573424374L;

	private final static Logger LOGGER = SQLComposerLogger.create();

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	private static final SQLServer2012LimitHandler LIMIT_HANDLER_2012 = new SQLServer2012LimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private boolean version2012orHigher;

	public SQLServerDialect() {
		super();
		this.functions.registerFunction(Avg.class, new AvgFunction());
		this.functions.registerFunction(CurrentDate.class, new CurrentDateFunction());
		this.functions.registerFunction(CurrentLocalDate.class, new CurrentDateFunction());
		this.functions.registerFunction(Year.class, new ExtractTemporalPartFunction("year"));
		this.functions.registerFunction(Month.class, new ExtractTemporalPartFunction("month"));
		this.functions.registerFunction(Day.class, new ExtractTemporalPartFunction("day"));
		this.functions.registerFunction(Hour.class, new ExtractTemporalPartFunction("hour"));
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
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			int version = databaseMetaData.getDatabaseMajorVersion();
			version2012orHigher = version >= 0 && version >= 11;
		} else {
			LOGGER.warn("Failed to detect SQLServer database version - a limit handler will not be available");
		}
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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#updateStatementFromSupported()
	 */
	@Override
	public boolean updateStatementFromSupported() {
		return true;
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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return version2012orHigher ? Optional.of(LIMIT_HANDLER_2012) : Optional.empty();
	}

	private static final class AvgFunction implements SQLFunction {

		@Override
		public String serialize(List<String> arguments) {
			final StringBuilder sb = new StringBuilder();
			sb.append("AVG");
			sb.append("(");
			sb.append("CAST(");
			sb.append(arguments.get(0));
			sb.append(" AS decimal)");
			sb.append(")");
			return sb.toString();
		}

		@Override
		public void validate() throws InvalidExpressionException {
		}

	}

	private static final class ExtractTemporalPartFunction implements SQLFunction {

		private final String part;

		public ExtractTemporalPartFunction(String part) {
			super();
			this.part = part;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.expressions.SQLFunction#serialize(java.util.List)
		 */
		@Override
		public String serialize(List<String> arguments) throws InvalidExpressionException {
			final StringBuilder sb = new StringBuilder();
			sb.append("DATEPART(");
			sb.append(part);
			sb.append(", ");
			sb.append(arguments.get(0));
			sb.append(")");
			return sb.toString();
		}

		@Override
		public void validate() throws InvalidExpressionException {
		}

	}

	private static final class CurrentDateFunction implements SQLFunction {

		@Override
		public String serialize(List<String> arguments) {
			return "CAST(GETDATE() AS DATE)";
		}

		@Override
		public void validate() throws InvalidExpressionException {
		}

	}

	@SuppressWarnings("serial")
	private static final class SQLServer2012LimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

}
