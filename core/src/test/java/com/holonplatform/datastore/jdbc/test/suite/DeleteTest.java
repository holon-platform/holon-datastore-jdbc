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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_NMB;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NOPK_TXT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES_NOID;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PropertyBox;

public class DeleteTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testDelete() {
		inTransaction(() -> {

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
					.orElse(null);
			assertNotNull(value);

			OperationResult result = getDatastore().delete(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			assertFalse(getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).isPresent());

		});
	}

	@Test
	public void testDeleteUsingKey() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 1L).build();

			OperationResult result = getDatastore().delete(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			assertFalse(getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES).isPresent());

		});
	}

	@Test
	public void testDeleteNoId() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES_NOID).set(KEY, 1L).build();

			OperationResult result = getDatastore().delete(NAMED_TARGET, value);
			assertEquals(1, result.getAffectedCount());

			assertFalse(getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES_NOID)
					.isPresent());

		});
	}

	@Test(expected = DataAccessException.class)
	public void testDeleteMissingKey() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(STR, "test").build();
			getDatastore().delete(NAMED_TARGET, value);

		});
	}

	@Test(expected = DataAccessException.class)
	public void testDeleteMissingPk() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(NOPK_NMB, NOPK_TXT).set(NOPK_NMB, 1).set(NOPK_TXT, "x").build();
			getDatastore().delete(NAMED_TARGET, value);

		});
	}

}
