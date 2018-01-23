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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.expressions.SQLFunction;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;
import com.holonplatform.datastore.jdbc.expressions.SQLQueryClauses;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.dialect.DialectFunctionsRegistry;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;

/**
 * SQLite {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class SQLiteDialect implements JdbcDialect {

	private static final long serialVersionUID = -2218115258014233676L;

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	private static final SQLiteLimitHandler LIMIT_HANDLER = new SQLiteLimitHandler();

	private static final SQLiteParameterProcessor PARAMETER_PROCESSOR = new SQLiteParameterProcessor();

	private static final SQLiteValueDeserializer DESERIALIZER = new SQLiteValueDeserializer();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	public SQLiteDialect() {
		super();
		this.functions.registerFunction(Year.class, new ExtractTemporalPartFunction("%Y"));
		this.functions.registerFunction(Month.class, new ExtractTemporalPartFunction("%m"));
		this.functions.registerFunction(Day.class, new ExtractTemporalPartFunction("%d"));
		this.functions.registerFunction(Hour.class, new ExtractTemporalPartFunction("%H"));
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
		public String limitResults(SQLQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

	@SuppressWarnings("serial")
	private static final class SQLiteValueDeserializer implements SQLValueDeserializer {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T deserializeValue(Connection connection, TypedExpression<T> expression, Object value) {
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
			return SQLValueDeserializer.getDefault().deserializeValue(connection, expression, value);
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
		public SQLParameterDefinition processParameter(SQLParameterDefinition parameter) {
			// Reader type serialization
			if (Reader.class.isAssignableFrom(parameter.getType())) {
				try {
					return SQLParameterDefinition.create(ConversionUtils.readerToString((Reader) parameter.getValue()),
							String.class);
				} catch (IOException e) {
					throw new RuntimeException("Failed to convert Reader to String [" + parameter.getValue() + "]", e);
				}
			}
			// check temporals
			return parameter.getTemporalType()
					.flatMap(temporalType -> SQLValueSerializer.serializeDate(parameter.getValue(), temporalType)
							.map(value -> SQLParameterDefinition.create(value, String.class)))
					.orElse(parameter);
		}

	}

	@SuppressWarnings("serial")
	private static final class ExtractTemporalPartFunction implements SQLFunction {

		private final String part;

		public ExtractTemporalPartFunction(String part) {
			super();
			this.part = part;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.expressions.SQLFunction#serialize(java.util.List)
		 */
		@Override
		public String serialize(List<String> arguments) throws InvalidExpressionException {
			final StringBuilder sb = new StringBuilder();
			sb.append("CAST(");
			sb.append("strftime('");
			sb.append(part);
			sb.append("', ");
			sb.append(arguments.get(0));
			sb.append(")");
			sb.append(" AS integer)");
			return sb.toString();
		}

		@Override
		public void validate() throws InvalidExpressionException {
		}

	}

}
