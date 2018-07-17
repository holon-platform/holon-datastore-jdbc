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
import com.holonplatform.core.internal.query.lock.LockAcquisitionException;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.SQLExceptionHelper;

/**
 * HANA {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class HANADialect implements SQLDialect {

	private static final long serialVersionUID = 2380865898634838990L;

	private static final HANALimitHandler LIMIT_HANDLER = new HANALimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned = false;
	private boolean supportsLikeEscapeClause;

	public HANADialect() {
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
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
		}
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
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#getLockClause(com.holonplatform.core.query.lock.LockMode,
	 * long)
	 */
	@Override
	public Optional<String> getLockClause(LockMode mode, long timeout) {
		if (timeout == 0) {
			return Optional.of("FOR UPDATE NOWAIT");
		}
		if (timeout > 0) {
			return Optional.of("FOR UPDATE WAIT " + ((timeout < 1000) ? 1 : Math.round((float) timeout / 1000)));
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
		if (errorCode == 131 || errorCode == 132 || errorCode == 133 || errorCode == 146) {
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
	private static final class HANALimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

}
