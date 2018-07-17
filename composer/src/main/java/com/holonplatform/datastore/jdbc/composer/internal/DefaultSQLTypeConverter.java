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

import java.io.InputStream;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLType;
import com.holonplatform.datastore.jdbc.composer.SQLTypeConverter;

/**
 * Default {@link SQLTypeConverter}.
 *
 * @since 5.1.0
 */
public enum DefaultSQLTypeConverter implements SQLTypeConverter {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLTypeConverter#getJavaType(com.holonplatform.datastore.jdbc.composer.
	 * SQLContext, com.holonplatform.datastore.jdbc.composer.SQLType)
	 */
	@Override
	public Optional<Class<?>> getJavaType(SQLContext context, SQLType sqlType) {
		ObjectUtils.argumentNotNull(sqlType, "SQL type must be not null");

		// check dialect
		Optional<Class<?>> dialect = context.getDialect().getJavaType(sqlType);
		if (dialect.isPresent()) {
			return dialect;
		}

		Class<?> javaType = null;

		switch (sqlType.getType()) {
		case Types.NULL:
			javaType = Void.class;
			break;

		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			javaType = String.class;
			break;

		case Types.NUMERIC:
		case Types.DECIMAL:
			javaType = java.math.BigDecimal.class;
			break;

		case Types.BIT:
		case Types.BOOLEAN:
			javaType = Boolean.class;
			break;

		case Types.TINYINT:
			javaType = Byte.class;
			break;

		case Types.SMALLINT:
			javaType = Short.class;
			break;

		case Types.INTEGER:
			javaType = Integer.class;
			break;

		case Types.BIGINT:
			javaType = Long.class;
			break;

		case Types.REAL:
		case Types.FLOAT:
			javaType = Float.class;
			break;

		case Types.DOUBLE:
			javaType = Double.class;
			break;

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			javaType = byte[].class;
			break;

		case Types.DATE:
			javaType = java.sql.Date.class;
			break;

		case Types.TIME:
			javaType = java.sql.Time.class;
			break;

		case Types.TIMESTAMP:
			javaType = java.sql.Timestamp.class;
			break;

		case Types.CLOB:
			javaType = java.sql.Clob.class;
			break;

		case Types.BLOB:
			javaType = java.sql.Blob.class;
			break;

		default:
			break;
		}

		return Optional.ofNullable(javaType);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLTypeConverter#getSqlType(com.holonplatform.datastore.jdbc.composer.
	 * SQLContext, java.lang.Class)
	 */
	@Override
	public Optional<SQLType> getSqlType(SQLContext context, Class<?> javaType) {
		ObjectUtils.argumentNotNull(javaType, "Java type must be not null");

		// check dialect
		Optional<SQLType> dialect = context.getDialect().getSqlType(javaType);
		if (dialect.isPresent()) {
			return dialect;
		}

		if (Void.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.NULL));
		}

		if (String.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.VARCHAR));
		}

		if (TypeUtils.isBoolean(javaType)) {
			return Optional.of(SQLType.create(Types.BOOLEAN));
		}

		if (TypeUtils.isInteger(javaType)) {
			return Optional.of(SQLType.create(Types.INTEGER));
		}
		if (TypeUtils.isLong(javaType)) {
			return Optional.of(SQLType.create(Types.BIGINT));
		}
		if (TypeUtils.isDouble(javaType) || TypeUtils.isFloat(javaType) || TypeUtils.isBigDecimal(javaType)) {
			return Optional.of(SQLType.create(Types.DECIMAL));
		}
		if (TypeUtils.isShort(javaType)) {
			return Optional.of(SQLType.create(Types.SMALLINT));
		}
		if (TypeUtils.isByte(javaType)) {
			return Optional.of(SQLType.create(Types.TINYINT));
		}
		if (TypeUtils.isNumber(javaType)) {
			return Optional.of(SQLType.create(Types.NUMERIC));
		}

		if (javaType == byte[].class) {
			return Optional.of(SQLType.create(Types.BINARY));
		}

		if (InputStream.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.BINARY));
		}

		if (TypeUtils.isDate(javaType) || TypeUtils.isCalendar(javaType)
				|| java.sql.Date.class.isAssignableFrom(javaType) || LocalDate.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.DATE));
		}
		if (LocalTime.class.isAssignableFrom(javaType) || java.sql.Time.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.TIME));
		}
		if (LocalDateTime.class.isAssignableFrom(javaType) || java.sql.Timestamp.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.TIMESTAMP));
		}
		if (ZonedDateTime.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.TIMESTAMP_WITH_TIMEZONE));
		}
		if (java.sql.Time.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.TIME));
		}
		if (java.sql.Timestamp.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.TIMESTAMP));
		}

		if (java.sql.Clob.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.CLOB));
		}
		if (java.sql.Blob.class.isAssignableFrom(javaType)) {
			return Optional.of(SQLType.create(Types.BLOB));
		}

		return Optional.empty();
	}

}
