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
package com.holonplatform.datastore.jdbc.internal;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverHandler;
import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.core.ExpressionResolver.ResolutionContext;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.WriteOption;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction;
import com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurationException;
import com.holonplatform.datastore.jdbc.internal.dialect.DefaultSQLFunctions;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.support.DefaultPreparedSql;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;
import com.holonplatform.datastore.jdbc.internal.support.PreparedSql;

/**
 * JDBC query utils.
 * 
 * @since 5.0.0
 */
public final class JdbcDatastoreUtils implements Serializable {

	private static final long serialVersionUID = 6908756408186059733L;

	/**
	 * Logger
	 */
	private final static Logger LOGGER = JdbcDatastoreLogger.create();

	/*
	 * Empty private constructor: this class is intended only to provide constants ad utility methods.
	 */
	private JdbcDatastoreUtils() {
	}

	/**
	 * Get the {@link SQLFunction} implementation to use for given function <code>name</code>.
	 * @param name Function name
	 * @param dialect Jdbc dialect
	 * @return {@link SQLFunction} implementation to use for given function <code>name</code>, if available
	 */
	public static Optional<SQLFunction> getSQLFunction(String name, JdbcDialect dialect) {
		SQLFunction dialectFunction = dialect.getFunction(name);
		if (dialectFunction != null) {
			return Optional.of(dialectFunction);
		}
		return DefaultSQLFunctions.getFunction(name);
	}

	/**
	 * Resolve given <code>expression</code> using given <code>resolver</code> to obtain a <code>resolutionType</code>
	 * type expression. If no {@link ExpressionResolver} is available to resolve given expression, an
	 * {@link InvalidExpressionException} is thrown. The resolved expression is validate using
	 * {@link Expression#validate()} before returning it to caller.
	 * @param <E> Expression type
	 * @param <R> Resolution type
	 * @param resolver {@link ExpressionResolverHandler}
	 * @param expression Expression to resolve
	 * @param resolutionType Expression type to obtain
	 * @param context Resolution context
	 * @return Resolved expression
	 * @throws InvalidExpressionException If an error occurred during resolution, or if no {@link ExpressionResolver} is
	 *         available to resolve given expression or if expression validation failed
	 */
	public static <E extends Expression, R extends Expression> R resolveExpression(ExpressionResolverHandler resolver,
			E expression, Class<R> resolutionType, ResolutionContext context) throws InvalidExpressionException {
		// resolve
		R resolved = resolver.resolve(expression, resolutionType, context).map(e -> {
			// validate
			e.validate();
			return e;
		}).orElse(null);
		// check
		if (resolved == null) {
			LOGGER.debug(() -> "No ExpressionResolver available to resolve expression [" + expression + "]");
			if (resolver instanceof ExpressionResolverSupport) {
				LOGGER.debug(() -> "Available ExpressionResolvers: "
						+ ((ExpressionResolverSupport) resolver).getExpressionResolvers());
			}
			throw new InvalidExpressionException("Failed to resolve expression [" + expression + "]");
		}
		return resolved;
	}

	/**
	 * Perform SQL preparation, replacing context named parameters with JDBC parameter character.
	 * @param sql SQL to prepare
	 * @param context Resolution context
	 * @return Prepared sql
	 * @throws StatementConfigurationException If an error occurred
	 */
	public static PreparedSql prepareSql(String sql, JdbcResolutionContext context)
			throws StatementConfigurationException {

		if (sql == null) {
			throw new StatementConfigurationException("Null sql");
		}
		if (context == null) {
			throw new StatementConfigurationException("Null context");
		}

		final Map<String, ParameterValue> namedParameters = context.getNamedParameters();

		if (!namedParameters.isEmpty()) {

			char[] chars = sql.toCharArray();
			final int length = chars.length;

			StringBuilder sb = new StringBuilder();

			List<ParameterValue> parameters = new ArrayList<>(namedParameters.size());

			for (int i = 0; i < length; i++) {
				if (chars[i] == ':' && (length - i) >= 7) {
					String namedParameter = String.valueOf(Arrays.copyOfRange(chars, i, i + 7));
					if (namedParameters.containsKey(namedParameter)) {
						sb.append('?');
						parameters.add(namedParameters.get(namedParameter));
						i = i + 6;
						continue;
					}
				}
				sb.append(chars[i]);
			}

			return new DefaultPreparedSql(sb.toString(), parameters);

		}

		return new DefaultPreparedSql(sql, Collections.emptyList());

	}

