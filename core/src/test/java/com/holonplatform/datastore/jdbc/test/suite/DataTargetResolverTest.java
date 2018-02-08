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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DataTarget.DataTargetResolver;
import com.holonplatform.core.datastore.relational.RelationalTarget;

public class DataTargetResolverTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testDataTargetResolver() {

		@SuppressWarnings("rawtypes")
		ExpressionResolver<DataTarget, DataTarget> dr = DataTargetResolver.create(DataTarget.class,
				(t, c) -> "#testres#".equals(t.getName()) ? Optional.of(DataTarget.named("test1")) : Optional.empty());

		long count = getDatastore().query().withExpressionResolver(dr).target(DataTarget.named("#testres#"))
				.filter(STR.eq("One")).count();
		assertEquals(1, count);

	}

	@Test
	public void testRelationalTargetResolver() {

		@SuppressWarnings("rawtypes")
		ExpressionResolver<DataTarget, DataTarget> dr = DataTargetResolver.create(DataTarget.class,
				(t, c) -> "#testres2#".equals(t.getName()) ? Optional.of(RelationalTarget.of(NAMED_TARGET))
						: Optional.empty());

		long count = getDatastore().query().withExpressionResolver(dr).target(DataTarget.named("#testres2#"))
				.filter(STR.eq("One")).count();
		assertEquals(1, count);

	}

}
