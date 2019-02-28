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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.holonplatform.core.ConverterExpression;
import com.holonplatform.core.ExpressionValueConverter;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;

/**
 * Default {@link SQLValueDeserializer}.
 *
 * @since 5.1.0
 */
public enum DefaultSQLValueDeserializer implements SQLValueDeserializer {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	private static final Logger LOGGER = SQLComposerLogger.create();

	/**
	 * Additional value processors
	 */
	private final List<ValueProcessor> valueProcessors = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer#addValueProcessor(com.holonplatform.datastore.jdbc
	 * .composer.SQLValueDeserializer.ValueProcessor)
	 */
	@Override
	public void addValueProcessor(ValueProcessor valueProcessor) {
		ObjectUtils.argumentNotNull(valueProcessor, "Value processor must be not null");
		valueProcessors.add(valueProcessor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer#deserialize(com.holonplatform.datastore.jdbc.
	 * composer.SQLExecutionContext, com.holonplatform.core.TypedExpression, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(SQLExecutionContext context, TypedExpression<T> expression,
			final Object valueToDeserialize) throws SQLException {

		ObjectUtils.argumentNotNull(expression, "Value deserialization expression must be not null");

		LOGGER.debug(() -> "<DefaultSQLValueDeserializer> Deserializing value [" + valueToDeserialize + "] of type ["
				+ ((valueToDeserialize == null) ? "NULL" : valueToDeserialize.getClass()) + "] for expression type ["
				+ expression.getType() + "]");

		Object value = valueToDeserialize;

		// apply processors
		for (ValueProcessor processor : valueProcessors) {
			value = processor.processValue(context, expression, value);
			LOGGER.debug(() -> "<DefaultSQLValueDeserializer> Value to deserialize processed by ValueProcessor ["
					+ processor.getClass() + "]");
		}

		// null always deserialized as null
		if (value == null) {
			LOGGER.debug(() -> "<DefaultSQLValueDeserializer> Value to deserialize is NULL, return it as NULL");
			return null;
		}

		// check converter
		final ExpressionValueConverter<?, ?> converter = (expression instanceof ConverterExpression)
				? ((ConverterExpression<?>) expression).getExpressionValueConverter().orElse(null)
				: null;

		// actual type to deserialize
		Class<?> targetType = (converter != null) ? converter.getModelType() : expression.getType();

		LOGGER.debug(() -> "<DefaultSQLValueDeserializer> ExpressionValueConverter "
				+ ((converter != null) ? "detected" : "not detected") + " - deserialization target type: [" + targetType
				+ "]");

		Object deserialized = deserialize(targetType, value);

		if (converter != null) {
			if (deserialized == null || TypeUtils.isAssignable(deserialized.getClass(), converter.getModelType())) {
				deserialized = ((ExpressionValueConverter<Object, Object>) converter).fromModel(deserialized);
			}
		}

		final Object deserializedValue = deserialized;
		LOGGER.debug(() -> "<DefaultSQLValueDeserializer> Deserialized value: [" + deserializedValue + "] - Type: ["
				+ ((deserializedValue == null) ? "NULL" : deserializedValue.getClass()) + "]");

		// check type
		if (TypeUtils.isAssignable(deserializedValue.getClass(), expression.getType())) {
			return (T) deserializedValue;
		} else {
			throw new SQLException("Failed to deserialize value [" + value + "] for required type ["
					+ expression.getType() + "]: Expected expression type [" + expression.getType()
					+ "] but got value type [" + deserializedValue.getClass().getName() + "]");
		}
	}

	/**
	 * Deserialize given <code>value</code> using supported value types.
	 * @param targetType Target type to obtain
	 * @param value Value to deserialize (not null)
	 * @return Optional deserialized value
	 * @throws SQLException If an error occurred
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object deserialize(Class<?> targetType, Object value) throws SQLException {
		// enum
		if (TypeUtils.isEnum(targetType)) {
			return ConversionUtils.convertEnumValue((Class<Enum>) targetType, value);
		}

		// number
		if (TypeUtils.isNumber(targetType) && TypeUtils.isNumber(value.getClass())) {
			return ConversionUtils.convertNumberToTargetClass((Number) value, (Class<Number>) targetType);
		}

		// date and times
		if (Date.class.isAssignableFrom(value.getClass())) {
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDate((Date) value);
			}
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDateTime((Date) value);
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalTime((Date) value);
			}
		}

		if (Timestamp.class.isAssignableFrom(value.getClass())) {
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime();
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime().toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime().toLocalTime();
			}
			if (java.util.Date.class.isAssignableFrom(targetType)) {
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(((Timestamp) value).getTime());
				return c.getTime();
			}
		}

		if (Time.class.isAssignableFrom(value.getClass())) {
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((Time) value).toLocalTime();
			}
		}

		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return Date.valueOf(((LocalDate) value));
			}
		}
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return new Date(Timestamp.valueOf(((LocalDateTime) value)).getTime());
			}
			if (Timestamp.class.isAssignableFrom(targetType)) {
				return Timestamp.valueOf(((LocalDateTime) value));
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((LocalDateTime) value).toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((LocalDateTime) value).toLocalTime();
			}
		}
		if (OffsetDateTime.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return new Date(Timestamp.valueOf(((OffsetDateTime) value).toLocalDateTime()).getTime());
			}
			if (Timestamp.class.isAssignableFrom(targetType)) {
				return Timestamp.valueOf(((OffsetDateTime) value).toLocalDateTime());
			}
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalDateTime();
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalTime();
			}
		}

		// String to Reader
		if (TypeUtils.isString(value.getClass()) && Reader.class.isAssignableFrom(targetType)) {
			return new StringReader((String) value);
		}

		// Byte[] to InputStream
		if (value instanceof byte[] && InputStream.class.isAssignableFrom(targetType)) {
			return new ByteArrayInputStream((byte[]) value);
		}

		// clob

		if (Clob.class.isAssignableFrom(value.getClass())) {
			// as Reader
			if (Reader.class.isAssignableFrom(targetType)) {
				return new StringReader(clobToString((Clob) value));
			}
			// as String
			if (String.class.isAssignableFrom(targetType)) {
				return clobToString((Clob) value);
			}
		}

		// blob
		if (Blob.class.isAssignableFrom(value.getClass())) {
			// as InputStream
			if (InputStream.class.isAssignableFrom(targetType)) {
				return new ByteArrayInputStream(blobToBytes((Blob) value));
				// return ((Blob) value).getBinaryStream();
			}
			// as byte[]
			if (byte[].class.isAssignableFrom(targetType)) {
				return blobToBytes((Blob) value);
			}
		}

		return value;
	}

	/**
	 * Convert given CLOB value into a String.
	 * @param value Clob value
	 * @return Clob content as String
	 * @throws SQLException If a SQL error occurred
	 * @throws IOException If an IO error occurred
	 */
	private static final String clobToString(final Clob value) throws SQLException {
		try {
			final StringBuilder sb = new StringBuilder();
			final Reader reader = value.getCharacterStream();
			try (BufferedReader br = new BufferedReader(reader)) {
				int b;
				while (-1 != (b = br.read())) {
					sb.append((char) b);
				}
			}
			return sb.toString();
		} catch (IOException ioe) {
			throw new SQLException("Falied to read Clob contents", ioe);
		} finally {
			try {
				value.free();
			} catch (@SuppressWarnings("unused") SQLFeatureNotSupportedException e) {
				// ignore
			}
		}
	}

	/**
	 * Convert given BLOB value into a byte array.
	 * @param blob Blob to convert
	 * @return Blob content as byte array
	 * @throws SQLException If a SQL error occurred
	 */
	private static final byte[] blobToBytes(final Blob blob) throws SQLException {
		try {
			try {
				return blob.getBytes(1, (int) blob.length());
			} finally {
				try {
					blob.free();
				} catch (@SuppressWarnings("unused") SQLFeatureNotSupportedException fnse) {
					// ignore
				}
			}
		} catch (@SuppressWarnings("unused") SQLFeatureNotSupportedException e) {
			// not supported
			try (InputStream is = blob.getBinaryStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
				int nRead;
				byte[] data = new byte[16384];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
				return buffer.toByteArray();
			} catch (IOException ioe) {
				throw new DataAccessException("Error reading CLOB type value", ioe);
			} finally {
				try {
					blob.free();
				} catch (@SuppressWarnings("unused") SQLFeatureNotSupportedException fnse) {
					// ignore
				}
			}
		}
	}

}
