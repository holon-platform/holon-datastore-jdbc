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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LDAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LTMS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.query.QueryFunction;

public class TemporalFunctionsTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testCurrentDate() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(NAMED_TARGET).list(QueryFunction.currentDate());
		assertTrue(dates.size() > 0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(dates.get(0));
		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(DAT.lt(QueryFunction.currentDate())).count();
		assertEquals(2L, cnt);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(DAT, QueryFunction.currentDate())
					.filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			Date date = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(DAT).orElse(null);
			assertNotNull(date);

			Calendar c = Calendar.getInstance();
			c.setTime(date);
			assertEquals(now.get(Calendar.YEAR), c.get(Calendar.YEAR));
			assertEquals(now.get(Calendar.MONTH), c.get(Calendar.MONTH));
			assertEquals(now.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));

		});
	}

	@Test
	public void testCurrentDateAsLocalDate() {

		final LocalDate lnow = LocalDate.now();

		LocalDate ldate = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L))
				.findOne(QueryFunction.currentLocalDate()).orElse(null);
		assertNotNull(ldate);
		assertEquals(lnow, ldate);

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(LDAT.loe(QueryFunction.currentLocalDate()))
				.count();
		assertEquals(2L, cnt);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(LDAT, QueryFunction.currentLocalDate())
					.filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			LocalDate date = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LDAT).orElse(null);
			assertNotNull(date);

			assertEquals(lnow, ldate);

		});
	}

	@Test
	public void testCurrentTimestamp() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(NAMED_TARGET).list(QueryFunction.currentTimestamp());
		assertTrue(dates.size() > 0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(dates.get(0));
		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(NAMED_TARGET)
				.filter(TMS.isNotNull().and(TMS.lt(QueryFunction.currentTimestamp()))).count();
		assertEquals(1L, cnt);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(TMS, QueryFunction.currentTimestamp())
					.filter(KEY.eq(2L)).execute();
			assertEquals(1, result.getAffectedCount());

			Date date = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(TMS).orElse(null);
			assertNotNull(date);

			Calendar c = Calendar.getInstance();
			c.setTime(date);
			assertEquals(now.get(Calendar.YEAR), c.get(Calendar.YEAR));
			assertEquals(now.get(Calendar.MONTH), c.get(Calendar.MONTH));
			assertEquals(now.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));

		});

	}

	@Test
	public void testCurrentTimestampAsLocalDateTime() {

		final LocalDateTime lnow = LocalDateTime.now().withSecond(0).withNano(0);

		List<LocalDateTime> dates = getDatastore().query().target(NAMED_TARGET)
				.list(QueryFunction.currentLocalDateTime());
		assertTrue(dates.size() > 0);
		assertEquals(lnow.toLocalDate(), dates.get(0).withSecond(0).withNano(0).toLocalDate());

		long cnt = getDatastore().query().target(NAMED_TARGET)
				.filter(LTMS.isNotNull().and(LTMS.loe(QueryFunction.currentLocalDateTime()))).count();
		assertEquals(1L, cnt);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET)
					.set(LTMS, QueryFunction.currentLocalDateTime()).filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			LocalDateTime ldate = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LTMS)
					.orElse(null);
			assertNotNull(ldate);

			assertEquals(lnow.toLocalDate(), ldate.withSecond(0).withNano(0).toLocalDate());

		});
	}

	@Test
	public void testTemporalFunctionProjection() {
		Integer value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(DAT.year()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LDAT.year()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(DAT.month()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LDAT.month()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(DAT.day()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LDAT.day()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(TMS.hour()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(LTMS.hour()).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);
	}

	@Test
	public void testTemporalFunctionFilter() {

		// year

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(DAT.year().eq(2016)).count();
		assertEquals(2L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LDAT.year().eq(2016)).count();
		assertEquals(2L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(TMS.year().eq(2017)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.year().eq(2017)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.year().eq(TMS.year())).count();
		assertEquals(1L, cnt);

		// month

		cnt = getDatastore().query().target(NAMED_TARGET).filter(DAT.month().eq(5)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LDAT.month().eq(5)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(TMS.month().eq(3)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.month().eq(3)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.month().eq(TMS.month())).count();
		assertEquals(1L, cnt);

		// day

		cnt = getDatastore().query().target(NAMED_TARGET).filter(DAT.day().eq(19)).count();
		assertEquals(2L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LDAT.day().eq(19)).count();
		assertEquals(2L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(TMS.day().eq(23)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.day().eq(23)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.day().eq(TMS.day())).count();
		assertEquals(1L, cnt);

		// hour

		cnt = getDatastore().query().target(NAMED_TARGET).filter(TMS.hour().eq(15)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.hour().eq(15)).count();
		assertEquals(1L, cnt);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.hour().eq(TMS.hour())).count();
		assertEquals(1L, cnt);

	}

}
