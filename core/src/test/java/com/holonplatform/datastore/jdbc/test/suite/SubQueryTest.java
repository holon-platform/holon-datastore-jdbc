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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_CODE;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.TEST3_TEXT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.property.PathProperty;

public class SubQueryTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testSubQuery() {

		long count = getDatastore().query().target(NAMED_TARGET)
				.filter(KEY.in(
						SubQuery.create(Long.class).target(TEST3).filter(TEST3_TEXT.eq("TestJoin")).select(TEST3_CODE)))
				.count();
		assertEquals(1, count);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(KEY.nin(SubQuery.create(TEST3_CODE).target(TEST3).filter(TEST3_CODE.isNotNull()))).count();
		assertEquals(1, count);

		final PathProperty<Long> T_KEY = KEY.clone().parent(NAMED_TARGET);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(SubQuery.create().target(TEST3).filter(TEST3_CODE.eq(T_KEY)).exists()).count();
		assertEquals(1, count);

		final PathProperty<Long> D_KEY = NAMED_TARGET.property(KEY);

		count = getDatastore().query().target(NAMED_TARGET)
				.filter(SubQuery.create().target(TEST3).filter(TEST3_CODE.eq(D_KEY)).notExists()).count();
		assertEquals(1, count);
	}

	@Test
	public void testSubQueryExplicitAlias() {

		final RelationalTarget<String> AT = RelationalTarget.of(NAMED_TARGET).alias("parent");
		final PathProperty<Long> A_KEY = AT.property(KEY);

		long count = getDatastore().query().target(AT)
				.filter(SubQuery.create().target(TEST3).filter(TEST3_CODE.eq(A_KEY)).notExists()).count();
		assertEquals(1, count);

		final RelationalTarget<String> AT2 = RelationalTarget.of(TEST3).alias("sub");
		final PathProperty<Long> A2_KEY = AT2.property(TEST3_CODE);

		count = getDatastore().query().target(AT)
				.filter(SubQuery.create().target(AT2).filter(A2_KEY.eq(A_KEY)).notExists()).count();
		assertEquals(1, count);

	}

}
