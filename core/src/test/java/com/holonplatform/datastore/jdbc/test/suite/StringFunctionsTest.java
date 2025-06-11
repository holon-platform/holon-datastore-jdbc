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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;

public class StringFunctionsTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testLower() {
		String str = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(STR1.lower()).orElse(null);
		assertNotNull(str);
		assertEquals("one", str);

		Long key = getDatastore().query().target(NAMED_TARGET).filter(STR1.lower().eq("one")).findOne(KEY).orElse(null);
		assertNotNull(key);
		assertEquals(Long.valueOf(1L), key);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR1, STR1.lower()).filter(KEY.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			String v = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(STR1).orElse(null);
			assertNotNull(v);
			assertEquals("one", v);

		});
	}

	@Test
	public void testUpper() {
		String str = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(STR1.upper()).orElse(null);
		assertNotNull(str);
		assertEquals("ONE", str);

		Long key = getDatastore().query().target(NAMED_TARGET).filter(STR1.upper().eq("ONE")).findOne(KEY).orElse(null);
		assertNotNull(key);
		assertEquals(Long.valueOf(1L), key);

		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR1, STR1.upper()).filter(KEY.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			String v = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(STR1).orElse(null);
			assertNotNull(v);
			assertEquals("ONE", v);

		});
	}

}
