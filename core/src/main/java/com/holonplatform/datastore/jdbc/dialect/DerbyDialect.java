/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.datastore.jdbc.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction.DefaultFunction;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;

/**
 * Derby {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class DerbyDialect implements JdbcDialect {

	private static final long serialVersionUID = 2181521615336045432L;

	private final static Logger LOGGER = JdbcDatastoreLogger.create();

	private static final SQLFunction AVG_FUNCTION = new AvgFunction();

	private static final DerbyLimitHandler LIMIT_HANDLER = new DerbyLimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private Integer databaseMajorVersion = null;
	private Integer databaseMinorVersion = null;

	private final StatementConfigurator statementConfigurator;

	public DerbyDialect() {
		super();
		this.statementConfigurator = StatementConfigurator.create(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#init(com.holonplatform.datastore.jdbc.JdbcDatastore)
	 */
	@Override
	public void init(JdbcDatastore datastore) throws SQLException {
		datastore.withConnection(c -> {
			try {
				DatabaseMetaData dbmd = c.getMetaData();
				supportsGeneratedKeys = true; // dbmd.supportsGetGeneratedKeys();
				generatedKeyAlwaysReturned = false; // dbmd.generatedKeyAlwaysReturned();
				supportsLikeEscapeClause = dbmd.supportsLikeEscapeClause();
				String version = dbmd.getDatabaseProductVersion();
				if (version != null) {
					databaseMajorVersion = dbmd.getDatabaseMajorVersion();
					databaseMinorVersion = dbmd.getDatabaseMinorVersion();
					LOGGER.info("Detected Derby database version: " + version + " [" + databaseMajorVersion + "."
							+ databaseMinorVersion + "]");
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to detect Derby database version", e);
			}
			return null;
		});
	}

	private boolean isTenDotFiveOrNewer() {
		if (databaseMajorVersion != null && databaseMinorVersion != null) {
			return databaseMajorVersion >= 10 && databaseMinorVersion >= 5;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getStatementConfigurator()
	 */
	@Override
	public StatementConfigurator getStatementConfigurator() {
		return statementConfigurator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#supportsLikeEscapeClause()
	 */
	@Override
	public boolean supportsLikeEscapeClause() {
		return supportsLikeEscapeClause;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#supportsGetGeneratedKeys()
	 */
	@Override
	public boolean supportsGetGeneratedKeys() {
		return supportsGeneratedKeys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() {
		return generatedKeyAlwaysReturned;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#supportGetGeneratedKeyByName()
	 */
	@Override
	public boolean supportGetGeneratedKeyByName() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getTableName(java.lang.String)
	 */
	@Override
	public String getTableName(String tableName) {
		return (tableName != null) ? tableName.toUpperCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getColumnName(java.lang.String)
	 */
	@Override
	public String getColumnName(String columnName) {
		return (columnName != null) ? columnName.toUpperCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getFunction(java.lang.String)
	 */
	@Override
	public SQLFunction getFunction(String name) {
		if (DefaultFunction.AVG.getName().equals(name)) {
			return AVG_FUNCTION;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return isTenDotFiveOrNewer() ? Optional.of(LIMIT_HANDLER) : Optional.empty();
	}

	// -------

	@SuppressWarnings("serial")
	private static final class AvgFunction implements SQLFunction {

		@Override
		public String getName() {
			return "avg";
		}

		@Override
		public boolean hasParenthesesIfNoArguments() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.SQLFunction#serialize(java.util.List)
		 */
		@Override
		public String serialize(List<String> arguments) {
			final StringBuilder sb = new StringBuilder();
			sb.append(getName());
			sb.append("(");
			sb.append("cast(");
			sb.append(arguments.get(0));
			sb.append(" as double)");
			sb.append(")");
			return sb.toString();
		}

	}

	@SuppressWarnings("serial")
	private static final class DerbyLimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

}
