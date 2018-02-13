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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import com.holonplatform.core.internal.utils.CalendarUtils;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.streams.LimitedInputStream;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementConfigurator;
import com.holonplatform.datastore.jdbc.composer.SQLType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;

/**
 * Default {@link SQLStatementConfigurator}.
 *
 * @since 5.1.0
 */
public enum DefaultSQLStatementConfigurator implements SQLStatementConfigurator {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLStatementConfigurator#configureStatement(com.holonplatform.datastore
	 * .jdbc.composer.SQLContext, java.sql.PreparedStatement,
	 * com.holonplatform.datastore.jdbc.composer.expression.SQLStatement)
	 */
	@Override
	public PreparedStatement configureStatement(SQLContext context, PreparedStatement jdbcStatement,
			SQLStatement sqlStatement) throws SQLException {
		ObjectUtils.argumentNotNull(context, "SQLContext must be not null");
		ObjectUtils.argumentNotNull(jdbcStatement, "PreparedStatement must be not null");
		ObjectUtils.argumentNotNull(sqlStatement, "SQLStatement must be not null");

		// statement parameters
		final SQLParameter<?>[] parameters = sqlStatement.getParameters();

		for (int i = 0; i < parameters.length; i++) {
			final SQLParameter<?> parameter = parameters[i];

			// check null
			if (parameter.getValue() == null) {
				setNullStatementParameterValue(context, jdbcStatement, (i + 1), parameter.getType());
			} else {
				setStatementParameterValue(context, jdbcStatement, (i + 1), parameter.getType(), parameter.getValue(),
						parameter.getTemporalType().orElse(null));
			}
		}

		return jdbcStatement;
	}

	/**
	 * Set a <code>null</code> statement parameter value.
	 * @param context SQL context
	 * @param jdbcStatement The JDBC statement
	 * @param parameterIndex Statement parameter index
	 * @param type Statement parameter type
	 * @throws SQLException If an error occurred
	 */
	private static void setNullStatementParameterValue(SQLContext context, PreparedStatement jdbcStatement,
			int parameterIndex, Class<?> type) throws SQLException {
		Optional<SQLType> sqlType = context.getTypeConverter().getSqlType(context, type);
		if (!sqlType.isPresent()) {
			// try to use NULL type
			if (context.getDialect().supportsSqlType(Types.NULL)) {
				sqlType = Optional.of(SQLType.create(Types.NULL));
			} else {
				throw new SQLException("Failed to set null statement parameter for value type [" + type
						+ "]: no SQL type can be resolved and dialect does not support NULL sql type");
			}
		}

		SQLType parameterType = sqlType.orElseThrow(() -> new SQLException(
				"Failed to set statement parameter for value type [" + type + "]: no SQL type can be resolved"));

		// set null value
		if (parameterType.getName().isPresent()) {
			jdbcStatement.setNull(parameterIndex, parameterType.getType(), parameterType.getName().get());
		} else {
			jdbcStatement.setNull(parameterIndex, parameterType.getType());
		}
	}

	/**
	 * Set a statement parameter value.
	 * @param context SQL context
	 * @param jdbcStatement The JDBC statement
	 * @param parameterIndex Statement parameter index
	 * @param type Statement parameter type
	 * @param value Statement parameter value (not null)
	 * @param temporalType Optional value temporal type
	 * @return Actual parameter value
	 * @throws SQLException If an error occurred
	 */
	private static Object setStatementParameterValue(SQLContext context, PreparedStatement jdbcStatement,
			int parameterIndex, Class<?> type, Object value, TemporalType temporalType) throws SQLException {

		// CharSequence
		if (TypeUtils.isCharSequence(type)) {
			final String str = value.toString();
			jdbcStatement.setString(parameterIndex, str);
			return str;
		}

		// boolean
		if (TypeUtils.isBoolean(type)) {
			jdbcStatement.setBoolean(parameterIndex, (Boolean) value);
			return value;
		}

		// Numbers
		if (TypeUtils.isInteger(type)) {
			// check value type matches
			jdbcStatement.setInt(parameterIndex, checkNumericValue(value, Integer.class));
			return value;
		}
		if (TypeUtils.isLong(type)) {
			jdbcStatement.setLong(parameterIndex, checkNumericValue(value, Long.class));
			return value;
		}
		if (TypeUtils.isDouble(type)) {
			jdbcStatement.setDouble(parameterIndex, checkNumericValue(value, Double.class));
			return value;
		}
		if (TypeUtils.isFloat(type)) {
			jdbcStatement.setFloat(parameterIndex, checkNumericValue(value, Float.class));
			return value;
		}
		if (TypeUtils.isShort(type)) {
			jdbcStatement.setShort(parameterIndex, checkNumericValue(value, Short.class));
			return value;
		}
		if (Byte.class.isAssignableFrom(type)) {
			jdbcStatement.setByte(parameterIndex, checkNumericValue(value, Byte.class));
			return value;
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			jdbcStatement.setBigDecimal(parameterIndex, checkNumericValue(value, BigDecimal.class));
			return value;
		}
		if (BigInteger.class.isAssignableFrom(type)) {
			jdbcStatement.setLong(parameterIndex, checkNumericValue(value, Long.class));
			return value;
		}

		// Enum (by default, ordinal value is used)
		if (TypeUtils.isEnum(type)) {
			Enum<?> enumValue = checkEnumValue(value, type);
			jdbcStatement.setInt(parameterIndex, enumValue.ordinal());
			return enumValue;
		}

		// Byte[]
		if (type == byte[].class) {
			jdbcStatement.setBytes(parameterIndex, (byte[]) value);
			return value;
		}

		// Dates and times
		if (java.sql.Date.class.isAssignableFrom(type)) {
			if (java.sql.Date.class.isAssignableFrom(value.getClass())) {
				jdbcStatement.setDate(parameterIndex, (java.sql.Date) value);
				return value;
			}
		}
		if (TypeUtils.isDate(type) || TypeUtils.isCalendar(type)) {
			final Date dateValue = asDate(value);
			if (dateValue != null) {
				if (temporalType != null && temporalType == TemporalType.DATE_TIME) {
					final java.sql.Timestamp ts = new java.sql.Timestamp(dateValue.getTime());
					jdbcStatement.setTimestamp(parameterIndex, ts);
					return ts;
				}
				if (temporalType != null && temporalType == TemporalType.TIME) {
					final java.sql.Time t = new java.sql.Time(dateValue.getTime());
					jdbcStatement.setTime(parameterIndex, t);
					return t;
				}
				if (temporalType != null && temporalType == TemporalType.DATE) {
					Date truncated = CalendarUtils.floorTime(dateValue);
					jdbcStatement.setDate(parameterIndex, new java.sql.Date(truncated.getTime()));
					return truncated;
				}
				jdbcStatement.setDate(parameterIndex, new java.sql.Date(dateValue.getTime()));
				return dateValue;
			}
		}

		if (LocalDate.class.isAssignableFrom(type)) {
			final LocalDate ld = asLocalDate(value);
			if (ld != null) {
				final java.sql.Date d = java.sql.Date.valueOf(ld);
				jdbcStatement.setDate(parameterIndex, d);
				return d;
			}
		}
		if (LocalTime.class.isAssignableFrom(type)) {
			final LocalTime lt = asLocalTime(value);
			if (lt != null) {
				final java.sql.Time t = java.sql.Time.valueOf(lt);
				jdbcStatement.setTime(parameterIndex, t);
				return t;
			}
		}
		if (LocalDateTime.class.isAssignableFrom(type)) {
			final LocalDateTime ldt = asLocalDateTime(value);
			if (ldt != null) {
				final java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ldt);
				jdbcStatement.setTimestamp(parameterIndex, ts);
				return ts;
			}
		}

		// Reader (CharacterStream)
		if (Reader.class.isAssignableFrom(type)) {
			jdbcStatement.setCharacterStream(parameterIndex, (Reader) value);
			return value;
		}

		// Blob
		if (Blob.class.isAssignableFrom(type)) {
			jdbcStatement.setBlob(parameterIndex, (Blob) value);
			return value;
		}

		// Clob
		if (Clob.class.isAssignableFrom(type)) {
			jdbcStatement.setClob(parameterIndex, (Clob) value);
			return value;
		}

		// File
		if (File.class.isAssignableFrom(type)) {
			final File file = (File) value;
			try (FileInputStream fis = new FileInputStream(file)) {
				jdbcStatement.setBinaryStream(parameterIndex, fis, file.length());
				return file;
			} catch (IOException e) {
				throw new SQLException("Failed to read File [" + file + "]", e);
			}
		}

		// streams
		if (InputStream.class.isAssignableFrom(type)) {
			if (value instanceof LimitedInputStream) {
				final LimitedInputStream lis = (LimitedInputStream) value;

				if (context.getDialect().supportsBinaryStreamParameter()) {
					jdbcStatement.setBinaryStream(parameterIndex, lis.getActualStream(), lis.getLength());
					return lis;
				} else {
					try {
						jdbcStatement.setBytes(parameterIndex, ConversionUtils
								.convertInputStreamToBytes(((LimitedInputStream) value).getActualStream()));
					} catch (IOException e) {
						throw new SQLException("Failed to convert InputStream to bytes", e);
					}
					return value;
				}
			}
			if (value instanceof ByteArrayInputStream) {
				try {
					jdbcStatement.setBytes(parameterIndex,
							ConversionUtils.convertInputStreamToBytes((ByteArrayInputStream) value));
				} catch (IOException e) {
					throw new SQLException("Failed to convert ByteArrayInputStream to bytes", e);
				}
				return value;
			}
		}

		// default

		Optional<SQLType> sqlType = context.getTypeConverter().getSqlType(context, type);
		if (sqlType.isPresent()) {
			jdbcStatement.setObject(parameterIndex, value, sqlType.get().getType());
			return value;
		}

		// generic object
		jdbcStatement.setObject(parameterIndex, value);
		return value;

	}

	@SuppressWarnings("unchecked")
	private static <N extends Number> N checkNumericValue(Object value, Class<N> type) throws SQLException {
		if (!TypeUtils.isNumber(value.getClass())) {
			throw new SQLException("Cannot set a numeric parameter type [" + type
					+ "] using a parameter value which is not a number: [" + value + "]");
		}
		if (!TypeUtils.isAssignable(value.getClass(), type)) {
			// try to convert
			return ConversionUtils.convertNumberToTargetClass((Number) value, type);
		}
		return (N) value;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Enum<?> checkEnumValue(Object value, Class<?> type) {
		if (Enum.class.isAssignableFrom(value.getClass())) {
			return (Enum<?>) value;
		} else {
			return ConversionUtils.convertEnumValue((Class<Enum>) type, value);
		}
	}

	private static Date asDate(Object value) {
		if (TypeUtils.isDate(value.getClass())) {
			return (Date) value;
		}
		if (java.sql.Date.class.isAssignableFrom(value.getClass())) {
			return ((java.sql.Date) value);
		}
		if (TypeUtils.isCalendar(value.getClass())) {
			return ((Calendar) value).getTime();
		}
		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			return ConversionUtils.fromLocalDate((LocalDate) value);
		}
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			return ConversionUtils.fromLocalDateTime((LocalDateTime) value);
		}
		return null;
	}

	private static LocalDate asLocalDate(Object value) {
		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			return (LocalDate) value;
		}
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			return LocalDate.of(((LocalDateTime) value).getYear(), ((LocalDateTime) value).getMonth(),
					((LocalDateTime) value).getDayOfMonth());
		}
		if (TypeUtils.isDate(value.getClass())) {
			return ConversionUtils.toLocalDate((Date) value);
		}
		if (TypeUtils.isCalendar(value.getClass())) {
			return ConversionUtils.toLocalDate((Calendar) value);
		}
		return null;
	}

	private static LocalDateTime asLocalDateTime(Object value) {
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			return (LocalDateTime) value;
		}
		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			return LocalDateTime.of(((LocalDate) value).getYear(), ((LocalDate) value).getMonth(),
					((LocalDate) value).getDayOfMonth(), 0, 0);
		}
		if (TypeUtils.isDate(value.getClass())) {
			return ConversionUtils.toLocalDateTime((Date) value);
		}
		if (TypeUtils.isCalendar(value.getClass())) {
			return ConversionUtils.toLocalDateTime((Calendar) value);
		}
		return null;
	}

	private static LocalTime asLocalTime(Object value) {
		if (LocalTime.class.isAssignableFrom(value.getClass())) {
			return (LocalTime) value;
		}
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			return LocalTime.of(((LocalDateTime) value).getHour(), ((LocalDateTime) value).getMinute(),
					((LocalDateTime) value).getSecond());
		}
		if (TypeUtils.isDate(value.getClass())) {
			return ConversionUtils.toLocalTime((Date) value);
		}
		return null;
	}

}
