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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Month;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer.ValueProcessor;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.DialectFunctionsRegistry;
import com.holonplatform.datastore.jdbc.composer.internal.dialect.ReaderToStringParameterResolver;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

import jakarta.annotation.Priority;

/**
 * SQLite {@link SQLDialect}.
 *
 * @since 5.1.0
 */
public class SQLiteDialect implements SQLDialect {

	private static final long serialVersionUID = -2218115258014233676L;

	private final DialectFunctionsRegistry functions = new DialectFunctionsRegistry();

	private static final SQLiteLimitHandler LIMIT_HANDLER = new SQLiteLimitHandler();

	private static final TemporalParameterResolver TEMPORAL_PARAMETER_RESOLVER = new TemporalParameterResolver();

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
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#init(com.holonplatform.datastore.jdbc.
	 * composer. SQLExecutionContext)
	 */
	@Override
	public void init(SQLDialectContext context) throws SQLException {
		DatabaseMetaData databaseMetaData = context.getOrRetrieveDatabaseMetaData().orElse(null);
		if (databaseMetaData != null) {
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			try {
				generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
			} catch (SQLException e) {
				generatedKeyAlwaysReturned = false;
			}
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
		}
		context.getValueDeserializer().addValueProcessor(DESERIALIZER);
		context.addExpressionResolver(ReaderToStringParameterResolver.INSTANCE);
		context.addExpressionResolver(TEMPORAL_PARAMETER_RESOLVER);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLDialect#resolveFunction(com.holonplatform.core.query
	 * .QueryFunction)
	 */
	@Override
	public Optional<SQLFunction> resolveFunction(QueryFunction<?, ?> function) {
		return functions.getFunction(function);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#updateStatementAliasSupported()
	 */
	@Override
	public boolean updateStatementAliasSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect#supportsBinaryStreamParameter()
	 */
	@Override
	public boolean supportsBinaryStreamParameter() {
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
		public String limitResults(SQLQueryDefinition query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" limit " + limit + " offset " + offset) : (" limit " + limit));
		}

	}

	private static final class SQLiteValueDeserializer implements ValueProcessor {

		@Override
		public Object processValue(SQLExecutionContext context, TypedExpression<?> expression, Object value)
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

	@SuppressWarnings({ "rawtypes", "serial" })
	@Priority(Integer.MAX_VALUE - 10000)
	private static final class TemporalParameterResolver
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

			expression.validate();

			if (expression.getValue() != null) {
				if (TypeUtils.isDate(expression.getValue().getClass())
						|| Temporal.class.isAssignableFrom(expression.getValue().getClass())) {
					return context.getValueSerializer()
							.serializeTemporal(expression.getValue(),
									((SQLParameter<?>) expression).getTemporalType().orElse(null))
							.map(value -> SQLParameter.create(value, String.class));
				}
			}

			return Optional.empty();
		}

	}

}
