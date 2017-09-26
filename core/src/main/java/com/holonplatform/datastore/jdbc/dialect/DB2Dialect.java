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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction.DefaultFunction;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * DB2 {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class DB2Dialect implements JdbcDialect {

	private static final long serialVersionUID = -7970780196163324775L;

	private static final SQLFunction AVG_FUNCTION = new AvgFunction();

	private static final DB2ParameterValueHandler PARAMETER_HANDLER = new DB2ParameterValueHandler();

	private static final DB2LimitHandler LIMIT_HANDLER = new DB2LimitHandler();

	private boolean supportsLikeEscapeClause;

	private final StatementConfigurator statementConfigurator;

	public DB2Dialect() {
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
			DatabaseMetaData databaseMetaData = c.getMetaData();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			return null;
		});
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getStatementParameterHandler()
	 */
	@Override
	public Optional<StatementParameterHandler> getStatementParameterHandler() {
		return Optional.of(PARAMETER_HANDLER);
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() {
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return Optional.of(LIMIT_HANDLER);
	}

	@SuppressWarnings("serial")
	private static final class DB2LimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			int maxRows = (offset > -1) ? limit + offset : limit;

			if (offset > -1) {
				return "select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( "
						+ serializedSql + " fetch first " + maxRows
						+ " rows only ) as inner2_ ) as inner1_ where rownumber_ > " + offset + " order by rownumber_";
			}
			return serializedSql + " fetch first " + maxRows + " rows only";
		}

	}

	private static final class DB2ParameterValueHandler implements StatementParameterHandler {

		@Override
		public Optional<Object> setParameterValue(Connection connection, PreparedStatement statement, int index,
				ParameterValue parameterValue) throws SQLException {
			if (parameterValue.getValue() != null && Reader.class.isAssignableFrom(parameterValue.getType())) {
				// treat as string
				try {
					String str = readerToString((Reader) parameterValue.getValue());
					statement.setString(index, str);
					return Optional.of(str);
				} catch (IOException e) {
					throw new SQLException(e);
				}
			}
			return Optional.empty();
		}

		private static String readerToString(Reader reader) throws IOException {
			char[] arr = new char[8 * 1024];
			StringBuilder buffer = new StringBuilder();
			int numCharsRead;
			while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
				buffer.append(arr, 0, numCharsRead);
			}
			reader.close();
			return buffer.toString();
		}

	}

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
			sb.append(" as decimal)");
			sb.append(")");
			return sb.toString();
		}

	}

}
