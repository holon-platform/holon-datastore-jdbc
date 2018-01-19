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

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryClauses;

/**
 * PostgreSQL {@link SQLDialect}.
 *
 * @since 5.0.0
 */
public class PostgreSQLDialect implements SQLDialect {

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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#init(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext)
	 */
	@Override
	public void init(SQLExecutionContext context) throws SQLException {
		DatabaseMetaData databaseMetaData = context.withConnection(c -> c.getMetaData());
		supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
		generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
		supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#getParameterProcessor()
	 */
	@Override
	public Optional<SQLParameterProcessor> getParameterProcessor() {
		return Optional.of(PARAMETER_PROCESSOR);
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

	private static final class PostgreParameterProcessor implements SQLParameterProcessor {

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.composer.SQLDialect.SQLParameterProcessor#processParameter(com.holonplatform
		 * .datastore.jdbc.composer.SQLContext,
		 * com.holonplatform.datastore.jdbc.composer.expression.SQLParameterDefinition)
		 */
		@Override
		public SQLProcessedParameter processParameter(SQLContext context, SQLParameter parameter) {
			// Reader type serialization
			if (Reader.class.isAssignableFrom(parameter.getType())) {
				try {
					return SQLProcessedParameter
							.create(SQLParameter.create(ConversionUtils.readerToString((Reader) parameter.getValue())));
				} catch (IOException e) {
					throw new RuntimeException("Failed to convert Reader to String [" + parameter.getValue() + "]", e);
				}
			}
			return SQLProcessedParameter.create(parameter);
		}

	}

	@SuppressWarnings("serial")
	private static final class PostgreLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

}
