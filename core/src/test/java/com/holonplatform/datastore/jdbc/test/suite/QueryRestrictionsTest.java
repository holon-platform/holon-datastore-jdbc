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

import java.util.List;

import org.junit.Test;

public class QueryRestrictionsTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testRestrictions() {
		List<String> str = getDatastore().query().target(NAMED_TARGET).restrict(1, 0).sort(KEY.asc()).list(STR1);
		assertEquals(1, str.size());
		assertEquals("One", str.get(0));

		str = getDatastore().query().target(NAMED_TARGET).restrict(1, 1).sort(KEY.asc()).list(STR1);
		assertEquals(1, str.size());
		assertEquals("Two", str.get(0));
	}

}
