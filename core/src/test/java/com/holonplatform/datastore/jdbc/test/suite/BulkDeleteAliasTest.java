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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_CODE;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_SET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_TEXT;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.property.PropertyBox;

public class BulkDeleteAliasTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testBulkDeleteSubquery() {
		if (AbstractJdbcDatastoreTestSuite.updateAliasTest) {

			inTransaction(() -> {

				PropertyBox t3 = PropertyBox.builder(TEST3_SET).set(TEST3_CODE, 501L).set(TEST3_TEXT, "Two").build();
				OperationResult result = getDatastore().insert(TEST3, t3);
				assertEquals(1, result.getAffectedCount());

				final SubQuery<?> sq = SubQuery.create().target(TEST3)
						.filter(TEST3_TEXT.eq(NAMED_TARGET.property(STR1)));

				List<PropertyBox> values = getDatastore().query().target(NAMED_TARGET).filter(sq.exists())
						.list(PROPERTIES);
				assertEquals(1, values.size());
				assertEquals(Long.valueOf(2), values.get(0).getValue(KEY));

				result = getDatastore().bulkDelete(NAMED_TARGET).filter(sq.exists()).execute();
				assertEquals(1, result.getAffectedCount());

				long cnt = getDatastore().query(NAMED_TARGET).filter(KEY.eq(2L)).count();
				assertEquals(0, cnt);

			});

		} else {
			LOGGER.info("SKIP delete with alias test");
		}
	}

}
