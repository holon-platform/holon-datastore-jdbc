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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DatastoreOperations.WriteOption;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.internal.context.StatementExecutionContext;

/**
 * JDBC query utils.
 * 
 * @since 5.0.0
 */
public final class JdbcDatastoreUtils implements Serializable {

	private static final long serialVersionUID = 6908756408186059733L;

	/*
	 * Empty private constructor: this class is intended only to provide constants ad utility methods.
	 */
	private JdbcDatastoreUtils() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static QueryFilter getPrimaryKeyFilter(StatementExecutionContext context, SQLPrimaryKey primaryKey,
			PropertyBox propertyBox) {

		if (primaryKey.getPaths() == null || primaryKey.getPaths().length == 0) {
			throw new DataAccessException("Invalid primary key: no paths available");
		}

		final PathPropertyBoxAdapter adapter = PathPropertyBoxAdapter.builder(propertyBox)
				.pathMatcher(new DialectPathMatcher(context.getDialect())).build();

		List<QueryFilter> filters = new LinkedList<>();
		for (Path path : primaryKey.getPaths()) {
			Optional<Object> value = adapter.getValue(path);
			if (!value.isPresent()) {
				throw new DataAccessException("Primary key path [" + path + "] value not available in PropertyBox");
			}
			filters.add(QueryFilter.eq(path, value.get()));
		}
		return QueryFilter.allOf(filters)
				.orElseThrow(() -> new DataAccessException("Invalid table primary key: no paths available"));
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
	 * Get the Java type which corresponds to the SQL type of the table column with given <code>columnName</code> using
	 * the JDBC database metadata.
	 * @param databaseMetaData Database metadata
	 * @param tableName Table name
	 * @param columnName Column name
	 * @return Column Java type, or <code>null</code> if column was not found
	 * @throws SQLException If an error occurred
	 */
	public static Class<?> getColumnType(DatabaseMetaData databaseMetaData, String tableName, String columnName)
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
		if (type == null || Void.class.isAssignableFrom(type)) {
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

}
