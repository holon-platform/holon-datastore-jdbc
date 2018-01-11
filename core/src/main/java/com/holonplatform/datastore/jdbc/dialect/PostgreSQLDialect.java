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
package com.holonplatform.datastore.jdbc.dialect;

import java.io.IOException;
import java.io.Reader;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * PostgreSQL {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class PostgreSQLDialect implements JdbcDialect {

	private static final long serialVersionUID = -1351306688409439156L;

	private static final PostgreParameterProcessor PARAMETER_PROCESSOR = new PostgreParameterProcessor();

	private static final PostgreLimitHandler LIMIT_HANDLER = new PostgreLimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	public PostgreSQLDialect() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#init(com.holonplatform.datastore.jdbc.JdbcDatastore)
	 */
	@Override
	public void init(JdbcDatastore datastore) throws SQLException {
		datastore.withConnection(c -> {
			DatabaseMetaData databaseMetaData = c.getMetaData();
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			return null;
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getParameterProcessor()
	 */
	@Override
	public SQLParameterProcessor getParameterProcessor() {
		return PARAMETER_PROCESSOR;
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return Optional.of(LIMIT_HANDLER);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getTableName(java.lang.String)
	 */
	@Override
	public String getTableName(String tableName) {
		return (tableName != null) ? tableName.toLowerCase() : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getColumnName(java.lang.String)
	 */
	@Override
	public String getColumnName(String columnName) {
		return (columnName != null) ? columnName.toLowerCase() : null;
	}
	
	private static final class PostgreParameterProcessor implements SQLParameterProcessor {

		@Override
		public SQLParameterDefinition processParameter(SQLParameterDefinition parameter,
				JdbcResolutionContext context) {
			// Reader type serialization
			if (Reader.class.isAssignableFrom(parameter.getType())) {
				try {
					return SQLParameterDefinition.create(ConversionUtils.readerToString((Reader) parameter.getValue()));
				} catch (IOException e) {
					throw new RuntimeException("Failed to convert Reader to String [" + parameter.getValue() + "]", e);
				}
			}
			return parameter;
		}

	}

	@SuppressWarnings("serial")
	private static final class PostgreLimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

}
