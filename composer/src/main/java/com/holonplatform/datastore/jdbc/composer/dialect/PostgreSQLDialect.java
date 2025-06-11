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

import java.io.IOException;
import java.io.Reader;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.query.lock.LockAcquisitionException;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.SQLExceptionHelper;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.ReaderToStringParameterResolver;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * PostgreSQL {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class PostgreSQLDialect implements SQLDialect {

	private static final long serialVersionUID = -1351306688409439156L;

	private static final PostgreLimitHandler LIMIT_HANDLER = new PostgreLimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	public PostgreSQLDialect() {
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
		}
		context.addExpressionResolver(ReaderToStringParameterResolver.INSTANCE);
		context.addExpressionResolver(new ReaderParameterResolver());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#updateStatementSetAliasSupported()
	 */
	@Override
	public boolean updateStatementSetAliasSupported() {
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
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#getLockClause(com.holonplatform.core.query.lock.LockMode,
	 * long)
	 */
	@Override
	public Optional<String> getLockClause(LockMode mode, long timeout) {
		if (timeout == 0) {
			return Optional.of("FOR UPDATE NOWAIT");
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
		final String sqlState = SQLExceptionHelper.getSqlState(exception).orElse(null);
		if ("40P01".equals(sqlState) || "55P03".equals(sqlState)) {
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

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getTableName(java.lang.String)
	 */
	@Override
	public String getTableName(String tableName) {
		return (tableName != null) ? tableName.toLowerCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getColumnName(java.lang.String)
	 */
	@Override
	public String getColumnName(String columnName) {
		return (columnName != null) ? columnName.toLowerCase() : null;
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	@Priority(Integer.MAX_VALUE - 10000)
	private static final class ReaderParameterResolver
			implements SQLContextExpressionResolver<SQLParameter, SQLParameter> {

		@Override
		public Class<? extends SQLParameter> getExpressionType() {
			return SQLParameter.class;
		}

		@Override
		public Class<? extends SQLParameter> getResolvedType() {
			return SQLParameter.class;
		}

		@Override
		public Optional<SQLParameter> resolve(SQLParameter expression, SQLCompositionContext context)
				throws InvalidExpressionException {
			if (expression.getValue() != null && expression.getValue() instanceof Reader
					&& (Reader.class.isAssignableFrom(expression.getType())
							|| String.class.isAssignableFrom(expression.getType()))) {
				try {
					return Optional.of(SQLParameter.create(
							ConversionUtils.readerToString((Reader) expression.getValue(), false), String.class));
				} catch (IOException e) {
					throw new InvalidExpressionException("Failed to convert Reader parameter to String", e);
				}
			}
			return Optional.empty();
		}

	}

	@SuppressWarnings("serial")
	private static final class PostgreLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

}
