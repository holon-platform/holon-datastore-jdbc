/*
 * Copyright 2000-2016 Holon TDCN.
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
import java.util.Calendar;
import java.util.Optional;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLValueDeserializer;

/**
 * Default {@link SQLValueDeserializer}.
 *
 * @since 5.0.0
 */
public enum DefaultSQLValueDeserializer implements SQLValueDeserializer {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.JdbcDialect.SQLValueDeserializer#deserializeValue(com.holonplatform.core.query.
	 * QueryExpression, java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> T deserializeValue(QueryExpression<T> expression, Object value) {
		if (value != null) {

			Class<?> targetType = expression.getType();

			// check property converter for model type
			Optional<PropertyValueConverter> converter = Optional.empty();
			if (expression instanceof Property) {
				converter = ((Property) expression).getConverter();
				targetType = converter.map(c -> c.getModelType()).orElse(targetType);
			}

			Object deserialized = deserialize(targetType, value);

			if (converter.isPresent()) {
				if (deserialized == null
						|| TypeUtils.isAssignable(deserialized.getClass(), converter.get().getModelType())) {
					deserialized = converter.get().fromModel(deserialized, (Property) expression);
				}
			}

			// check type
			if (!TypeUtils.isAssignable(deserialized.getClass(), expression.getType())) {
				throw new QueryResultConversionException("Expected a value of type [" + expression.getType().getName()
						+ "], got a value of type: " + deserialized.getClass().getName());
			}

			return (T) deserialized;

		}
		return null;
	}

	/**
	 * Deserialize given <code>value</code>.
	 * @param targetType Target type to obtain
	 * @param value Value to deserialize
	 * @return Deserialized value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object deserialize(Class<?> targetType, Object value) {
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

		// String to Reader
		if (TypeUtils.isString(value.getClass()) && Reader.class.isAssignableFrom(targetType)) {
			return new StringReader((String) value);
		}

		// Byte[] to InputStream
		if (value instanceof byte[] && InputStream.class.isAssignableFrom(targetType)) {
			return new ByteArrayInputStream((byte[]) value);
		}

		// ---- NOTE: LOB content is always fully read and serialized (Connection won't be available after result
		// query stream is returned)

		if (Clob.class.isAssignableFrom(value.getClass())) {
			try {
				// as Reader
				if (Reader.class.isAssignableFrom(targetType)) {
					return new StringReader(clobToString((Clob) value));
				}
				// as String
				if (String.class.isAssignableFrom(targetType)) {
					return clobToString((Clob) value);
				}
			} catch (SQLException | IOException e) {
				throw new DataAccessException("Error reading CLOB type value", e);
			}
		}

		// blob
		if (Blob.class.isAssignableFrom(value.getClass())) {
			try {
				// as InputStream
				if (InputStream.class.isAssignableFrom(targetType)) {
					return new ByteArrayInputStream(blobToBytes((Blob) value));
					// return ((Blob) value).getBinaryStream();
				}
				// as byte[]
				if (byte[].class.isAssignableFrom(targetType)) {
					return blobToBytes((Blob) value);
				}
			} catch (SQLException e) {
				throw new DataAccessException("Error reading CLOB type value", e);
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
	private static final String clobToString(final Clob value) throws SQLException, IOException {
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
