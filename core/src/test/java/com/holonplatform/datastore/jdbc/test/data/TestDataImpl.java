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
package com.holonplatform.datastore.jdbc.test.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@SuppressWarnings("serial")
public class TestDataImpl implements TestData {

	private long keycode;
	private String strv;
	private Double decv;
	private Date datv;
	private LocalDate datv2;
	private TestEnum enmv;
	private boolean nbv;
	private String nst1;
	private BigDecimal nst2;
	private Date tms;
	private LocalDateTime tms2;
	private LocalTime tm;

	public void setKeycode(long keycode) {
		this.keycode = keycode;
	}

	public void setStrv(String strv) {
		this.strv = strv;
	}

	public void setDecv(Double decv) {
		this.decv = decv;
	}

	public void setDatv(Date datv) {
		this.datv = datv;
	}

	public void setEnmv(TestEnum enmv) {
		this.enmv = enmv;
	}

	public void setNbv(boolean nbv) {
		this.nbv = nbv;
	}

	public void setNst1(String nst1) {
		this.nst1 = nst1;
	}

	public void setNst2(BigDecimal nst2) {
		this.nst2 = nst2;
	}

	public String getNst1() {
		return nst1;
	}

	public BigDecimal getNst2() {
		return nst2;
	}

	public Date getTms() {
		return tms;
	}

	public void setTms(Date tms) {
		this.tms = tms;
	}

	public LocalDateTime getTms2() {
		return tms2;
	}

	public void setTms2(LocalDateTime tms2) {
		this.tms2 = tms2;
	}

	public LocalTime getTm() {
		return tm;
	}

	public void setTm(LocalTime tm) {
		this.tm = tm;
	}

	public LocalDate getDatv2() {
		return datv2;
	}

	public void setDatv2(LocalDate datv2) {
		this.datv2 = datv2;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getKey()
	 */
	@Override
	public long getKey() {
		return keycode;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return strv;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getDecimalValue()
	 */
	@Override
	public Double getDecimalValue() {
		return decv;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getDateValue()
	 */
	@Override
	public Date getDateValue() {
		return datv;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getEnumValue()
	 */
	@Override
	public TestEnum getEnumValue() {
		return enmv;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.data.TestData#getNumericBooleanValue()
	 */
	@Override
	public boolean getNumericBooleanValue() {
		return nbv;
	}

}
