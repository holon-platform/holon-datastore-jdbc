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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.internal.expressions.LiteralValue;

/**
 * SQL constant value serializer.
 *
 * @since 5.0.0
 */
public final class SQLValueSerializer implements Serializable {

	private static final long serialVersionUID = 7172624787331055754L;

	private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
	private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance(Locale.US);

	public static final String ANSI_DATE_FORMAT = "yyyy-MM-dd";
	public static final String ANSI_TIME_FORMAT = "HH:mm:ss";
	public static final String ANSI_DATETIME_FORMAT = ANSI_DATE_FORMAT + " " + ANSI_TIME_FORMAT;

	private static final DateTimeFormatter ANSI_LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(ANSI_DATE_FORMAT,
			Locale.US);
	private static final DateTimeFormatter ANSI_LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern(ANSI_TIME_FORMAT,
			Locale.US);

	private static final DateTimeFormatter ANSI_OFFSET_ID_FORMATTER = new DateTimeFormatterBuilder()
			.parseCaseInsensitive().appendOffsetId().toFormatter();

	static {
		INTEGER_FORMAT.setGroupingUsed(false);
		INTEGER_FORMAT.setParseIntegerOnly(true);
		DECIMAL_FORMAT.setGroupingUsed(false);
	}

	/*
	 * Empty private constructor: this class is intended only to provide constants ad utility methods.
	 */
	private SQLValueSerializer() {
	}

	/**
	 * Serialize given {@link LiteralValue} as SQL string.
	 * @param value Value to serialize
	 * @param temporalType Optional temporal type
	 * @return Serialized SQL value
	 */
	public static String serializeValue(Object value, TemporalType temporalType) {

		if (value != null) {

			// CharSequence
			if (TypeUtils.isCharSequence(value.getClass())) {
				return "'" + value.toString() + "'";
			}

			// Number
			if (TypeUtils.isNumber(value.getClass())) {
				if (TypeUtils.isDecimalNumber(value.getClass())) {
					return DECIMAL_FORMAT.format(value);
				}
				return INTEGER_FORMAT.format(value);
			}

			// Enum (by default, ordinal value is used)
			if (TypeUtils.isEnum(value.getClass())) {
				return String.valueOf(((Enum<?>) value).ordinal());
			}

			// Reader
			if (Reader.class.isAssignableFrom(value.getClass())) {
				try (Reader reader = ((Reader) value)) {
					StringBuilder sb = new StringBuilder();
					sb.append("'");
					int buffer;
					while ((buffer = reader.read()) != -1) {
						sb.append((char) buffer);
					}
					sb.append("'");
					return sb.toString();
				} catch (IOException e) {
					throw new RuntimeException("Failed to read value from the Reader [" + value + "]", e);
				}
			}

			// Date and times
			Optional<String> serializedDate = serializeDate(value, temporalType);
			if (serializedDate.isPresent()) {
				return "'" + serializedDate.get() + "'";
			}

			// defaults to toString()
			return value.toString();
		}

		return null;
	}

	/**
	 * Serialize given date/time value.
	 * @param value Value to serialize
	 * @param temporalType Temporal type
	 * @return Serialized date/time value
	 */
	public static Optional<String> serializeDate(Object value, TemporalType temporalType) {
		if (value != null) {
			if (TypeUtils.isDate(value.getClass()) || TypeUtils.isCalendar(value.getClass())) {
				final Date date = TypeUtils.isCalendar(value.getClass()) ? ((Calendar) value).getTime() : (Date) value;
				TemporalType tt = (temporalType != null) ? temporalType : TemporalType.DATE;

				LocalDate datePart = null;
				LocalTime timePart = null;

				switch (tt) {
				case DATE_TIME:
					datePart = ConversionUtils.toLocalDate(date);
					timePart = ConversionUtils.toLocalTime(date);
					break;
				case TIME:
					timePart = ConversionUtils.toLocalTime(date);
					break;
				case DATE:
				default:
					datePart = ConversionUtils.toLocalDate(date);
					break;
				}

				return Optional.of(serializeDateTimeValue(datePart, timePart, null));
			}

			if (TemporalAccessor.class.isAssignableFrom(value.getClass())) {

				LocalDate datePart = null;
				LocalTime timePart = null;
				ZoneOffset offset = null;

				if (value instanceof LocalDate) {
					datePart = (LocalDate) value;
				} else if (value instanceof LocalTime) {
					timePart = (LocalTime) value;
				} else if (value instanceof LocalDateTime) {
					datePart = ((LocalDateTime) value).toLocalDate();
					timePart = ((LocalDateTime) value).toLocalTime();
				} else if (value instanceof OffsetTime) {
					timePart = ((OffsetTime) value).toLocalTime();
					offset = ((OffsetTime) value).getOffset();
				} else if (value instanceof OffsetDateTime) {
					datePart = ((OffsetDateTime) value).toLocalDate();
					timePart = ((OffsetDateTime) value).toLocalTime();
					offset = ((OffsetDateTime) value).getOffset();
				} else if (value instanceof ZonedDateTime) {
					datePart = ((ZonedDateTime) value).toLocalDate();
					timePart = ((ZonedDateTime) value).toLocalTime();
					offset = ((ZonedDateTime) value).getOffset();
				}

				if (datePart != null || timePart != null) {
					LocalDate serializeDate = datePart;
					LocalTime serializeTime = timePart;

					if (temporalType != null) {
						if (temporalType == TemporalType.DATE) {
							serializeTime = null;
						} else if (temporalType == TemporalType.TIME) {
							serializeDate = null;
						}
					}

					return Optional.of(serializeDateTimeValue(serializeDate, serializeTime, offset));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Serialize a date/time value using given {@link LocalDate} part, {@link LocalTime} part and zone offset.
	 * @param datePart Date part
	 * @param timePart Time part
	 * @param offset Zone offset
	 * @return Serialized date/time value
	 */
	private static String serializeDateTimeValue(LocalDate datePart, LocalTime timePart, ZoneOffset offset) {
		final StringBuilder sb = new StringBuilder();

		boolean appendSpace = false;
		if (datePart != null) {
			sb.append(ANSI_LOCAL_DATE_FORMATTER.format(datePart));
			appendSpace = true;
		}
		if (timePart != null) {
			if (appendSpace) {
				sb.append(" ");
			}
			sb.append(ANSI_LOCAL_TIME_FORMATTER.format(timePart));
			appendSpace = true;

			if (offset != null) {
				sb.append(" ");
				sb.append(ANSI_OFFSET_ID_FORMATTER.format(offset));
			}
		}

		return sb.toString();
	}

}
