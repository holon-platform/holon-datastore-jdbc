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
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.datastore.jdbc.test.data.TestProjectionBean;

public class DataMappingTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testBeanQuery() {

		final BeanPropertySet<TestProjectionBean> BPS = BeanPropertySet.create(TestProjectionBean.class);

		List<TestProjectionBean> results = getDatastore().query().target(NAMED_TARGET)
				.filter(BPS.property("text").eq("One")).sort(KEY.asc())
				.list(BeanProjection.of(TestProjectionBean.class));

		assertEquals(1, results.size());
		assertEquals(1L, results.get(0).getKeycode());

		results = getDatastore().query().target(NAMED_TARGET).sort(BPS.property("text").desc())
				.list(BeanProjection.of(TestProjectionBean.class));

		assertEquals(2, results.size());
		assertEquals(2L, results.get(0).getKeycode());

	}

}
