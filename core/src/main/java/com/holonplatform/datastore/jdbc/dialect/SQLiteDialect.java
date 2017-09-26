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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * SQLite {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class SQLiteDialect implements JdbcDialect {

	private static final long serialVersionUID = -2218115258014233676L;

	private static final SQLiteLimitHandler LIMIT_HANDLER = new SQLiteLimitHandler();

	private static final SQLiteParameterValueHandler PARAMETER_HANDLER = new SQLiteParameterValueHandler();
	private static final SQLiteParameterProcessor PARAMETER_PROCESSOR = new SQLiteParameterProcessor();

	private static final SQLiteValueDeserializer DESERIALIZER = new SQLiteValueDeserializer();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private final StatementConfigurator statementConfigurator;

	public SQLiteDialect() {
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
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getParameterProcessor()
	 */
	@Override
	public Optional<SQLParameterProcessor> getParameterProcessor() {
		return Optional.of(PARAMETER_PROCESSOR);
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#useOuterInJoins()
	 */
	@Override
	public boolean useOuterInJoins() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return false;
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
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return Optional.of(LIMIT_HANDLER);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getValueDeserializer()
	 */
	@Override
	public SQLValueDeserializer getValueDeserializer() {
		return DESERIALIZER;
	}

	@SuppressWarnings("serial")
	private static final class SQLiteLimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

	private static final class SQLiteParameterValueHandler implements StatementParameterHandler {

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
	private static final class SQLiteValueDeserializer implements SQLValueDeserializer {

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.JdbcDialect.SQLValueDeserializer#deserializeValue(com.holonplatform.core.
		 * query.QueryExpression, java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> T deserializeValue(QueryExpression<T> expression, Object value) {
			if (value != null) {

				// date and times
				Optional<TemporalType> temporalType = JdbcDatastoreUtils.getTemporalType(expression, true);
				if (temporalType.isPresent()) {

					LocalDateTime dt = null;
					if (TypeUtils.isString(value.getClass())) {
						dt = asDateTime((String) value, temporalType.orElse(TemporalType.DATE));
					} else if (TypeUtils.isNumber(value.getClass())) {
						dt = asDateTime((Number) value);
					}

					if (dt != null) {
						if (LocalDateTime.class.isAssignableFrom(expression.getType())) {
							return (T) dt;
						}
						if (LocalDate.class.isAssignableFrom(expression.getType())) {
							return (T) dt.toLocalDate();
						}
						if (LocalTime.class.isAssignableFrom(expression.getType())) {
							return (T) dt.toLocalTime();
						}
						if (Date.class.isAssignableFrom(expression.getType())) {
							return (T) ConversionUtils.fromLocalDateTime(dt);
						}
					}
				}
			}

			// fallback to default
			return SQLValueDeserializer.getDefault().deserializeValue(expression, value);
		}

	}

	private static LocalDateTime asDateTime(String value, TemporalType type) {
		if (value != null) {
			switch (type) {
			case DATE:
				try {
					return LocalDateTime.of(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE),
							LocalTime.of(0, 0, 0));
				} catch (@SuppressWarnings("unused") DateTimeParseException e) {
					return LocalDateTime.parse(value.trim().replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				}
			case TIME:
				try {
					LocalTime lt = LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
					return LocalDateTime.of(LocalDate.ofEpochDay(0), lt);
				} catch (@SuppressWarnings("unused") DateTimeParseException e) {
					return LocalDateTime.parse(value.trim().replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				}
			case DATE_TIME:
				try {
					return LocalDateTime.parse(value.trim().replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				} catch (@SuppressWarnings("unused") DateTimeParseException e) {
					return LocalDateTime.of(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE),
							LocalTime.of(0, 0, 0));
				}
			default:
				break;
			}
		}
		return null;
	}

	private static LocalDateTime asDateTime(Number value) {
		if (value != null) {
			if (TypeUtils.isIntegerNumber(value.getClass())) {
				return LocalDateTime.ofEpochSecond(value.longValue(), 0, ZoneOffset.UTC);
			}
		}
		return null;
	}

	private static final class SQLiteParameterProcessor implements SQLParameterProcessor {

		@Override
		public String processParameter(String serialized, ParameterValue parameter, DialectResolutionContext context) {
			if (context.getResolutionQueryClause().isPresent()
					&& (context.getResolutionQueryClause().get() == ResolutionQueryClause.WHERE)) {
				TemporalType temporalType = parameter.getTemporalType().orElse(null);
				if (temporalType != null) {
					Optional<String> serializedTime = SQLValueSerializer.serializeDate(parameter.getValue(),
							temporalType);
					if (serializedTime.isPresent()) {
						context.replaceParameter(serialized,
								ParameterValue.create(String.class, serializedTime.get(), temporalType));
						return serialized;
					}
				}
			}
			return serialized;
		}

	}

}
