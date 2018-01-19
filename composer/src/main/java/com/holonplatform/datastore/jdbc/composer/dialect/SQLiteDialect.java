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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Provider;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer.ValueProcessor;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryClauses;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DialectFunctionsRegistry;

/**
 * SQLite {@link SQLDialect}.
 *
 * @since 5.0.0
 */
public class SQLiteDialect implements SQLDialect {

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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#init(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext)
	 */
	@Override
	public void init(SQLExecutionContext context) throws SQLException {
		DatabaseMetaData databaseMetaData = context.withConnection(c -> c.getMetaData());
		supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
		generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
		supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();

		context.getValueDeserializer().addValueProcessor(DESERIALIZER);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#resolveFunction(com.holonplatform.core.query.QueryFunction)
	 */
	@Override
	public Optional<SQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return functions.getFunction(function);
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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#useOuterInJoins()
	 */
	@Override
	public boolean useOuterInJoins() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#supportGetGeneratedKeyByName()
	 */
	@Override
	public boolean supportGetGeneratedKeyByName() {
		return false;
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
	private static final class SQLiteLimitHandler implements LimitHandler {

		@Override
		public String limitResults(SQLQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

	private static final class SQLiteValueDeserializer implements ValueProcessor {

		@Override
		public Object processValue(Provider<Connection> connection, TypedExpression<?> expression, Object value)
				throws SQLException {
			if (value != null) {
				if (TypeUtils.isDate(expression.getType()) || TypeUtils.isCalendar(expression.getType())
						|| TypeUtils.isTemporal(expression.getType())) {
					if (TypeUtils.isString(value.getClass())) {
						return asDateTime((String) value, expression.getTemporalType().orElse(TemporalType.DATE));
					} else if (TypeUtils.isNumber(value.getClass())) {
						return asDateTime((Number) value);
					}
				}
			}
			return value;
		}

	}

	private static Object asDateTime(String value, TemporalType type) {
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
			// check temporals
			return SQLProcessedParameter.create(parameter.getTemporalType()
					.flatMap(temporalType -> context.getValueSerializer()
							.serializeTemporal(parameter.getValue(), temporalType)
							.map(value -> SQLParameter.create(value)))
					.orElse(parameter));
		}

	}

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
