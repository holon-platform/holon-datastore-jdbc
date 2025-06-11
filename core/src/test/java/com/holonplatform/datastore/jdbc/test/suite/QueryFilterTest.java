/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.test.suite;

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DBL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LDAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LTMS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_DEC;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TIME;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.datastore.jdbc.composer.WhereFilter;

public class QueryFilterTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testFilters() {

		long count = getDatastore().query().target(NAMED_TARGET).filter(STR1.eq("One")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(new NotFilter(STR1.eq("One"))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.eq("One").not()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.neq("Two")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.isNotNull()).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(DBL.isNull()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.endsWith("x")).count();
		assertEquals(0, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.contains("w")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.containsIgnoreCase("O")).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.startsWith("O")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR1.startsWithIgnoreCase("o")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(DBL.gt(7d)).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(DBL.lt(8d)).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(NST_DEC.goe(new BigDecimal(3))).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(NST_DEC.loe(new BigDecimal(3))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.between(1L, 2L)).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.in(1L, 2L)).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.nin(1L, 2L)).count();
		assertEquals(0, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L).or(KEY.eq(2L))).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L).and(STR1.eq("One"))).count();
		assertEquals(1, count);

	}

	@Test
	public void testDateAndTimes() {

		List<Date> values = getDatastore().query().target(NAMED_TARGET).list(DAT);
		assertNotNull(values);
		Date value = values.get(0);
		assertNotNull(value);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2016);
		c.set(Calendar.MONTH, 4);
		c.set(Calendar.DAY_OF_MONTH, 19);

		values = getDatastore().query().target(NAMED_TARGET).filter(DAT.eq(c.getTime())).list(DAT);
		assertNotNull(values);
		assertEquals(1, values.size());

		c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2017);
		c.set(Calendar.MONTH, 2);
		c.set(Calendar.DAY_OF_MONTH, 23);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		values = getDatastore().query().target(NAMED_TARGET).filter(TMS.goe(c.getTime())).list(TMS);
		assertNotNull(values);
		assertEquals(1, values.size());

		c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2017);
		c.set(Calendar.MONTH, 2);
		c.set(Calendar.DAY_OF_MONTH, 23);
		c.set(Calendar.HOUR_OF_DAY, 15);
		c.set(Calendar.MINUTE, 30);
		c.set(Calendar.SECOND, 25);
		c.set(Calendar.MILLISECOND, 0);

		values = getDatastore().query().target(NAMED_TARGET).filter(TMS.eq(c.getTime())).list(TMS);
		assertNotNull(values);

		// Temporals

		List<LocalDate> lvalues = getDatastore().query().target(NAMED_TARGET).list(LDAT);
		assertNotNull(lvalues);
		LocalDate lvalue = lvalues.get(0);
		assertNotNull(lvalue);

		lvalues = getDatastore().query().target(NAMED_TARGET).filter(LDAT.eq(LocalDate.of(2016, Month.MAY, 19)))
				.list(LDAT);
		assertNotNull(lvalues);
		assertEquals(1, lvalues.size());

		lvalues = getDatastore().query().target(NAMED_TARGET).filter(LDAT.goe(LocalDate.of(2016, Month.APRIL, 19)))
				.list(LDAT);
		assertNotNull(lvalues);
		assertEquals(2, lvalues.size());
	}

	@Test
	public void testTimeFilter() {

		LocalTime time = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(TIME).orElse(null);
		assertNotNull(time);
		assertEquals(18, time.getHour());
		assertEquals(30, time.getMinute());
		assertEquals(15, time.getSecond());

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(TIME.eq(LocalTime.of(18, 30, 15))).count();
		assertEquals(1, cnt);

	}

	@Test
	public void testLocalDateTimeWithTimestampFilter() {

		List<LocalDateTime> ltvalues = getDatastore().query().target(NAMED_TARGET)
				.filter(LTMS.eq(LocalDateTime.of(2017, Month.MARCH, 23, 15, 30, 25))).list(LTMS);
		assertNotNull(ltvalues);
		assertEquals(1, ltvalues.size());

	}

	@Test
	public void testWhereFilter() {

		long count = getDatastore().query().target(NAMED_TARGET)
				.filter(WhereFilter.create("keycode = ?", Long.valueOf(1))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(WhereFilter.create("keycode = 1")).count();
		assertEquals(1, count);

	}

}
