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
package com.holonplatform.datastore.jdbc.internal.dialect;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurationException;
import com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurator;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * Default {@link StatementConfigurator} implementation.
 *
 * @since 5.0.0
 */
public class DefaultStatementConfigurator implements StatementConfigurator {

	/**
	 * Dialect to use
	 */
	private final JdbcDialect dialect;

	/**
	 * Constructor
	 * @param dialect Dialect to use (not null)
	 */
	public DefaultStatementConfigurator(JdbcDialect dialect) {
		super();
		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		this.dialect = dialect;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurator#configureStatement(java.sql.Connection,
	 * java.sql.PreparedStatement, java.lang.String, java.util.List)
	 */
	@Override
	public void configureStatement(Connection connection, PreparedStatement statement, String sql,
			List<ParameterValue> parameterValues) throws StatementConfigurationException {
		for (int i = 0; i < parameterValues.size(); i++) {
			final int index = (i + 1);
			final ParameterValue parameterValue = parameterValues.get(i);
			try {
				if (dialect.getStatementParameterHandler().isPresent()) {
					if (dialect.getStatementParameterHandler().get()
							.setParameterValue(connection, statement, index, parameterValue).isPresent()) {
						continue;
					}
				}
				// default
				setParameterValue(statement, index, parameterValue);
			} catch (Exception e) {
				throw new StatementConfigurationException("Failed to configure statement for SQL [" + sql + "]", e);
			}
		}
	}

	/**
	 * Set the value of given {@link ParameterValue} representation into the given <code>statement</code> at given
	 * <code>index</code>.
	 * @param statement Prepared statement for which to set the parameter value
	 * @param index Parameter index
	 * @param parameterValue Parameter value representation
	 * @throws SQLException If an error occurred
	 */
	private static void setParameterValue(PreparedStatement statement, int index, ParameterValue parameterValue)
			throws SQLException {
		if (parameterValue.getValue() != null) {
			setNotNullParameterValue(statement, index, parameterValue);
		} else {
			statement.setNull(index, JdbcDatastoreUtils.classToJdbcType(parameterValue.getType()));
		}
	}

	/**
	 * Set a not null parameter value using given {@link ParameterValue} representation into the given
	 * <code>statement</code> at given <code>index</code>.
	 * @param statement Prepared statement for which to set the parameter value
	 * @param index Parameter index
	 * @param parameterValue Parameter value representation
	 * @return The setted parameter value
	 * @throws SQLException If an error occurred
	 */
	@SuppressWarnings("unchecked")
	private static Object setNotNullParameterValue(PreparedStatement statement, int index,
			ParameterValue parameterValue) throws SQLException {

		final Class<?> type = parameterValue.getType();
		Object pvalue = parameterValue.getValue();

		// check type
		if (!TypeUtils.isAssignable(pvalue.getClass(), type)) {
			if (TypeUtils.isNumber(pvalue.getClass()) && TypeUtils.isNumber(type)) {
				pvalue = ConversionUtils.convertNumberToTargetClass((Number) pvalue, (Class<Number>) type);
			}
		}

		final Object value = pvalue;

		// CharSequence
		if (TypeUtils.isCharSequence(type)) {
			final String str = value.toString();
			statement.setString(index, str);
			return str;
		}

		// Numbers

		if (TypeUtils.isInteger(type)) {
			statement.setInt(index, (int) value);
			return value;
		}
		if (TypeUtils.isLong(type)) {
			statement.setLong(index, (long) value);
			return value;
		}
		if (TypeUtils.isDouble(type)) {
			statement.setDouble(index, (double) value);
			return value;
		}
		if (TypeUtils.isFloat(type)) {
			statement.setFloat(index, (float) value);
			return value;
		}
		if (TypeUtils.isShort(type)) {
			statement.setShort(index, (short) value);
			return value;
		}
		if (Byte.class.isAssignableFrom(type) || int.class == type) {
			statement.setByte(index, (byte) value);
			return value;
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			statement.setBigDecimal(index, (BigDecimal) value);
			return value;
		}

		// Enum (by default, ordinal value is used)
		if (TypeUtils.isEnum(value.getClass())) {
			final int ordinal = ((Enum<?>) value).ordinal();
			statement.setInt(index, ordinal);
			return ordinal;
		}

		// Byte[]
		if (value instanceof byte[]) {
			statement.setBytes(index, (byte[]) value);
			return value;
		}

		// Date and times
		if (java.sql.Date.class.isAssignableFrom(type)) {
			statement.setDate(index, (java.sql.Date) value);
			return value;
		}
		if (TypeUtils.isDate(type)) {
			TemporalType tt = parameterValue.getTemporalType().orElse(TemporalType.DATE);
			if (tt == TemporalType.DATE_TIME) {
				final java.sql.Timestamp ts = new java.sql.Timestamp(((java.util.Date) value).getTime());
				statement.setTimestamp(index, ts);
				return ts;
			}
			if (tt == TemporalType.TIME) {
				final java.sql.Time t = new java.sql.Time(((java.util.Date) value).getTime());
				statement.setTime(index, t);
				return t;
			}

			final java.sql.Date d = new java.sql.Date(((java.util.Date) value).getTime());
			statement.setDate(index, d);
			return d;
		}
		if (TypeUtils.isCalendar(type)) {
			TemporalType tt = parameterValue.getTemporalType().orElse(TemporalType.DATE);
			if (tt == TemporalType.DATE_TIME) {
				final java.sql.Timestamp ts = new java.sql.Timestamp(((Calendar) value).getTimeInMillis());
				statement.setTimestamp(index, ts);
				return ts;
			}
			if (tt == TemporalType.TIME) {
				final java.sql.Time t = new java.sql.Time(((Calendar) value).getTimeInMillis());
				statement.setTime(index, t);
				return t;
			}

			final java.sql.Date d = new java.sql.Date(((Calendar) value).getTimeInMillis());
			statement.setDate(index, d, (Calendar) value);
			return d;
		}

		if (LocalDate.class.isAssignableFrom(type)) {
			final java.sql.Date d = java.sql.Date.valueOf(((LocalDate) value));
			statement.setDate(index, d);
			return d;
		}
		if (LocalTime.class.isAssignableFrom(type)) {
			final java.sql.Time t = java.sql.Time.valueOf((LocalTime) value);
			statement.setTime(index, t);
			return t;
		}
		if (LocalDateTime.class.isAssignableFrom(type)) {
			final java.sql.Timestamp ts = java.sql.Timestamp.valueOf((LocalDateTime) value);
			statement.setTimestamp(index, ts);
			return ts;
		}

		// Reader (CharacterStream)
		if (Reader.class.isAssignableFrom(type)) {
			statement.setCharacterStream(index, (Reader) value);
			return value;
		}

		// default
		statement.setObject(index, value);
		return value;
	}

}
