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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryExecutionException;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

import oracle.jdbc.OracleConnection;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPTZ;

/**
 * Oracle {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class OracleDialect implements JdbcDialect {

	private static final long serialVersionUID = 7693711472395387628L;

	private static final OracleParameterProcessor PARAMETER_PROCESSOR = new OracleParameterProcessor();

	private static final OracleValueDeserializer DESERIALIZER = new OracleValueDeserializer();

	private static final OracleLimitHandler LIMIT_HANDLER = new OracleLimitHandler();
	private static final Oracle12LimitHandler LIMIT_HANDLER_12c = new Oracle12LimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private int oracleVersion;

	public OracleDialect() {
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

			int driverMajorVersion = databaseMetaData.getDriverMajorVersion();

			oracleVersion = databaseMetaData.getDatabaseMajorVersion();
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			generatedKeyAlwaysReturned = (driverMajorVersion < 12) ? false
					: databaseMetaData.generatedKeyAlwaysReturned();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			return null;
		});
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

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getValueDeserializer()
	 */
	@Override
	public SQLValueDeserializer getValueDeserializer() {
		return DESERIALIZER;
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return (oracleVersion >= 12) ? Optional.of(LIMIT_HANDLER_12c) : Optional.of(LIMIT_HANDLER);
	}

	@SuppressWarnings("serial")
	private static final class OracleLimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
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
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

	@SuppressWarnings("serial")
	private static final class OracleValueDeserializer implements SQLValueDeserializer {

		@Override
		public <T> T deserializeValue(Connection connection, QueryExpression<T> expression, Object value) {
			try {
				Object valueToDeserialize = value;
				if (value != null) {
					// Oracle TIMESTAMP
					if (TIMESTAMP.class.isAssignableFrom(value.getClass())) {
						valueToDeserialize = ((TIMESTAMP) value).timestampValue();
					}
					if (TIMESTAMPTZ.class.isAssignableFrom(value.getClass())) {
						valueToDeserialize = TIMESTAMPTZ.toTimestamp(connection.unwrap(OracleConnection.class),
								((TIMESTAMPTZ) value).toBytes());
					}
				}
				// fallback to default
				return SQLValueDeserializer.getDefault().deserializeValue(connection, expression, valueToDeserialize);
			} catch (SQLException e) {
				throw new QueryExecutionException("Failed to deserialize value [" + value + "]", e);
			}
		}

	}

	private static final class OracleParameterProcessor implements SQLParameterProcessor {

		@Override
		public SQLParameterDefinition processParameter(SQLParameterDefinition parameter,
				JdbcResolutionContext context) {
			TemporalType temporalType = parameter.getTemporalType().orElse(null);
			if (temporalType != null && TemporalType.DATE == temporalType) {
				Optional<String> value = SQLValueSerializer.serializeDate(parameter.getValue(), temporalType);
				if (value.isPresent()) {
					return SQLParameterDefinition.create(value.get(),
							p -> "to_date(" + p + ", '" + SQLValueSerializer.ANSI_DATE_FORMAT.toUpperCase() + "')");
				}
			}
			return parameter;
		}

	}

}
