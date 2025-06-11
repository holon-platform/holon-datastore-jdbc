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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NST_STR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;

public class BulkInsertTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testBulkInsert() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkInsert(NAMED_TARGET, PropertySet.of(KEY, STR1, NBOOL))
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 201L).set(STR1, "k201").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 202L).set(STR1, "k202").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 203L).set(STR1, "k203").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 204L).set(STR1, "k204").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 205L).set(STR1, "k205").set(NBOOL, false).build())
					.execute();

			assertEquals(5, result.getAffectedCount());

			List<String> vals = getDatastore().query(NAMED_TARGET).filter(KEY.between(201L, 205L)).sort(KEY.asc())
					.list(STR1);
			assertEquals(5, vals.size());
			assertEquals("k201", vals.get(0));
			assertEquals("k202", vals.get(1));
			assertEquals("k203", vals.get(2));
			assertEquals("k204", vals.get(3));
			assertEquals("k205", vals.get(4));

		});
	}

	@Test
	public void testBulkInsertDefaults() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkInsert(NAMED_TARGET, PropertySet.of(KEY, STR1, NBOOL))
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 201L).set(STR1, "k201").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 202L).set(STR1, "k202").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 203L).set(STR1, "k203").set(NBOOL, false).build())
					.execute();

			assertEquals(3, result.getAffectedCount());

			String nst = getDatastore().query(NAMED_TARGET).filter(KEY.eq(201L)).findOne(NST_STR).orElse(null);
			assertEquals("nst1", nst);
			nst = getDatastore().query(NAMED_TARGET).filter(KEY.eq(202L)).findOne(NST_STR).orElse(null);
			assertEquals("nst1", nst);
			nst = getDatastore().query(NAMED_TARGET).filter(KEY.eq(203L)).findOne(NST_STR).orElse(null);
			assertEquals("nst1", nst);

		});
	}

	@Test
	public void testBulkInsertPropertySet() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkInsert(NAMED_TARGET, PropertySet.of(PROPERTIES))
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 201L).set(STR1, "k201").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 202L).set(STR1, "k202").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 203L).set(STR1, "k203").set(NBOOL, false).build())
					.execute();

			assertEquals(3, result.getAffectedCount());

			Stream<String> nsts = getDatastore().query(NAMED_TARGET).filter(KEY.in(201L, 202L, 203L)).stream(NST_STR);
			nsts.forEach(nst -> assertNull(nst));

		});
	}

	@Test
	public void testBulkInsertPropertySets() {
		inTransaction(() -> {

			OperationResult result = getDatastore().bulkInsert(NAMED_TARGET, PropertySet.of(PROPERTIES))
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 201L).set(STR1, "k201").set(NBOOL, false).build())
					.add(PropertyBox.builder(PROPERTIES).set(KEY, 202L).set(STR1, "k202").set(NBOOL, true).set(DBL, 3.2)
							.build())
					.add(PropertyBox.builder(KEY, NBOOL, NST_STR).set(KEY, 203L).set(NST_STR, "ns203").set(NBOOL, false)
							.build())
					.execute();

			assertEquals(3, result.getAffectedCount());

			List<PropertyBox> vals = getDatastore().query(NAMED_TARGET).filter(KEY.between(201L, 203L)).sort(KEY.asc())
					.list(PROPERTIES);
			assertEquals(3, vals.size());
			assertEquals("k201", vals.get(0).getValue(STR1));
			assertFalse(vals.get(0).getValue(NBOOL));
			assertEquals("k202", vals.get(1).getValue(STR1));
			assertTrue(vals.get(1).getValue(NBOOL));
			assertEquals(Double.valueOf(3.2), vals.get(1).getValue(DBL));
			assertEquals("ns203", vals.get(2).getValue(NST_STR));
		});
	}

}
