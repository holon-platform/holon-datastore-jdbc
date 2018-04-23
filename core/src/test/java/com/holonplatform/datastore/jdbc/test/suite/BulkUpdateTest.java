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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DBL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.stream.Stream;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;

public class BulkUpdateTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testBulkUpdate() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR, "upd").set(NBOOL, false)
					.filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);
			assertEquals("upd", value.getValue(STR));
			assertFalse(value.getValue(NBOOL));

			String sv = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(2L)).findOne(STR).orElse(null);
			assertNotNull(sv);
			assertEquals("Two", sv);

			result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR, "updx").filter(KEY.loe(2L)).execute();
			assertEquals(2, result.getAffectedCount());

			Stream<String> vals = getDatastore().query().target(NAMED_TARGET).filter(KEY.loe(2L)).stream(STR);
			vals.forEach(v -> assertEquals("updx", v));
		});
	}

	@Test
	public void testBulkUpdateNulls() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).setNull(STR).set(DBL, 557.88)
					.filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);
			assertNull(value.getValue(STR));
			assertEquals(Double.valueOf(557.88), value.getValue(DBL));

		});
	}

	@Test
	public void testBulkUpdatePropertyBox() {
		inTransaction(() -> {

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);
			value.setValue(STR, "updpb");
			value.setValue(DBL, null);

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(value).filter(KEY.eq(1L)).execute();
			assertEquals(1, result.getAffectedCount());

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).orElse(null);
			assertNotNull(value);
			assertEquals("updpb", value.getValue(STR));
			assertNull(value.getValue(DBL));

		});
	}

	@Test
	public void testBulkUpdateAll() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR, "all").execute();
			assertEquals(2, result.getAffectedCount());

			Stream<String> vals = getDatastore().query().target(NAMED_TARGET).stream(STR);
			vals.forEach(v -> assertEquals("all", v));

		});
	}

}
