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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.query.QuerySort;
import com.holonplatform.core.query.QuerySort.SortDirection;
import com.holonplatform.datastore.jdbc.composer.OrderBySort;

public class QuerySortTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testSorts() {
		List<Long> res = getDatastore().query().target(NAMED_TARGET).sort(STR.desc()).sort(KEY.desc()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(STR.desc()).sort(KEY.asc()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(STR.desc().and(KEY.desc())).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(KEY.asc()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(1), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(QuerySort.asc(KEY)).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(1), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(QuerySort.of(KEY, SortDirection.DESCENDING)).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));

		res = getDatastore().query().target(NAMED_TARGET).sort(QuerySort.of(STR.desc(), KEY.asc())).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));
	}

	@Test
	public void testOrderBySort() {

		List<Long> res = getDatastore().query().target(NAMED_TARGET).sort(OrderBySort.create(KEY.getName() + " asc"))
				.list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(1), res.get(0));

	}

}
