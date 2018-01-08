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
package com.holonplatform.datastore.jdbc.test;

import static com.holonplatform.datastore.jdbc.test.data.TestProperties.DAT;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.DBL;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.ENM;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.KEY_P;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.LDAT;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.LTMS;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.NST_DEC;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.PROPS;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.STR;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.STR_P;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.TIME;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.TMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DataTarget.DataTargetResolver;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryAggregation;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.StringFunction.Upper;
import com.holonplatform.core.query.TemporalFunction.Day;
import com.holonplatform.core.query.TemporalFunction.Hour;
import com.holonplatform.core.query.TemporalFunction.Year;
import com.holonplatform.datastore.jdbc.JdbcWhereFilter;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.test.data.KeyIs;
import com.holonplatform.datastore.jdbc.test.data.TestData;
import com.holonplatform.datastore.jdbc.test.data.TestDataImpl;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.data.TestProjectionBean;

public abstract class AbstractJdbcDatastoreTest {

	protected abstract Datastore getDatastore();

	@Test
	public void testPrimaryKey() {
		final JdbcDatastoreCommodityContext ds = (JdbcDatastoreCommodityContext) getDatastore();
		Path<?>[] primaryKey = ds.withConnection(c -> {
			return JdbcDatastoreUtils.getPrimaryKey(ds.getDialect(), NAMED_TARGET.getName(), c).orElse(null);
		});
		assertNotNull(primaryKey);
		assertEquals(1, primaryKey.length);
		assertTrue(primaryKey[0].getName().equalsIgnoreCase("keycode"));
	}

	@Test
	public void testQueryResults() {
		List<PropertyBox> results = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(-100L)).list(PROPS);
		assertNotNull(results);
		assertEquals(0, results.size());

