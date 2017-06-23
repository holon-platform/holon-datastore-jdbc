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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.temporal.TemporalType;

public final class TestProperties implements Serializable {

	private static final long serialVersionUID = -820043080015011043L;

	public final static DataTarget<String> NAMED_TARGET = DataTarget.named("test1");

	public final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	public final static PathProperty<String> STR = PathProperty.create("strv", String.class);
	public final static PathProperty<Double> DBL = PathProperty.create("decv", Double.class);
	public final static PathProperty<Date> DAT = PathProperty.create("datv", Date.class);
	public final static PathProperty<LocalDate> LDAT = PathProperty.create("datv2", LocalDate.class);
	public final static PathProperty<TestEnum> ENM = PathProperty.create("enmv", TestEnum.class);
	public final static PathProperty<Boolean> NBOOL = PathProperty.create("nbv", boolean.class)
			.converter(PropertyValueConverter.numericBoolean(Integer.class));
	public final static PathProperty<String> NST_STR = PathProperty.create("nst1", String.class);
	public final static PathProperty<BigDecimal> NST_DEC = PathProperty.create("nst2", BigDecimal.class);

	public final static PathProperty<Date> TMS = PathProperty.create("tms", Date.class)
			.temporalType(TemporalType.DATE_TIME);
	public final static PathProperty<LocalDateTime> LTMS = PathProperty.create("tms2", LocalDateTime.class);

	public final static PathProperty<LocalTime> TIME = PathProperty.create("tm", LocalTime.class);

	public final static PropertySet<?> PROPS = PropertySet.of(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC,
			TMS, LTMS, TIME);

	// with parent
	public final static PathProperty<Long> KEY_P = NAMED_TARGET.property(KEY);
	public final static PathProperty<String> STR_P = NAMED_TARGET.property(STR);

	private TestProperties() {
	}

}
