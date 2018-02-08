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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DBL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.ENM;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LDAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LTMS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_NMB;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_TXT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_DEC;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_STR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES_NOID;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TIME;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.data.TestSampleData;

public class SaveTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testSaveAsInsert() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 401L).set(STR, "k401").set(DBL, 7.45)
					.set(DAT, TestSampleData.DATE1).set(LDAT, TestSampleData.LDATE1).set(ENM, TestEnum.SECOND)
					.set(NST_STR, "str1").set(NST_DEC, TestSampleData.BD1).set(NBOOL, false)
					.set(TMS, TestSampleData.DATETIME1).set(LTMS, TestSampleData.LDATETIME1)
					.set(TIME, TestSampleData.LTIME1).build();

			OperationResult result = getDatastore().save(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

			value = getDatastore().query(NAMED_TARGET).filter(KEY.eq(401L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(401), value.getValue(KEY));
			assertEquals("k401", value.getValue(STR));
			assertEquals(Double.valueOf(7.45), value.getValue(DBL));
			assertEquals(TestSampleData.DATE1, value.getValue(DAT));
			assertEquals(TestSampleData.LDATE1, value.getValue(LDAT));
			assertEquals(TestEnum.SECOND, value.getValue(ENM));
			assertEquals("str1", value.getValue(NST_STR));
			assertEquals(TestSampleData.BD1, value.getValue(NST_DEC));
			assertFalse(value.getValue(NBOOL));
			assertEquals(TestSampleData.DATETIME1, value.getValue(TMS));
			assertEquals(TestSampleData.LDATETIME1, value.getValue(LTMS));
			assertEquals(TestSampleData.LTIME1, value.getValue(TIME));

		});
	}

	@Test
	public void testSaveAsUpdate() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 1L).set(STR, "k401").set(DBL, 7.45)
					.set(DAT, TestSampleData.DATE1).set(LDAT, TestSampleData.LDATE1).set(ENM, TestEnum.SECOND)
					.set(NST_STR, "str1").set(NST_DEC, TestSampleData.BD1).set(NBOOL, false)
					.set(TMS, TestSampleData.DATETIME1).set(LTMS, TestSampleData.LDATETIME1)
					.set(TIME, TestSampleData.LTIME1).build();

			OperationResult result = getDatastore().save(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.UPDATE, result.getOperationType().orElse(null));

			value = getDatastore().query(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(1), value.getValue(KEY));
			assertEquals("k401", value.getValue(STR));
			assertEquals(Double.valueOf(7.45), value.getValue(DBL));
			assertEquals(TestSampleData.DATE1, value.getValue(DAT));
			assertEquals(TestSampleData.LDATE1, value.getValue(LDAT));
			assertEquals(TestEnum.SECOND, value.getValue(ENM));
			assertEquals("str1", value.getValue(NST_STR));
			assertEquals(TestSampleData.BD1, value.getValue(NST_DEC));
			assertFalse(value.getValue(NBOOL));
			assertEquals(TestSampleData.DATETIME1, value.getValue(TMS));
			assertEquals(TestSampleData.LDATETIME1, value.getValue(LTMS));
			assertEquals(TestSampleData.LTIME1, value.getValue(TIME));

		});
	}

	@Test
	public void testSaveNoId() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES_NOID).set(KEY, 501L).set(STR, "k501").set(NBOOL, true)
					.build();

			OperationResult result = getDatastore().save(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

			value = getDatastore().query(NAMED_TARGET).filter(KEY.eq(501L)).findOne(PROPERTIES_NOID).orElse(null);
			assertNotNull(value);
			assertEquals("k501", value.getValue(STR));

			value.setValue(STR, "uxs");

			result = getDatastore().save(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.UPDATE, result.getOperationType().orElse(null));

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(501L)).findOne(PROPERTIES_NOID)
					.orElse(null);
			assertNotNull(value);
			assertEquals("uxs", value.getValue(STR));

		});
	}

	@Test
	public void testSaveFallback() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(NOPK_NMB, NOPK_TXT).set(NOPK_NMB, 77).set(NOPK_TXT, "t77").build();

			OperationResult result = getDatastore().save(NOPK_TARGET, value);
			assertEquals(1, result.getAffectedCount());
			assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

			value = getDatastore().query(NOPK_TARGET).filter(NOPK_NMB.eq(77)).findOne(NOPK_NMB, NOPK_TXT).orElse(null);
			assertNotNull(value);
			assertEquals("t77", value.getValue(NOPK_TXT));

		});
	}

	@Test(expected = DataAccessException.class)
	public void testSaveFailure() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(NOPK_NMB, NOPK_TXT).set(NOPK_NMB, 77).set(NOPK_TXT, "t77").build();

			getDatastore().save(NOPK_TARGET, value, DefaultWriteOption.SAVE_DISABLE_INSERT_FALLBACK);

		});
	}

}
