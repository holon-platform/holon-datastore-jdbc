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
package com.holonplatform.datastore.jdbc.test.suite;

import static com.holonplatform.datastore.jdbc.test.data.jpa.TestJpaBean.CODE;
import static com.holonplatform.datastore.jdbc.test.data.jpa.TestJpaBean.PROPERTIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.junit.Test;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.beans.BeanDatastore;
import com.holonplatform.core.datastore.beans.BeanDatastore.BeanOperationResult;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.core.query.QuerySort.SortDirection;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.data.TestSampleData;
import com.holonplatform.datastore.jdbc.test.data.jpa.TestJpaBean;

public class BeanDatastoreJpaTest extends AbstractJdbcDatastoreSuiteTest {

	private BeanDatastore beanDatastore;

	protected BeanDatastore getBeanDatastore() {
		if (beanDatastore == null) {
			beanDatastore = BeanDatastore.of(getDatastore());
		}
		return beanDatastore;
	}

	@Override
	protected void inTransaction(Runnable operation) {
		getBeanDatastore().requireTransactional().withTransaction(tx -> {
			tx.setRollbackOnly();
			operation.run();
		});
	}

	// ------- Tests

	@Test
	public void testRefresh() {

		TestJpaBean value2 = getBeanDatastore().query(TestJpaBean.class).filter(PROPERTIES.property("code").eq(2L))
				.findOne().orElse(null);
		assertNotNull(value2);
		assertEquals(2L, value2.getCode());

		TestJpaBean refreshed2 = getBeanDatastore().refresh(value2);
		assertNotNull(refreshed2);
		assertEquals(2L, refreshed2.getCode());

		inTransaction(() -> {
			TestJpaBean value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals("One", value.getStringValue());

			// update string value
			BeanOperationResult<TestJpaBean> result = getBeanDatastore().bulkUpdate(TestJpaBean.class)
					.set(PROPERTIES.property("stringValue"), "OneX").filter(CODE.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			// refresh
			TestJpaBean refreshed = getBeanDatastore().refresh(value);
			assertNotNull(refreshed);
			assertEquals(1L, refreshed.getCode());
			assertEquals("OneX", refreshed.getStringValue());

		});

		final TestJpaBean valueNoId = new TestJpaBean();
		valueNoId.setStringValue("NoId");

		expectedException(DataAccessException.class, () -> {
			getBeanDatastore().refresh(valueNoId);
		});

	}

	@Test
	public void testInsert() {

		inTransaction(() -> {

			TestJpaBean value = new TestJpaBean();
			value.setCode(301L);
			value.setStringValue("k301");
			value.setDecv(7.45);
			value.setDateValue(TestSampleData.DATE1);
			value.setLocalDateValue(TestSampleData.LDATE1);
			value.setEnumValue(TestEnum.SECOND);
			value.setNestedStringValue("str1");
			value.setNestedDecimalValue(TestSampleData.BD1);
			value.setBooleanValue(false);
			value.setTimestampValue(TestSampleData.DATETIME1);
			value.setLocalDateTimeValue(TestSampleData.LDATETIME1);
			value.setLocalTimeValue(TestSampleData.LTIME1);

			BeanOperationResult<?> result = getBeanDatastore().insert(value);
			assertEquals(1, result.getAffectedCount());

			value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(301L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals(301L, value.getCode());
			assertEquals("k301", value.getStringValue());
			assertEquals(Double.valueOf(7.45), value.getDecv());
			assertEquals(TestSampleData.DATE1, value.getDateValue());
			assertEquals(TestSampleData.LDATE1, value.getLocalDateValue());
			assertEquals(TestEnum.SECOND, value.getEnumValue());
			assertEquals("str1", value.getNestedStringValue());
			assertEquals(TestSampleData.BD1, value.getNestedDecimalValue());
			assertFalse(value.isBooleanValue());
			assertEquals(TestSampleData.DATETIME1, value.getTimestampValue());
			assertEquals(TestSampleData.LDATETIME1, value.getLocalDateTimeValue());
			assertEquals(TestSampleData.LTIME1, value.getLocalTimeValue());

		});
	}

	@Test
	public void testUpdate() {
		inTransaction(() -> {

			TestJpaBean value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);

			value.setStringValue("Ustr");
			value.setDecv(432.67d);
			value.setDateValue(TestSampleData.DATE1);
			value.setLocalDateValue(TestSampleData.LDATE1);
			value.setEnumValue(TestEnum.THIRD);
			value.setBooleanValue(false);
			value.setNestedStringValue("Unstr");
			value.setNestedDecimalValue(TestSampleData.BD1);
			value.setTimestampValue(TestSampleData.DATETIME1);
			value.setLocalDateTimeValue(TestSampleData.LDATETIME1);
			value.setLocalTimeValue(TestSampleData.LTIME1);

			BeanOperationResult<?> result = getBeanDatastore().update(value);
			assertEquals(1, result.getAffectedCount());

			value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals(1L, value.getCode());
			assertEquals("Ustr", value.getStringValue());
			assertEquals(Double.valueOf(432.67), value.getDecv());
			assertEquals(TestSampleData.DATE1, value.getDateValue());
			assertEquals(TestSampleData.LDATE1, value.getLocalDateValue());
			assertEquals(TestEnum.THIRD, value.getEnumValue());
			assertEquals("Unstr", value.getNestedStringValue());
			assertEquals(TestSampleData.BD1, value.getNestedDecimalValue());
			assertFalse(value.isBooleanValue());
			assertEquals(TestSampleData.DATETIME1, value.getTimestampValue());
			assertEquals(TestSampleData.LDATETIME1, value.getLocalDateTimeValue());
			assertEquals(TestSampleData.LTIME1, value.getLocalTimeValue());

		});
	}

	@Test
	public void testSaveAsInsert() {
		inTransaction(() -> {

			TestJpaBean value = new TestJpaBean();
			value.setCode(401L);
			value.setStringValue("k401");
			value.setDecv(7.45);
			value.setDateValue(TestSampleData.DATE1);
			value.setLocalDateValue(TestSampleData.LDATE1);
			value.setEnumValue(TestEnum.SECOND);
			value.setNestedStringValue("str1");
			value.setNestedDecimalValue(TestSampleData.BD1);
			value.setBooleanValue(false);
			value.setTimestampValue(TestSampleData.DATETIME1);
			value.setLocalDateTimeValue(TestSampleData.LDATETIME1);
			value.setLocalTimeValue(TestSampleData.LTIME1);

			BeanOperationResult<?> result = getBeanDatastore().save(value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

			value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(401L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals(401L, value.getCode());
			assertEquals("k401", value.getStringValue());
			assertEquals(Double.valueOf(7.45), value.getDecv());
			assertEquals(TestSampleData.DATE1, value.getDateValue());
			assertEquals(TestSampleData.LDATE1, value.getLocalDateValue());
			assertEquals(TestEnum.SECOND, value.getEnumValue());
			assertEquals("str1", value.getNestedStringValue());
			assertEquals(TestSampleData.BD1, value.getNestedDecimalValue());
			assertFalse(value.isBooleanValue());
			assertEquals(TestSampleData.DATETIME1, value.getTimestampValue());
			assertEquals(TestSampleData.LDATETIME1, value.getLocalDateTimeValue());
			assertEquals(TestSampleData.LTIME1, value.getLocalTimeValue());

		});
	}

	@Test
	public void testSaveAsUpdate() {
		inTransaction(() -> {

			TestJpaBean value = new TestJpaBean();
			value.setCode(1L);
			value.setStringValue("k401");
			value.setDecv(7.45);
			value.setDateValue(TestSampleData.DATE1);
			value.setLocalDateValue(TestSampleData.LDATE1);
			value.setEnumValue(TestEnum.SECOND);
			value.setNestedStringValue("str1");
			value.setNestedDecimalValue(TestSampleData.BD1);
			value.setBooleanValue(false);
			value.setTimestampValue(TestSampleData.DATETIME1);
			value.setLocalDateTimeValue(TestSampleData.LDATETIME1);
			value.setLocalTimeValue(TestSampleData.LTIME1);

			BeanOperationResult<?> result = getBeanDatastore().save(value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.UPDATE, result.getOperationType().orElse(null));

			value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals(1L, value.getCode());
			assertEquals("k401", value.getStringValue());
			assertEquals(Double.valueOf(7.45), value.getDecv());
			assertEquals(TestSampleData.DATE1, value.getDateValue());
			assertEquals(TestSampleData.LDATE1, value.getLocalDateValue());
			assertEquals(TestEnum.SECOND, value.getEnumValue());
			assertEquals("str1", value.getNestedStringValue());
			assertEquals(TestSampleData.BD1, value.getNestedDecimalValue());
			assertFalse(value.isBooleanValue());
			assertEquals(TestSampleData.DATETIME1, value.getTimestampValue());
			assertEquals(TestSampleData.LDATETIME1, value.getLocalDateTimeValue());
			assertEquals(TestSampleData.LTIME1, value.getLocalTimeValue());

		});
	}

	@Test
	public void testDelete() {
		inTransaction(() -> {

			TestJpaBean value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);

			BeanOperationResult<?> result = getBeanDatastore().delete(value);
			assertEquals(1, result.getAffectedCount());

			assertFalse(getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().isPresent());

		});
	}

	@Test
	public void testBulkInsert() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkInsert(TestJpaBean.class)
					.add(new TestJpaBean(201L, "k201")).add(new TestJpaBean(202L, "k202"))
					.add(new TestJpaBean(203L, "k203")).add(new TestJpaBean(204L, "k204"))
					.add(new TestJpaBean(205L, "k205")).execute();

			assertEquals(5, result.getAffectedCount());

			List<TestJpaBean> vals = getBeanDatastore().query(TestJpaBean.class).filter(CODE.between(201L, 205L))
					.sort(CODE.asc()).list();
			assertEquals(5, vals.size());
			assertEquals("k201", vals.get(0).getStringValue());
			assertEquals("k202", vals.get(1).getStringValue());
			assertEquals("k203", vals.get(2).getStringValue());
			assertEquals("k204", vals.get(3).getStringValue());
			assertEquals("k205", vals.get(4).getStringValue());

		});
	}

	@Test
	public void testBulkUpdate() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkUpdate(TestJpaBean.class)
					.set(PROPERTIES.property("stringValue"), "upd").set("booleanValue", false).filter(CODE.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			TestJpaBean value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals("upd", value.getStringValue());
			assertFalse(value.isBooleanValue());

			result = getBeanDatastore().bulkUpdate(TestJpaBean.class).set("stringValue", "updx").filter(CODE.loe(2L))
					.execute();
			assertEquals(2, result.getAffectedCount());

			Stream<String> vals = getBeanDatastore().query(TestJpaBean.class).filter(CODE.loe(2L)).stream()
					.map(r -> r.getStringValue());
			vals.forEach(v -> assertEquals("updx", v));
		});
	}

	@Test
	public void testBulkUpdateNulls() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkUpdate(TestJpaBean.class).setNull("stringValue")
					.set(PROPERTIES.property("decv"), 557.88).filter(CODE.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			TestJpaBean value = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertNull(value.getStringValue());
			assertEquals(Double.valueOf(557.88), value.getDecv());

		});
	}

	@Test
	public void testBulkDelete() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkDelete(TestJpaBean.class).filter(CODE.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			assertFalse(getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne().isPresent());
		});
	}

	@Test
	public void testBulkDeleteMulti() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkDelete(TestJpaBean.class).filter(CODE.loe(2L))
					.execute();
			assertEquals(2, result.getAffectedCount());

			assertEquals(0, getBeanDatastore().query(TestJpaBean.class).count());
		});
	}

	@Test
	public void testSorts() {

		final StringProperty STR1 = PROPERTIES.propertyString("stringValue");

		List<Long> res = getBeanDatastore().query(TestJpaBean.class).sort(STR1.desc()).sort(CODE.desc()).stream()
				.map(r -> r.getCode()).collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(STR1.desc()).sort(CODE.asc()).stream()
				.map(r -> r.getCode()).collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(STR1.desc().and(CODE.desc())).stream()
				.map(r -> r.getCode()).collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(CODE.asc()).stream().map(r -> r.getCode())
				.collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(1), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(QuerySort.asc(CODE)).stream().map(r -> r.getCode())
				.collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(1), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(QuerySort.of(CODE, SortDirection.DESCENDING)).stream()
				.map(r -> r.getCode()).collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getBeanDatastore().query(TestJpaBean.class).sort(QuerySort.of(STR1.desc(), CODE.asc())).stream()
				.map(r -> r.getCode()).collect(Collectors.toList());
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));
	}

	@Test
	public void testFilters() {

		final StringProperty STR1 = PROPERTIES.propertyString("stringValue");
		final PathProperty<Double> DBL = PROPERTIES.property("decv");
		final NumericProperty<BigDecimal> NST_DEC = PROPERTIES.propertyNumeric("nestedDecimalValue", BigDecimal.class);

		long count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.eq("One")).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(new NotFilter(STR1.eq("One"))).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.eq("One").not()).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.neq("Two")).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.isNotNull()).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(DBL.isNull()).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.endsWith("x")).count();
		assertEquals(0, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.contains("w")).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.containsIgnoreCase("O")).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.startsWith("O")).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(STR1.startsWithIgnoreCase("o")).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(DBL.gt(7d)).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(DBL.lt(8d)).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(NST_DEC.goe(new BigDecimal(3))).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(NST_DEC.loe(new BigDecimal(3))).count();
		assertEquals(1, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(CODE.between(1L, 2L)).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(CODE.in(1L, 2L)).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(CODE.nin(1L, 2L)).count();
		assertEquals(0, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L).or(CODE.eq(2L))).count();
		assertEquals(2, count);

		count = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L).and(STR1.eq("One"))).count();
		assertEquals(1, count);

	}

	@Test
	public void testDateAndTimes() {

		final TemporalProperty<Date> DAT = PROPERTIES.propertyTemporal("dateValue", Date.class);
		final TemporalProperty<Date> TMS = PROPERTIES.propertyTemporal("timestampValue", Date.class);
		final TemporalProperty<LocalDate> LDAT = PROPERTIES.propertyTemporal("localDateValue", LocalDate.class);

		List<Date> values = getBeanDatastore().query(TestJpaBean.class).stream().map(r -> r.getDateValue())
				.collect(Collectors.toList());
		assertNotNull(values);
		Date value = values.get(0);
		assertNotNull(value);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2016);
		c.set(Calendar.MONTH, 4);
		c.set(Calendar.DAY_OF_MONTH, 19);

		values = getBeanDatastore().query(TestJpaBean.class).filter(DAT.eq(c.getTime())).stream()
				.map(r -> r.getDateValue()).collect(Collectors.toList());
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

		values = getBeanDatastore().query(TestJpaBean.class).filter(TMS.goe(c.getTime())).stream()
				.map(r -> r.getTimestampValue()).collect(Collectors.toList());
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

		values = getBeanDatastore().query(TestJpaBean.class).filter(TMS.eq(c.getTime())).stream()
				.map(r -> r.getTimestampValue()).collect(Collectors.toList());
		assertNotNull(values);

		// Temporals

		List<LocalDate> lvalues = getBeanDatastore().query(TestJpaBean.class).stream().map(r -> r.getLocalDateValue())
				.collect(Collectors.toList());
		assertNotNull(lvalues);
		LocalDate lvalue = lvalues.get(0);
		assertNotNull(lvalue);

		lvalues = getBeanDatastore().query(TestJpaBean.class).filter(LDAT.eq(LocalDate.of(2016, Month.MAY, 19)))
				.stream().map(r -> r.getLocalDateValue()).collect(Collectors.toList());
		assertNotNull(lvalues);
		assertEquals(1, lvalues.size());

		lvalues = getBeanDatastore().query(TestJpaBean.class).filter(LDAT.goe(LocalDate.of(2016, Month.APRIL, 19)))
				.stream().map(r -> r.getLocalDateValue()).collect(Collectors.toList());
		assertNotNull(lvalues);
		assertEquals(2, lvalues.size());
	}

	@Test
	public void testTimeFilter() {

		final TemporalProperty<LocalTime> TIME = PROPERTIES.propertyTemporal("localTimeValue", LocalTime.class);

		LocalTime time = getBeanDatastore().query(TestJpaBean.class).filter(CODE.eq(1L)).findOne()
				.map(r -> r.getLocalTimeValue()).orElse(null);
		assertNotNull(time);
		assertEquals(18, time.getHour());
		assertEquals(30, time.getMinute());
		assertEquals(15, time.getSecond());

		long cnt = getBeanDatastore().query(TestJpaBean.class).filter(TIME.eq(LocalTime.of(18, 30, 15))).count();
		assertEquals(1, cnt);

	}

	@Test
	public void testLocalDateTimeWithTimestampFilter() {

		final TemporalProperty<LocalDateTime> LTMS = PROPERTIES.propertyTemporal("localDateTimeValue",
				LocalDateTime.class);

		List<LocalDateTime> ltvalues = getBeanDatastore().query(TestJpaBean.class)
				.filter(LTMS.eq(LocalDateTime.of(2017, Month.MARCH, 23, 15, 30, 25))).stream()
				.map(r -> r.getLocalDateTimeValue()).collect(Collectors.toList());
		assertNotNull(ltvalues);
		assertEquals(1, ltvalues.size());

	}

	@Test
	public void testRestrictions() {
		List<String> str = getBeanDatastore().query(TestJpaBean.class).restrict(1, 0).sort(CODE.asc()).stream()
				.map(r -> r.getStringValue()).collect(Collectors.toList());
		assertEquals(1, str.size());
		assertEquals("One", str.get(0));

		str = getBeanDatastore().query(TestJpaBean.class).restrict(1, 1).sort(CODE.asc()).stream()
				.map(r -> r.getStringValue()).collect(Collectors.toList());
		assertEquals(1, str.size());
		assertEquals("Two", str.get(0));
	}

	@Test
	public void testClobString() {

		final BeanPropertySet<TestJpaBeanClob1> PS = BeanPropertySet.create(TestJpaBeanClob1.class);

		final NumericProperty<Long> KEY = PS.propertyNumeric("code", Long.class);

		inTransaction(() -> {

			// query

			String sval = getBeanDatastore().query(TestJpaBeanClob1.class).filter(KEY.eq(1L)).findOne()
					.map(r -> r.getClob()).orElse(null);
			assertNotNull(sval);
			assertEquals("clocbcontent", sval);

			TestJpaBeanClob1 value = getBeanDatastore().query(TestJpaBeanClob1.class).filter(KEY.eq(1L)).findOne()
					.orElse(null);
			assertNotNull(value);
			assertEquals("clocbcontent", value.getClob());

			// update

			value.setClob("updclob");
			getBeanDatastore().update(value);

			value = getBeanDatastore().query(TestJpaBeanClob1.class).filter(KEY.eq(1L)).findOne().orElse(null);
			assertNotNull(value);
			assertEquals("updclob", value.getClob());

			// insert
			value = new TestJpaBeanClob1();
			value.setCode(77L);
			value.setStringValue("Test clob");
			value.setClob("savedclob");
			getBeanDatastore().insert(value);

			sval = getBeanDatastore().query(TestJpaBeanClob1.class).filter(KEY.eq(77L)).findOne().map(r -> r.getClob())
					.orElse(null);
			assertNotNull(sval);
			assertEquals("savedclob", sval);

		});
	}

	// -------

	@Entity
	@Table(name = "test1")
	@SuppressWarnings("serial")
	public static class TestJpaBeanClob1 extends TestJpaBean {

		@Lob
		@Column(name = "clb")
		private String clob;

		public TestJpaBeanClob1() {
			super();
		}

		public String getClob() {
			return clob;
		}

		public void setClob(String clob) {
			this.clob = clob;
		}

	}

}
