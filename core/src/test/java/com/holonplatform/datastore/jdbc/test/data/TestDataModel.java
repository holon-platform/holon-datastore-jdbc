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

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.core.temporal.TemporalType;

public interface TestDataModel {

	public final static DataTarget<String> NAMED_TARGET = DataTarget.named("test1");

	public final static NumericProperty<Long> KEY = NumericProperty.create("keycode", long.class);
	public final static StringProperty STR = StringProperty.create("strv");
	public final static NumericProperty<Double> DBL = NumericProperty.doubleType("decv");
	public final static TemporalProperty<Date> DAT = TemporalProperty.create("datv", Date.class)
			.temporalType(TemporalType.DATE);
	public final static TemporalProperty<LocalDate> LDAT = TemporalProperty.localDate("datv2");
	public final static PathProperty<TestEnum> ENM = PathProperty.create("enmv", TestEnum.class);
	public final static PathProperty<Boolean> NBOOL = PathProperty.create("nbv", boolean.class)
			.converter(PropertyValueConverter.numericBoolean(Integer.class));
	public final static PathProperty<String> NST_STR = PathProperty.create("nst1", String.class);
	public final static PathProperty<BigDecimal> NST_DEC = PathProperty.create("nst2", BigDecimal.class);

	public final static TemporalProperty<Date> TMS = TemporalProperty.create("tms", Date.class)
			.temporalType(TemporalType.DATE_TIME);
	public final static TemporalProperty<LocalDateTime> LTMS = TemporalProperty.localDateTime("tms2");

	public final static TemporalProperty<LocalTime> TIME = TemporalProperty.localTime("tm");

	public final static PropertySet<?> PROPERTIES = PropertySet
			.builderOf(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME).identifier(KEY).build();

	public final static PropertySet<?> PROPERTIES_NOID = PropertySet.of(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR,
			NST_DEC, TMS, LTMS, TIME);

	// with parent
	public final static PathProperty<Long> KEY_P = NAMED_TARGET.property(KEY);
	public final static PathProperty<String> STR_P = NAMED_TARGET.property(STR);

}
