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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.*;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_NMB;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_TXT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES_NOID;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PropertyBox;

public class RefreshTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testRefresh() {
		PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
				.orElse(null);
		assertNotNull(value);

		PropertyBox refreshed = getDatastore().refresh(NAMED_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
	}

	@Test
	public void testRefreshVirtual() {
		PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_V)
				.orElse(null);
		assertNotNull(value);
		assertEquals("[One]", value.getValue(VIRTUAL_STR));

		PropertyBox refreshed = getDatastore().refresh(NAMED_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
		assertEquals("[One]", refreshed.getValue(VIRTUAL_STR));
	}

	@Test
	public void testUpdateRefresh() {

		inTransaction(() -> {
			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);
			assertEquals("One", value.getValue(STR));

			// update STR value
			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(STR, "OneX").filter(KEY.eq(1L))
					.execute();
			assertEquals(1, result.getAffectedCount());

			// refresh
			PropertyBox refreshed = getDatastore().refresh(NAMED_TARGET, value);
			assertNotNull(refreshed);
			assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
			assertEquals("OneX", refreshed.getValue(STR));

		});
	}

	@Test
	public void testRefreshNoId() {
		PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_NOID)
				.orElse(null);
		assertNotNull(value);

		PropertyBox refreshed = getDatastore().refresh(NAMED_TARGET, value);
		assertNotNull(refreshed);
		assertEquals(Long.valueOf(1), refreshed.getValue(KEY));
	}

	@Test(expected = DataAccessException.class)
	public void testRefreshMissingKey() {
		PropertyBox value = PropertyBox.builder(PROPERTIES).set(STR, "test").build();

		getDatastore().refresh(NAMED_TARGET, value);
	}

	@Test(expected = DataAccessException.class)
	public void testRefreshMissingPk() {
		PropertyBox value = getDatastore().query().target(NOPK_TARGET).filter(NOPK_NMB.eq(1))
				.findOne(NOPK_NMB, NOPK_TXT).orElse(null);
		assertNotNull(value);

		getDatastore().refresh(NOPK_TARGET, value);
	}

}
