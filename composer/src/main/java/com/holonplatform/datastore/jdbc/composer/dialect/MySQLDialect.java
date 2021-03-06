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
import java.util.Optional;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.lock.LockAcquisitionException;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.SQLExceptionHelper;

/**
 * MySQL {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class MySQLDialect implements SQLDialect {

	private static final long serialVersionUID = 2199028796025162694L;

	private static final MySQLLimitHandler LIMIT_HANDLER = new MySQLLimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private int majorVersion;

	public MySQLDialect() {
		super();
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

			majorVersion = databaseMetaData.getDatabaseMajorVersion();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#updateStatementFromSupported()
	 */
	@Override
	public boolean updateStatementFromSupported() {
		return false;
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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#useOuterInJoins()
	 */
	@Override
	public boolean useOuterInJoins() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#deleteStatementTargetRequired()
	 */
	@Override
	public boolean deleteStatementTargetRequired() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#getLockClause(com.holonplatform.core.query.lock.LockMode,
	 * long)
	 */
	@Override
	public Optional<String> getLockClause(LockMode mode, long timeout) {
		if (majorVersion >= 8) {
			if (timeout == 0) {
				return Optional.of("FOR UPDATE NOWAIT");
			}
		}
		return Optional.of("FOR UPDATE");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#translateException(java.sql.SQLException)
	 */
	@Override
	public DataAccessException translateException(SQLException exception) {
		// check lock acquisition exception
		final int errorCode = SQLExceptionHelper.getErrorCode(exception);
		if (errorCode == 1205 || errorCode == 1206 || errorCode == 1207 || errorCode == 3572) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		final String sqlState = SQLExceptionHelper.getSqlState(exception).orElse(null);
		if ("41000".equals(sqlState) || "40001".equals(sqlState)) {
			return new LockAcquisitionException("Failed to acquire lock", exception);
		}
		return SQLDialect.super.translateException(exception);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return Optional.of(LIMIT_HANDLER);
	}

	@SuppressWarnings("serial")
	private static final class MySQLLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + offset + "," + limit) : (" limit " + limit));
		}

	}

}
