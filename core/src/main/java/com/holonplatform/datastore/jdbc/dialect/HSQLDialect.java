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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.QueryFunction.Avg;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.expressions.SQLFunction;
import com.holonplatform.datastore.jdbc.expressions.SQLQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.DialectFunctionsRegistry;

/**
 * HSQL {@link JdbcDialect}.
 * 
 * <p>
 * Note: only version 2.x or higher is supported for query pagination using limit/offset.
 * </p>
 *
 * @since 5.0.0
 */
public class HSQLDialect implements JdbcDialect {

	private static final long serialVersionUID = -4357003634609368564L;

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	private static final HSQLLimitHandler LIMIT_HANDLER = new HSQLLimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	public HSQLDialect() {
		super();
		this.functions.registerFunction(Avg.class, new AvgFunction());
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#resolveFunction(com.holonplatform.core.query.QueryFunction)
	 */
	@Override
	public Optional<SQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return functions.getFunction(function);
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

	// -------

	@SuppressWarnings("serial")
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
	private static final class HSQLLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" offset " + offset + " limit " + limit) : (" limit " + limit));
		}

	}

}
