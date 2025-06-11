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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_DEC;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_STR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES_V;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TIME;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TMS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.VIRTUAL_STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.data.TestSampleData;

public class InsertTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testInsert() {

		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 301L).set(STR1, "k301").set(DBL, 7.45)
					.set(DAT, TestSampleData.DATE1).set(LDAT, TestSampleData.LDATE1).set(ENM, TestEnum.SECOND)
					.set(NST_STR, "str1").set(NST_DEC, TestSampleData.BD1).set(NBOOL, false)
					.set(TMS, TestSampleData.DATETIME1).set(LTMS, TestSampleData.LDATETIME1)
					.set(TIME, TestSampleData.LTIME1).build();

			OperationResult result = getDatastore().insert(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(301L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(301), value.getValue(KEY));
			assertEquals("k301", value.getValue(STR1));
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
	public void testInsertVirtual() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES_V).set(KEY, 301L).set(STR1, "k301").set(NBOOL, true)
					.build();
			OperationResult result = getDatastore().insert(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(301L)).findOne(PROPERTIES_V).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(301), value.getValue(KEY));
			assertEquals("k301", value.getValue(STR1));
			assertTrue(value.getValue(NBOOL));
			assertEquals("[k301]", value.getValue(VIRTUAL_STR));

		});
	}

	@Test
	public void testInsertDefaults() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 301L).set(STR1, "k301").set(NBOOL, true)
					.build();
			OperationResult result = getDatastore().insert(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(301L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals(Long.valueOf(301), value.getValue(KEY));
			assertEquals("k301", value.getValue(STR1));
			assertTrue(value.getValue(NBOOL));
			assertEquals("nst1", value.getValue(NST_STR));

		});
	}

}
