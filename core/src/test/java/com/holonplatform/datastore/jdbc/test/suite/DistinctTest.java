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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_CODE;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;

public class DistinctTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testDistinctSingle() {
		inTransaction(() -> {

			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 101L).set(TEST3_TEXT, "v1").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 102L).set(TEST3_TEXT, "v2").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 103L).set(TEST3_TEXT, "v1").build());

			List<String> values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).list(TEST3_TEXT);
			assertEquals(3, values.size());

			values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).distinct().list(TEST3_TEXT);
			assertEquals(2, values.size());
			assertTrue(values.contains("v1"));
			assertTrue(values.contains("v2"));

		});
	}

	@Test
	public void testDistinctMultiple() {
		inTransaction(() -> {

			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 101L).set(TEST3_TEXT, "v1").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 102L).set(TEST3_TEXT, "v2").build());
			getDatastore().insert(TEST3,
					PropertyBox.builder(TEST3_CODE, TEST3_TEXT).set(TEST3_CODE, 103L).set(TEST3_TEXT, "v1").build());

			List<PropertyBox> values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).list(TEST3_CODE,
					TEST3_TEXT);
			assertEquals(3, values.size());

			values = getDatastore().query(TEST3).filter(TEST3_CODE.goe(101L)).distinct().list(TEST3_CODE, TEST3_TEXT);
			assertEquals(3, values.size());

			List<Long> keys = values.stream().map(v -> v.getValue(TEST3_CODE)).collect(Collectors.toList());
			assertTrue(keys.contains(Long.valueOf(101L)));
			assertTrue(keys.contains(Long.valueOf(102L)));
			assertTrue(keys.contains(Long.valueOf(103L)));

		});
	}

}