		PropertyBox result = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPS).orElse(null);
		assertNotNull(result);
		assertEquals(Long.valueOf(1), result.getValue(KEY));
		assertEquals("One", result.getValue(STR));
		assertEquals(Double.valueOf(7.4), result.getValue(DBL));
		assertEquals(TestEnum.FIRST, result.getValue(ENM));
		assertEquals(Boolean.TRUE, result.getValue(NBOOL));
	}

	@Test
	public void testFindByKey() {
		Optional<PropertyBox> pb = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPS);
		assertTrue(pb.isPresent());
		assertEquals("One", pb.get().getValue(STR));
	}

	@Test
	public void testCount() {
		long count = getDatastore().query().target(NAMED_TARGET).count();
		assertEquals(2, count);
	}

	@Test
	public void testPropertyConverters() {
		Boolean value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(NBOOL).orElse(null);
		assertNotNull(value);
		assertTrue(value);

		Long key = getDatastore().query().target(NAMED_TARGET).filter(NBOOL.eq(true)).findOne(KEY).orElse(null);
		assertNotNull(key);
		assertEquals(Long.valueOf(1), key);
	}

	@Test
	public void testBeanConversion() {
		List<TestData> results = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc()).stream(PROPS)
				.map(r -> BeanIntrospector.get().write(r, new TestDataImpl())).collect(Collectors.toList());
		assertNotNull(results);
		assertEquals(2, results.size());

		TestData first = results.get(0);
		assertEquals(1L, first.getKey());
	}

	@Test
	public void testQueryProjection() {

		List<PropertyBox> results = getDatastore().query().target(NAMED_TARGET).list(PROPS);
		assertNotNull(results);
		assertEquals(2, results.size());

		List<String> values = getDatastore().query().target(NAMED_TARGET)
				.list(PathProperty.create("strv", String.class));
		assertNotNull(values);
		assertEquals(2, values.size());

		values = getDatastore().query().target(NAMED_TARGET).list(STR);
		assertNotNull(values);
		assertEquals(2, values.size());

		Optional<Long> count = getDatastore().query().target(NAMED_TARGET).findOne(STR.count());
		assertEquals(new Long(2), count.get());

		// results converter

		List<Long> keys = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc()).stream(PROPS)
				.map((r) -> r.getValue(KEY)).collect(Collectors.toList());
		assertNotNull(keys);
		assertEquals(2, keys.size());
		assertEquals(new Long(1), keys.get(0));
		assertEquals(new Long(2), keys.get(1));
	}

	@Test
	public void testQueryAggregateProjection() {

		Optional<Long> key = getDatastore().query().target(NAMED_TARGET).findOne(KEY.max());
		assertTrue(key.isPresent());
		assertEquals(new Long(2), key.get());

		key = getDatastore().query().target(NAMED_TARGET).findOne(KEY.min());
		assertTrue(key.isPresent());
		assertEquals(new Long(1), key.get());

		Optional<Double> avg = getDatastore().query().target(NAMED_TARGET).findOne(KEY.avg());
		assertTrue(avg.isPresent());
		assertEquals(new Double(1.5), avg.get());

		Optional<Long> sum = getDatastore().query().target(NAMED_TARGET).findOne(KEY.sum());
		assertTrue(sum.isPresent());
		assertEquals(new Long(3), sum.get());

		Optional<Long> count = getDatastore().query().target(NAMED_TARGET).findOne(KEY.count());
		assertEquals(new Long(2), count.get());
	}

	@Test
	public void testStringFunctions() {
		String str = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L))
				.findOne(STR.function(QueryFunction.lower())).orElse(null);
		assertNotNull(str);
		assertEquals("one", str);

		str = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(Upper.of(STR)).orElse(null);
		assertNotNull(str);
		assertEquals("ONE", str);
	}

	@Test
	@Transactional
	@Rollback
	public void testCurrentDate() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(NAMED_TARGET).list(QueryFunction.currentDate());
		assertTrue(dates.size() > 0);
		Date date = dates.get(0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(DAT.lt(QueryFunction.currentDate())).count();
		assertEquals(2L, cnt);

		OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(DAT, QueryFunction.currentDate())
				.filter(KEY.eq(1L)).execute();
		assertEquals(1, result.getAffectedCount());

		date = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(DAT).orElse(null);
		assertNotNull(date);

		dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		// LocalDate

		LocalDate lnow = LocalDate.now();

		result = getDatastore().bulkUpdate(NAMED_TARGET).set(LDAT, QueryFunction.currentLocalDate()).filter(KEY.eq(1L))
				.execute();
		assertEquals(1, result.getAffectedCount());

		LocalDate ldate = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LDAT).orElse(null);
		assertNotNull(ldate);

		assertEquals(lnow, ldate);

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LDAT.loe(QueryFunction.currentLocalDate())).count();
		assertEquals(2L, cnt);

		List<LocalDate> ldates = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc())
				.list(QueryFunction.currentLocalDate());
		assertTrue(ldates.size() > 0);

		ldate = ldates.get(0);

		assertEquals(lnow, ldate);
	}

	@Test
	@Transactional
	@Rollback
	public void testCurrentTimestamp() {

		final Calendar now = Calendar.getInstance();

		List<Date> dates = getDatastore().query().target(NAMED_TARGET).list(QueryFunction.currentTimestamp());
		assertTrue(dates.size() > 0);
		Date date = dates.get(0);

		Calendar dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		long cnt = getDatastore().query().target(NAMED_TARGET)
				.filter(TMS.isNotNull().and(TMS.lt(QueryFunction.currentTimestamp()))).count();
		assertEquals(1L, cnt);

		OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(TMS, QueryFunction.currentTimestamp())
				.filter(KEY.eq(2L)).execute();
		assertEquals(1, result.getAffectedCount());

		date = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(TMS).orElse(null);
		assertNotNull(date);

		dc = Calendar.getInstance();
		dc.setTime(date);

		assertEquals(now.get(Calendar.YEAR), dc.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.MONTH), dc.get(Calendar.MONTH));
		assertEquals(now.get(Calendar.DAY_OF_MONTH), dc.get(Calendar.DAY_OF_MONTH));

		// LocalDateTime

		LocalDateTime lnow = LocalDateTime.now().withSecond(0).withNano(0);

		result = getDatastore().bulkUpdate(NAMED_TARGET).set(LTMS, QueryFunction.currentLocalDateTime())
				.filter(KEY.eq(1L)).execute();
		assertEquals(1, result.getAffectedCount());

		LocalDateTime ldate = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(LTMS).orElse(null);
		assertNotNull(ldate);

		ldate = ldate.withSecond(0).withNano(0);

		assertEquals(lnow.toLocalDate(), ldate.toLocalDate());

		cnt = getDatastore().query().target(NAMED_TARGET).filter(LTMS.loe(QueryFunction.currentLocalDateTime()))
				.count();
		assertEquals(2L, cnt);

	}

	@Test
	public void testTemporalFunctions() {
		Integer value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(Year.of(DAT))
				.orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(Year.of(LDAT)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(2016), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L))
				.findOne(com.holonplatform.core.query.TemporalFunction.Month.of(DAT)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L))
				.findOne(com.holonplatform.core.query.TemporalFunction.Month.of(LDAT)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(5), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(Day.of(DAT)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(Day.of(LDAT)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(19), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(Hour.of(TMS)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);

		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(Hour.of(LTMS)).orElse(null);
		assertNotNull(value);
		assertEquals(Integer.valueOf(15), value);

		long cnt = getDatastore().query().target(NAMED_TARGET)
				.filter(QueryFilter.eq(com.holonplatform.core.query.TemporalFunction.Month.of(LDAT), 5)).count();
		assertEquals(1L, cnt);
	}

	@Test
	public void testPropertyConversion() {
		List<Boolean> values = getDatastore().query().target(NAMED_TARGET).list(NBOOL);
		assertNotNull(values);
		Boolean value = values.get(0);
		assertNotNull(value);
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

		// Time

		LocalTime time = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(TIME).orElse(null);
		assertNotNull(time);
		assertEquals(18, time.getHour());
		assertEquals(30, time.getMinute());
		assertEquals(15, time.getSecond());

		long cnt = getDatastore().query().target(NAMED_TARGET).filter(TIME.eq(LocalTime.of(18, 30, 15))).count();
		assertEquals(1, cnt);

	}

	@Test
	public void testDateTime() {
		List<LocalDateTime> ltvalues = getDatastore().query().target(NAMED_TARGET)
				.filter(LTMS.eq(LocalDateTime.of(2017, Month.MARCH, 23, 15, 30, 25))).list(LTMS);
		assertNotNull(ltvalues);
		assertEquals(1, ltvalues.size());
	}

	@Test
	@Transactional
	@Rollback
	public void testClob() throws IOException {
		final PathProperty<String> CLOB1 = PathProperty.create("clb", String.class);
		final PathProperty<Reader> CLOB2 = PathProperty.create("clb", Reader.class);

		String value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB1).orElse(null);
		assertNotNull(value);
		assertEquals("clocbcontent", value);

		try (Reader reader = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB2)
				.orElse(null)) {
			assertNotNull(reader);
			value = "";
			int charv;
			while ((charv = reader.read()) != -1) {
				value += (char) charv;
			}
		}
		assertEquals("clocbcontent", value);

		// save
		PropertyBox box = PropertyBox.builder(KEY, STR, NBOOL, CLOB1).set(KEY, 77L).set(STR, "Test clob")
				.set(NBOOL, false).set(CLOB1, "savedclob").build();
		getDatastore().save(NAMED_TARGET, box);
		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(CLOB1).orElse(null);
		assertNotNull(value);
		assertEquals("savedclob", value);

		OperationResult deleted = getDatastore().delete(NAMED_TARGET, PropertyBox.builder(KEY).set(KEY, 77L).build());
		assertTrue(deleted.getAffectedCount() == 1);

		box = PropertyBox.builder(KEY, STR, NBOOL, CLOB2).set(KEY, 78L).set(STR, "Test clob").set(NBOOL, false)
				.set(CLOB2, new StringReader("savedclob")).build();
		getDatastore().save(NAMED_TARGET, box);
		value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(78L)).findOne(CLOB1).orElse(null);
		assertNotNull(value);
		assertEquals("savedclob", value);

		deleted = getDatastore().delete(NAMED_TARGET, PropertyBox.builder(KEY).set(KEY, 78L).build());
		assertTrue(deleted.getAffectedCount() == 1);

	}

	@Test
	@Transactional
	@Rollback
	public void testBlob() throws IOException {
		final PathProperty<InputStream> BLOB1 = PathProperty.create("blb", InputStream.class);
		final PathProperty<byte[]> BLOB2 = PathProperty.create("blb", byte[].class);

		try (InputStream is = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB1)
				.orElse(null)) {
			assertNotNull(is);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			byte[] bs = buffer.toByteArray();
			assertNotNull(bs);
			assertEquals(15, bs.length);
		}

		byte[] bs = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB2).orElse(null);
		assertNotNull(bs);
		assertEquals(15, bs.length);

		// save
		final byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

		PropertyBox box = PropertyBox.builder(KEY, STR, NBOOL, BLOB2).set(KEY, 87L).set(STR, "Test clob")
				.set(NBOOL, false).set(BLOB2, bytes).build();
		getDatastore().save(NAMED_TARGET, box);
		byte[] value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(87L)).findOne(BLOB2).orElse(null);
		assertNotNull(value);
		assertTrue(Arrays.equals(bytes, value));

		OperationResult deleted = getDatastore().delete(NAMED_TARGET, PropertyBox.builder(KEY).set(KEY, 87L).build());
		assertTrue(deleted.getAffectedCount() == 1);
	}

	@Test
	public void testMultiSelect() {

		List<PropertyBox> results = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc()).list(KEY, STR);

		assertNotNull(results);
		assertEquals(2, results.size());

		PropertyBox box = results.get(0);

		assertNotNull(box);

		Long key = box.getValue(KEY);

		assertNotNull(key);
		assertEquals(new Long(1), key);

		String str = box.getValue(STR);
		assertNotNull(str);
		assertEquals("One", str);
	}

	@Test
	public void testRestrictions() {
		List<String> str = getDatastore().query().target(NAMED_TARGET).restrict(1, 0).sort(KEY.asc()).list(STR);
		assertEquals(1, str.size());
		assertEquals("One", str.get(0));

		str = getDatastore().query().target(NAMED_TARGET).restrict(1, 1).sort(KEY.asc()).list(STR);
		assertEquals(1, str.size());
		assertEquals("Two", str.get(0));
	}

	@Test
	public void testSorts() {
		List<Long> res = getDatastore().query().target(NAMED_TARGET).sort(STR.desc()).sort(KEY.desc()).list(KEY);
		assertEquals(new Long(2), res.get(0));
	}

	@Test
	public void testFilters() {

		long count = getDatastore().query().target(NAMED_TARGET).filter(STR.eq("One")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(new NotFilter(STR.eq("One"))).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.eq("One").not()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.neq("Two")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.isNotNull()).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(DBL.isNull()).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.endsWith("x")).count();
		assertEquals(0, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.contains("w")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.containsIgnoreCase("O")).count();
		assertEquals(2, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.startsWith("O")).count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET).filter(STR.startsWithIgnoreCase("o")).count();
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

		count = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L).and(STR.eq("One"))).count();
		assertEquals(1, count);

	}

	@Test
	public void testAggregation() {
		List<Long> keys = getDatastore().query().target(NAMED_TARGET).aggregate(KEY).list(KEY);
		assertEquals(2, keys.size());

		List<String> vs = getDatastore().query().target(NAMED_TARGET).aggregate(STR, DBL).list(STR);
		assertEquals(2, vs.size());

		keys = getDatastore().query().target(NAMED_TARGET).filter(KEY.gt(1L))
				.aggregate(QueryAggregation.builder().path(KEY).build()).list(KEY);
		assertEquals(1, keys.size());

		List<Double> ds = getDatastore().query().target(NAMED_TARGET)
				.aggregate(QueryAggregation.builder().path(DBL).filter(DBL.gt(7d)).build()).list(DBL);
		assertEquals(1, ds.size());

		ds = getDatastore().query().target(NAMED_TARGET)
				.aggregate(QueryAggregation.builder().path(DBL).filter(QueryFilter.gt(DBL.sum(), 7d)).build())
				.list(DBL);
		assertEquals(1, ds.size());

		Property<Long> MAX_KEY = KEY.max();

		List<PropertyBox> pbs = getDatastore().query().target(NAMED_TARGET).sort(STR.asc()).aggregate(STR, DBL)
				.list(STR, MAX_KEY);
		assertEquals(2, pbs.size());
		assertEquals(Long.valueOf(1), pbs.get(0).getValue(MAX_KEY));
		assertEquals(Long.valueOf(2), pbs.get(1).getValue(MAX_KEY));
	}

	@Test
	@Transactional
	@Rollback
	public void testSaveDelete() {
		// insert
		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 3L).set(STR, "Three").set(NBOOL, false)
				.set(ENM, TestEnum.THIRD).build();
		getDatastore().save(NAMED_TARGET, box);

		PropertyBox saved = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(3L)).findOne(PROPS).orElse(null);
		assertNotNull(saved);
		assertEquals("Three", saved.getValue(STR));

		// update
		saved.setValue(STR, "Three UPD");
		getDatastore().save(NAMED_TARGET, saved);

		saved = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(3L)).findOne(PROPS).orElse(null);
		assertNotNull(saved);
		assertEquals("Three UPD", saved.getValue(STR));

		// delete
		OperationResult deleted = getDatastore().delete(NAMED_TARGET, box);
		assertTrue(deleted.getAffectedCount() == 1);
	}

	@Test
	@Transactional
	@Rollback
	public void testSaveDeleteByKey() {
		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 3L).set(STR, "Three").set(NBOOL, true)
				.set(ENM, TestEnum.THIRD).set(DAT, new Date()).set(LDAT, LocalDate.of(2017, Month.MARCH, 24)).build();
		getDatastore().save(NAMED_TARGET, box);

		OperationResult deleted = getDatastore().delete(NAMED_TARGET, PropertyBox.builder(KEY).set(KEY, 3L).build());
		assertTrue(deleted.getAffectedCount() == 1);
	}

	@Test
	@Transactional
	@Rollback
	public void testSaveDeleteByTargetKey() {

		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 4L).set(STR, "Three").set(NBOOL, true).build();
		getDatastore().save(NAMED_TARGET, box);

		OperationResult deleted = getDatastore().delete(NAMED_TARGET, PropertyBox.builder(KEY).set(KEY, 4L).build());
		assertTrue(deleted.getAffectedCount() == 1);
	}

	@Test
	public void testRefresh() {
		PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPS).orElse(null);
		assertNotNull(value);

		PropertyBox refreshed = getDatastore().refresh(NAMED_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
	}

	@Test
	@Transactional
	@Rollback
	public void testBulkDelete() {
		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 10L).set(STR, "k10").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);
		box = PropertyBox.builder(PROPS).set(KEY, 11L).set(STR, "k11").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);
		box = PropertyBox.builder(PROPS).set(KEY, 12L).set(STR, "k12").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);

		OperationResult result = getDatastore().bulkDelete(NAMED_TARGET).filter(KEY.between(10L, 12L)).execute();
		assertEquals(3, result.getAffectedCount());
	}

	@Test
	@Transactional
	@Rollback
	public void testBulkUpdate() {
		PropertyBox box = PropertyBox.builder(PROPS).set(KEY, 10L).set(STR, "k10").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);
		box = PropertyBox.builder(PROPS).set(KEY, 11L).set(STR, "k11").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);
		box = PropertyBox.builder(PROPS).set(KEY, 12L).set(STR, "k12").set(NBOOL, false).build();
		getDatastore().save(NAMED_TARGET, box);

		OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).filter(KEY.between(10L, 12L)).set(STR, "UPD")
				.setNull(DAT).execute();
		assertEquals(3, result.getAffectedCount());

		List<String> values = getDatastore().query().target(NAMED_TARGET).filter(KEY.between(10L, 12L)).list(STR);
		assertEquals(3, values.size());
		values.forEach(v -> assertEquals("UPD", v));

		result = getDatastore().bulkDelete(NAMED_TARGET).filter(KEY.between(10L, 12L)).execute();
		assertEquals(3, result.getAffectedCount());
	}

	@Test
	@Transactional
	@Rollback
	public void testBulkInsert() {
		OperationResult result = getDatastore().bulkInsert(NAMED_TARGET, PropertySet.of(KEY, STR, NBOOL))
				.add(PropertyBox.builder(PROPS).set(KEY, 201L).set(STR, "k201").set(NBOOL, false).build())
				.add(PropertyBox.builder(PROPS).set(KEY, 202L).set(STR, "k202").set(NBOOL, false).build())
				.add(PropertyBox.builder(PROPS).set(KEY, 203L).set(STR, "k203").set(NBOOL, false).build())
				.add(PropertyBox.builder(PROPS).set(KEY, 204L).set(STR, "k204").set(NBOOL, false).build())
				.add(PropertyBox.builder(PROPS).set(KEY, 205L).set(STR, "k205").set(NBOOL, false).build()).execute();
		assertEquals(5, result.getAffectedCount());

		result = getDatastore().bulkDelete(NAMED_TARGET).filter(KEY.gt(200L)).execute();
		assertEquals(5, result.getAffectedCount());
	}

	@Test
	public void testProjectionBean() {
		List<TestProjectionBean> results = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc())
				.list(BeanProjection.of(TestProjectionBean.class));
		assertNotNull(results);
		assertEquals(2, results.size());

		assertEquals(1L, results.get(0).getKeycode());
		assertEquals("One", results.get(0).getStrv());

		assertEquals(2L, results.get(1).getKeycode());
		assertEquals("Two", results.get(1).getStrv());
	}

	@Test
	public void testWhereFilter() {
		long count = getDatastore().query().target(NAMED_TARGET)
				.filter(JdbcWhereFilter.create("keycode = ?", Long.valueOf(1))).count();
		assertEquals(1, count);
	}

	@Test
	@Transactional
	public void testCustomFilter() {

		long count = getDatastore().query().target(NAMED_TARGET).filter(new KeyIs(1)).count();
		assertEquals(1, count);

		Optional<String> str = getDatastore().query().target(NAMED_TARGET).filter(new KeyIs(1)).findOne(STR);
		assertEquals("One", str.get());

		OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(ENM, TestEnum.THIRD).filter(new KeyIs(1))
				.execute();
		assertEquals(1, result.getAffectedCount());

		result = getDatastore().bulkUpdate(NAMED_TARGET).set(ENM, TestEnum.FIRST).filter(new KeyIs(1)).execute();
		assertEquals(1, result.getAffectedCount());

		Optional<PropertyBox> pb = getDatastore().query().target(NAMED_TARGET).filter(new KeyIs(2)).findOne(PROPS);
		assertEquals(TestEnum.SECOND, pb.get().getValue(ENM));

		result = getDatastore().bulkUpdate(NAMED_TARGET).filter(new KeyIs(2)).setNull(DAT).execute();
		assertEquals(1, result.getAffectedCount());

		pb = getDatastore().query().target(NAMED_TARGET).filter(new KeyIs(1)).findOne(PROPS);
		assertEquals("One", pb.get().getValue(STR));
	}

	private final static DataTarget<String> TEST3 = DataTarget.named("test3");

	private final static PathProperty<Long> TEST3_CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEST3_TEXT = PathProperty.create("text", String.class);

	private final static PathProperty<Long> TEST3_CODE_P = PathProperty.create("code", long.class).parent(TEST3);
	private final static PathProperty<String> TEST3_TEXT_P = PathProperty.create("text", String.class).parent(TEST3);

	@Test
	public void testJoins() {
		long key = getDatastore().query().target(TEST3).filter(TEST3_CODE.eq(2L)).findOne(TEST3_CODE).orElse(null);
		assertEquals(2, key);

		List<PropertyBox> results = getDatastore().query()
				.target(RelationalTarget.of(TEST3).innerJoin(NAMED_TARGET).on(KEY_P.eq(TEST3_CODE)).add())
				.list(TEST3_TEXT, STR_P);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("Two", results.get(0).getValue(STR_P));
		assertEquals("TestJoin", results.get(0).getValue(TEST3_TEXT));

		results = getDatastore().query()
				.target(RelationalTarget.of(TEST3).innerJoin(NAMED_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
				.list(TEST3_TEXT_P, STR_P);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("Two", results.get(0).getValue(STR_P));
		assertEquals("TestJoin", results.get(0).getValue(TEST3_TEXT_P));

		results = getDatastore().query()
				.target(RelationalTarget.of(TEST3).innerJoin(NAMED_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
				.list(TEST3_TEXT_P, STR_P);
		assertNotNull(results);
		assertEquals(1, results.size());

		results = getDatastore().query()
				.target(RelationalTarget.of(NAMED_TARGET).leftJoin(TEST3).on(KEY_P.eq(TEST3_CODE_P)).add())
				.list(TEST3_TEXT_P, STR_P);
		assertNotNull(results);
		assertEquals(2, results.size());
	}

	@Test
	public void testRightJoins() {

		List<PropertyBox> results = getDatastore().query()
				.target(RelationalTarget.of(NAMED_TARGET).rightJoin(TEST3).on(KEY_P.eq(TEST3_CODE_P)).add())
				.list(TEST3_TEXT_P, STR_P);
		assertNotNull(results);
		assertEquals(2, results.size());

		results = getDatastore().query()
				.target(RelationalTarget.of(TEST3).rightJoin(NAMED_TARGET).on(KEY_P.eq(TEST3_CODE_P)).add())
				.list(TEST3_TEXT_P, STR_P);
		assertNotNull(results);
		assertEquals(2, results.size());
	}

	@Test
	public void testSubQuery() {

		long count = getDatastore().query().target(NAMED_TARGET).filter(KEY.in(SubQuery
				.create(getDatastore(), Long.class).target(TEST3).filter(TEST3_TEXT.eq("TestJoin")).select(TEST3_CODE)))
				.count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(KEY
						.nin(SubQuery.create(getDatastore(), TEST3_CODE).target(TEST3).filter(TEST3_CODE.isNotNull())))
				.count();
		assertEquals(1, count);

		final PathProperty<Long> T_KEY = KEY.clone().parent(NAMED_TARGET);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(SubQuery.create(getDatastore()).target(TEST3).filter(TEST3_CODE.eq(T_KEY)).exists()).count();
		assertEquals(1, count);

		final PathProperty<Long> D_KEY = NAMED_TARGET.property(KEY);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(SubQuery.create(getDatastore()).target(TEST3).filter(TEST3_CODE.eq(D_KEY)).notExists()).count();
		assertEquals(1, count);

		// explicit alias

		final RelationalTarget<String> AT = RelationalTarget.of(NAMED_TARGET).alias("parent");
		final PathProperty<Long> A_KEY = AT.property(KEY);

		count = getDatastore().query().target(AT)
				.filter(SubQuery.create(getDatastore()).target(TEST3).filter(TEST3_CODE.eq(A_KEY)).notExists()).count();
		assertEquals(1, count);

		final RelationalTarget<String> AT2 = RelationalTarget.of(TEST3).alias("sub");
		final PathProperty<Long> A2_KEY = AT2.property(TEST3_CODE);

		count = getDatastore().query().target(AT)
				.filter(SubQuery.create(getDatastore()).target(AT2).filter(A2_KEY.eq(A_KEY)).notExists()).count();
		assertEquals(1, count);

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testDataTargetResolver() {

		ExpressionResolver<DataTarget, DataTarget> dr = DataTargetResolver.create(DataTarget.class,
				(t, c) -> "#testres#".equals(t.getName()) ? Optional.of(DataTarget.named("test1")) : Optional.empty());

		getDatastore().addExpressionResolver(dr);

		long count = getDatastore().query().target(DataTarget.named("#testres#")).filter(STR.eq("One")).count();
		assertEquals(1, count);

		// relational

		getDatastore().addExpressionResolver(
				DataTargetResolver.create(DataTarget.class, (t, c) -> "#testres2#".equals(t.getName())
						? Optional.of(RelationalTarget.of(NAMED_TARGET)) : Optional.empty()));

		count = getDatastore().query().target(DataTarget.named("#testres#")).filter(STR.eq("One")).count();
		assertEquals(1, count);
	}

}
