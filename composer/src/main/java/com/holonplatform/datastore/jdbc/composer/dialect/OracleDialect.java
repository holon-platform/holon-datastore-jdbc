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

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.query.lock.LockAcquisitionException;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer.ValueProcessor;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.SQLExceptionHelper;

import oracle.jdbc.OracleConnection;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPTZ;

/**
 * Oracle {@link com.holonplatform.datastore.jdbc.composer.SQLDialect}.
 *
 * @since 5.1.0
 */
public class OracleDialect implements SQLDialect {

	private static final long serialVersionUID = 7693711472395387628L;

	private static final OracleValueDeserializer DESERIALIZER = new OracleValueDeserializer();

	private static final OracleLimitHandler LIMIT_HANDLER = new OracleLimitHandler();
	private static final Oracle12LimitHandler LIMIT_HANDLER_12c = new Oracle12LimitHandler();

	private boolean supportsLikeEscapeClause;

	private int oracleVersion;

	public OracleDialect() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.com.holonplatform.datastore.jdbc.composer.SQLDialect#init(com.
	 * holonplatform.datastore.jdbc.composer. SQLExecutionContext)
	 */
	@Override
	public void init(SQLDialectContext context) throws SQLException {
		DatabaseMetaData databaseMetaData = context.getOrRetrieveDatabaseMetaData().orElse(null);
		if (databaseMetaData != null) {
			// int driverMajorVersion = databaseMetaData.getDriverMajorVersion();
			oracleVersion = databaseMetaData.getDatabaseMajorVersion();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
		}
		context.getValueDeserializer().addValueProcessor(DESERIALIZER);
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() {
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
		if (errorCode == 54 || errorCode == 60 || errorCode == 4020 || errorCode == 4021 || errorCode == 30006) {
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
		return (oracleVersion >= 12) ? Optional.of(LIMIT_HANDLER_12c) : Optional.of(LIMIT_HANDLER);
	}

	@SuppressWarnings("serial")
	private static final class OracleLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			int maxRows = (offset > -1) ? limit + offset : limit;
			final StringBuilder sb = new StringBuilder(serializedSql.length() + 100);
			if (offset > -1) {
				sb.append("select * from ( select row_.*, rownum rownum_ from ( ");
			} else {
				sb.append("select * from ( ");
			}
			sb.append(serializedSql);
			if (offset > -1) {
				sb.append(" ) row_ where rownum <= ");
				sb.append(maxRows);
				sb.append(") where rownum_ > ");
				sb.append(offset);
			} else {
				sb.append(" ) where rownum <= ");
				sb.append(maxRows);
			}
			return sb.toString();
		}

	}

	@SuppressWarnings("serial")
	private static final class Oracle12LimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

	private static final class OracleValueDeserializer implements ValueProcessor {

		@Override
		public Object processValue(SQLExecutionContext context, TypedExpression<?> expression, Object value)
				throws SQLException {
			if (value != null) {
				// Oracle TIMESTAMP
				if (TIMESTAMP.class.isAssignableFrom(value.getClass())) {
					return ((TIMESTAMP) value).timestampValue();
				}
				if (TIMESTAMPTZ.class.isAssignableFrom(value.getClass()) && context.getConnection().isPresent()) {
					return TIMESTAMPTZ.toTimestamp(context.getConnection().get().unwrap(OracleConnection.class),
							((TIMESTAMPTZ) value).toBytes());
				}
			}
			return value;
		}

	}

}