	/**
	 * Try to obtain the {@link TemporalType} of given <code>expression</code>, if the expression type is a temporal
	 * type.
	 * @param expression Query expression
	 * @param treatDateTypeAsDate <code>true</code> to return {@link TemporalType#DATE} for {@link Date} type if
	 *        temporal information is not available
	 * @return The expression {@link TemporalType}, empty if not available or applicable
	 */
	public static Optional<TemporalType> getTemporalType(Expression expression, boolean treatDateTypeAsDate) {
		if (expression != null) {
			Class<?> type = null;
			if (Path.class.isAssignableFrom(expression.getClass())) {
				type = ((Path<?>) expression).getType();
			} else if (QueryExpression.class.isAssignableFrom(expression.getClass())) {
				type = ((QueryExpression<?>) expression).getType();
			}

			if (type != null) {
				if (LocalDate.class.isAssignableFrom(type) || ChronoLocalDate.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.DATE);
				}
				if (LocalTime.class.isAssignableFrom(type) || OffsetTime.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.TIME);
				}
				if (LocalDateTime.class.isAssignableFrom(type) || OffsetDateTime.class.isAssignableFrom(type)
						|| ZonedDateTime.class.isAssignableFrom(type)
						|| ChronoLocalDateTime.class.isAssignableFrom(type)) {
					return Optional.of(TemporalType.DATE);
				}

				if (Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
					if (Property.class.isAssignableFrom(expression.getClass())) {
						Optional<TemporalType> tt = ((Property<?>) expression).getConfiguration().getTemporalType();
						return treatDateTypeAsDate ? Optional.of(tt.orElse(TemporalType.DATE)) : tt;
					} else {
						return treatDateTypeAsDate ? Optional.of(TemporalType.DATE) : Optional.empty();
					}
				}

			}
		}
		return Optional.empty();
	}

	/**
	 * Checks if the {@link DefaultWriteOption#BRING_BACK_GENERATED_IDS} is present among given write options.
	 * @param options Write options
	 * @return <code>true</code> if the {@link DefaultWriteOption#BRING_BACK_GENERATED_IDS} is present
	 */
	public static boolean isBringBackGeneratedIds(WriteOption[] options) {
		if (options != null) {
			for (WriteOption option : options) {
				if (DefaultWriteOption.BRING_BACK_GENERATED_IDS == option) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the primary key {@link Path}s of given <code>table</code> using database metadata, if available.
	 * @param dialect Jdbc dialect
	 * @param table Table name for which to obtain th primary key
	 * @param connection Connection to use
	 * @return The table primary key {@link Path}s, empty if not available
	 * @throws SQLException Error accessing the database
	 */
	public static Optional<Path<?>[]> getPrimaryKey(JdbcDialect dialect, String table, Connection connection)
			throws SQLException {
		ObjectUtils.argumentNotNull(table, "Table name must be not null");
		ObjectUtils.argumentNotNull(connection, "Connection name must be not null");

		final String tableName = dialect.getTableName(table);

		List<OrderedPath> paths = new ArrayList<>();

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
			while (resultSet.next()) {
				final String columnName = resultSet.getString("COLUMN_NAME");
				OrderedPath op = new OrderedPath();
				op.path = Path.of(columnName, getColumnType(databaseMetaData, tableName, columnName));
				op.sequence = resultSet.getShort("KEY_SEQ");
				paths.add(op);
			}
		}

		if (!paths.isEmpty()) {
			Collections.sort(paths);
			return Optional
					.of(paths.stream().map(p -> p.path).collect(Collectors.toList()).toArray(new Path[paths.size()]));
		}

		return Optional.empty();
	}

	/**
	 * Get the Java type which corresponds to the SQL type of the table column with given <code>columnName</code> using
	 * the JDBC database metadata.
	 * @param databaseMetaData Database metadata
	 * @param tableName Table name
	 * @param columnName Column name
	 * @return Column Java type, or <code>null</code> if column was not found
	 * @throws SQLException If an error occurred
	 */
	private static Class<?> getColumnType(DatabaseMetaData databaseMetaData, String tableName, String columnName)
			throws SQLException {
		try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, columnName)) {
			if (rs.next()) {
				return jdbcTypeToClass(rs.getInt("DATA_TYPE"));
			}
		}
		return null;
	}

	/**
	 * Get the Java type which corresponds to given SQL type.
	 * @param type SQL type
	 * @return The Java type
	 */
	public static Class<?> jdbcTypeToClass(int type) {
		Class<?> result = Object.class;

		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			result = String.class;
			break;

		case Types.NUMERIC:
		case Types.DECIMAL:
			result = java.math.BigDecimal.class;
			break;

		case Types.BIT:
			result = Boolean.class;
			break;

		case Types.TINYINT:
			result = Byte.class;
			break;

		case Types.SMALLINT:
			result = Short.class;
			break;

		case Types.INTEGER:
			result = Integer.class;
			break;

		case Types.BIGINT:
			result = Long.class;
			break;

		case Types.REAL:
		case Types.FLOAT:
			result = Float.class;
			break;

		case Types.DOUBLE:
			result = Double.class;
			break;

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			result = byte[].class;
			break;

		case Types.DATE:
			result = java.sql.Date.class;
			break;

		case Types.TIME:
			result = java.sql.Time.class;
			break;

		case Types.TIMESTAMP:
			result = java.sql.Timestamp.class;
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * Get the SQL type which corresponds to given Java type.
	 * @param type Java type
	 * @return SQL type
	 */
	public static int classToJdbcType(Class<?> type) {
		if (type == null) {
			return Types.NULL;
		}

		if (String.class.isAssignableFrom(type)) {
			return Types.VARCHAR;
		}

		if (TypeUtils.isBoolean(type)) {
			return Types.BOOLEAN;
		}

		if (TypeUtils.isInteger(type)) {
			return Types.INTEGER;
		}
		if (TypeUtils.isLong(type)) {
			return Types.BIGINT;
		}
		if (TypeUtils.isDouble(type) || TypeUtils.isFloat(type) || TypeUtils.isBigDecimal(type)) {
			return Types.DECIMAL;
		}
		if (TypeUtils.isShort(type)) {
			return Types.SMALLINT;
		}
		if (TypeUtils.isByte(type)) {
			return Types.TINYINT;
		}
		if (TypeUtils.isNumber(type)) {
			return Types.NUMERIC;
		}

		if (type == byte[].class) {
			return Types.BINARY;
		}

		if (TypeUtils.isDate(type) || TypeUtils.isCalendar(type) || java.sql.Date.class.isAssignableFrom(type)
				|| LocalDate.class.isAssignableFrom(type)) {
			return Types.DATE;
		}
		if (LocalTime.class.isAssignableFrom(type) || java.sql.Time.class.isAssignableFrom(type)) {
			return Types.TIME;
		}
		if (LocalDateTime.class.isAssignableFrom(type) || java.sql.Timestamp.class.isAssignableFrom(type)) {
			return Types.TIMESTAMP;
		}
		if (ZonedDateTime.class.isAssignableFrom(type)) {
			return Types.TIMESTAMP_WITH_TIMEZONE;
		}

		return Types.OTHER;
	}

	/**
	 * Support class to order {@link Path}s using a sequence.
	 */
	private static class OrderedPath implements Comparable<OrderedPath> {

		@SuppressWarnings("rawtypes")
		Path path;
		short sequence;

		@Override
		public int compareTo(OrderedPath o) {
			return ((Short) sequence).compareTo(o.sequence);
		}

	}

}
