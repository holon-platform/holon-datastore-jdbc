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

import static com.holonplatform.datastore.jdbc.test.data.TestDataBean.CODE;
import static com.holonplatform.datastore.jdbc.test.data.TestDataBean.PROPERTIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.beans.BeanDatastore;
import com.holonplatform.core.datastore.beans.BeanDatastore.BeanOperationResult;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.datastore.jdbc.test.data.TestDataBean;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.data.TestSampleData;

public class BeanDatastoreTest extends AbstractJdbcDatastoreSuiteTest {

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

		TestDataBean value2 = getBeanDatastore().query(TestDataBean.class).filter(PROPERTIES.property("code").eq(2L))
				.findOne().orElse(null);
		assertNotNull(value2);
		assertEquals(2L, value2.getCode());

		TestDataBean refreshed2 = getBeanDatastore().refresh(value2);
		assertNotNull(refreshed2);
		assertEquals(2L, refreshed2.getCode());

		inTransaction(() -> {
			TestDataBean value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne()
					.orElse(null);
			assertNotNull(value);
			assertEquals("One", value.getStringValue());

			// update string value
			BeanOperationResult<TestDataBean> result = getBeanDatastore().bulkUpdate(TestDataBean.class)
					.set(PROPERTIES.property("stringValue"), "OneX").filter(CODE.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			// refresh
			TestDataBean refreshed = getBeanDatastore().refresh(value);
			assertNotNull(refreshed);
			assertEquals(1L, refreshed.getCode());
			assertEquals("OneX", refreshed.getStringValue());

		});

		final TestDataBean valueNoId = new TestDataBean();
		valueNoId.setStringValue("NoId");

		TestUtils.expectedException(DataAccessException.class, () -> {
			getBeanDatastore().refresh(valueNoId);
		});

	}

	@Test
	public void testInsert() {

		inTransaction(() -> {

			TestDataBean value = new TestDataBean();
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

			value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(301L)).findOne().orElse(null);
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

			TestDataBean value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne()
					.orElse(null);
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

			value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
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

			TestDataBean value = new TestDataBean();
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

			value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(401L)).findOne().orElse(null);
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

			TestDataBean value = new TestDataBean();
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

			value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne().orElse(null);
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
	public void testBulkInsert() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkInsert(TestDataBean.class)
					.add(new TestDataBean(201L, "k201")).add(new TestDataBean(202L, "k202"))
					.add(new TestDataBean(203L, "k203")).add(new TestDataBean(204L, "k204"))
					.add(new TestDataBean(205L, "k205")).execute();

			assertEquals(5, result.getAffectedCount());

			List<TestDataBean> vals = getBeanDatastore().query(TestDataBean.class).filter(CODE.between(201L, 205L))
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

			BeanOperationResult<?> result = getBeanDatastore().bulkUpdate(TestDataBean.class)
					.set(PROPERTIES.property("stringValue"), "upd").set("booleanValue", false).filter(CODE.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			TestDataBean value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne()
					.orElse(null);
			assertNotNull(value);
			assertEquals("upd", value.getStringValue());
			assertFalse(value.isBooleanValue());

			result = getBeanDatastore().bulkUpdate(TestDataBean.class).set("stringValue", "updx").filter(CODE.loe(2L))
					.execute();
			assertEquals(2, result.getAffectedCount());

			Stream<String> vals = getBeanDatastore().query(TestDataBean.class).filter(CODE.loe(2L)).stream()
					.map(r -> r.getStringValue());
			vals.forEach(v -> assertEquals("updx", v));
		});
	}

	@Test
	public void testBulkUpdateNulls() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkUpdate(TestDataBean.class).setNull("stringValue")
					.set(PROPERTIES.property("decv"), 557.88).filter(CODE.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			TestDataBean value = getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne()
					.orElse(null);
			assertNotNull(value);
			assertNull(value.getStringValue());
			assertEquals(Double.valueOf(557.88), value.getDecv());

		});
	}

	@Test
	public void testBulkDelete() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkDelete(TestDataBean.class).filter(CODE.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			assertFalse(getBeanDatastore().query(TestDataBean.class).filter(CODE.eq(1L)).findOne().isPresent());
		});
	}

	@Test
	public void testBulkDeleteMulti() {
		inTransaction(() -> {

			BeanOperationResult<?> result = getBeanDatastore().bulkDelete(TestDataBean.class).filter(CODE.loe(2L))
					.execute();
			assertEquals(2, result.getAffectedCount());

			assertEquals(0, getBeanDatastore().query(TestDataBean.class).count());
		});
	}

}
