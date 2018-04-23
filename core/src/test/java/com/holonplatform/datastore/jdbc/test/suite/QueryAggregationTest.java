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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.ENM;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryAggregation;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;

public class QueryAggregationTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testAggregation() {
		List<Long> keys = getDatastore().query().target(NAMED_TARGET).aggregate(KEY).list(KEY);
		assertEquals(2, keys.size());

		List<String> vs = getDatastore().query().target(NAMED_TARGET).aggregate(STR, DBL).sort(STR.asc()).list(STR);
		assertEquals(2, vs.size());
		assertEquals("One", vs.get(0));
		assertEquals("Two", vs.get(1));

		keys = getDatastore().query().target(NAMED_TARGET).filter(KEY.gt(1L))
				.aggregate(QueryAggregation.builder().path(KEY).build()).list(KEY);
		assertEquals(1, keys.size());

		List<Double> ds = getDatastore().query().target(NAMED_TARGET)
				.aggregate(QueryAggregation.builder().path(DBL).filter(DBL.gt(7d)).build()).list(DBL);
		assertEquals(1, ds.size());
		assertEquals(Double.valueOf(7.4), ds.get(0));

		// having

		ds = getDatastore().query().target(NAMED_TARGET)
				.aggregate(QueryAggregation.builder().path(DBL).filter(QueryFilter.gt(DBL.sum(), 7d)).build())
				.list(DBL);
		assertEquals(1, ds.size());
		assertEquals(Double.valueOf(7.4), ds.get(0));

	}

	@Test
	public void testAggregationMulti() {
		inTransaction(() -> {

			PropertyBox value = PropertyBox.builder(PROPERTIES).set(KEY, 701L).set(STR, "One").set(DBL, 1.2)
					.set(ENM, TestEnum.THIRD).set(NBOOL, false).build();

			OperationResult or = getDatastore().insert(NAMED_TARGET, value);
			assertEquals(1, or.getAffectedCount());

			assertEquals(3, getDatastore().query(NAMED_TARGET).count());

			List<String> strs = getDatastore().query(NAMED_TARGET).aggregate(STR).sort(STR.asc()).list(STR);
			assertEquals(2, strs.size());
			assertEquals("One", strs.get(0));
			assertEquals("Two", strs.get(1));

			List<Double> vs = getDatastore().query(NAMED_TARGET).aggregate(STR).sort(STR.asc()).list(DBL.sum());
			assertEquals(2, vs.size());
			assertEquals(Double.valueOf(8.6), vs.get(0));
			assertNull(vs.get(1));

		});
	}

	@Test
	public void testAggregationProjection() {

		Property<Long> MAX_KEY = KEY.max();

		List<PropertyBox> pbs = getDatastore().query().target(NAMED_TARGET).sort(STR.asc()).aggregate(STR, DBL)
				.list(STR, MAX_KEY);
		assertEquals(2, pbs.size());
		assertEquals(Long.valueOf(1), pbs.get(0).getValue(MAX_KEY));
		assertEquals(Long.valueOf(2), pbs.get(1).getValue(MAX_KEY));

	}

}
