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
package com.holonplatform.datastore.jdbc.test.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.beans.Converter;
import com.holonplatform.core.beans.Converter.BUILTIN;
import com.holonplatform.core.beans.DataPath;
import com.holonplatform.core.beans.Identifier;
import com.holonplatform.core.beans.Temporal;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.temporal.TemporalType;

@DataPath("test1")
public class TestDataBean {

	public static final BeanPropertySet<TestDataBean> PROPERTIES = BeanPropertySet.create(TestDataBean.class);

	public static final NumericProperty<Long> CODE = PROPERTIES.propertyNumeric("code", Long.class);

	@Identifier
	@DataPath("keycode")
	private long code;

	@DataPath("strv")
	private String stringValue;

	private Double decv;

	@Temporal(TemporalType.DATE)
	@DataPath("datv")
	private Date dateValue;

	@DataPath("datv2")
	private LocalDate localDateValue;

	@DataPath("enmv")
	private TestEnum enumValue;

	@Converter(builtin = BUILTIN.NUMERIC_BOOLEAN)
	@DataPath("nbv")
	private boolean booleanValue;

	@DataPath("nst1")
	private String nestedStringValue;

	@DataPath("nst2")
	private BigDecimal nestedDecimalValue;

	@Temporal(TemporalType.DATE_TIME)
	@DataPath("tms")
	private Date timestampValue;

	@DataPath("tms2")
	private LocalDateTime localDateTimeValue;

	@DataPath("tm")
	private LocalTime localTimeValue;

	public TestDataBean() {
		super();
	}

	public TestDataBean(long code) {
		super();
		this.code = code;
	}

	public TestDataBean(long code, String stringValue) {
		super();
		this.code = code;
		this.stringValue = stringValue;
	}

	public long getCode() {
		return code;
	}

	public void setCode(long code) {
		this.code = code;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Double getDecv() {
		return decv;
	}

	public void setDecv(Double decv) {
		this.decv = decv;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public LocalDate getLocalDateValue() {
		return localDateValue;
	}

	public void setLocalDateValue(LocalDate localDateValue) {
		this.localDateValue = localDateValue;
	}

	public TestEnum getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(TestEnum enumValue) {
		this.enumValue = enumValue;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getNestedStringValue() {
		return nestedStringValue;
	}

	public void setNestedStringValue(String nestedStringValue) {
		this.nestedStringValue = nestedStringValue;
	}

	public BigDecimal getNestedDecimalValue() {
		return nestedDecimalValue;
	}

	public void setNestedDecimalValue(BigDecimal nestedDecimalValue) {
		this.nestedDecimalValue = nestedDecimalValue;
	}

	public Date getTimestampValue() {
		return timestampValue;
	}

	public void setTimestampValue(Date timestampValue) {
		this.timestampValue = timestampValue;
	}

	public LocalDateTime getLocalDateTimeValue() {
		return localDateTimeValue;
	}

	public void setLocalDateTimeValue(LocalDateTime localDateTimeValue) {
		this.localDateTimeValue = localDateTimeValue;
	}

	public LocalTime getLocalTimeValue() {
		return localTimeValue;
	}

	public void setLocalTimeValue(LocalTime localTimeValue) {
		this.localTimeValue = localTimeValue;
	}

}
