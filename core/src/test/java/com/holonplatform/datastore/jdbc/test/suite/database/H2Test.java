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
package com.holonplatform.datastore.jdbc.test.suite.database;

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DBL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunction;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunctionExpression;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunctionResolver;
import com.holonplatform.jdbc.DatabasePlatform;

public class H2Test extends AbstractDatabaseSuiteTest {

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);
	private final static DataTarget<String> TEST2 = DataTarget.named("test2");

	@Override
	protected DatabasePlatform getDatabasePlatform() {
		return DatabasePlatform.H2;
	}

	@Test
	public void testAutoIncrement() {
		test(datastore -> {
			inTransaction(() -> {

				PropertyBox box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 1").build();
				OperationResult result = datastore.insert(TEST2, box);

				assertNotNull(result);
				assertEquals(1, result.getAffectedCount());
				assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

				assertEquals(1, result.getInsertedKeys().size());

				assertEquals(Long.valueOf(1), result.getInsertedKeys().values().iterator().next());
				assertEquals("CODE", result.getInsertedKeys().keySet().iterator().next().getName());

				// bring back ids

				box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 2").build();
				result = datastore.insert(TEST2, box, DefaultWriteOption.BRING_BACK_GENERATED_IDS);

				assertNotNull(result);
				assertEquals(1, result.getAffectedCount());
				assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

				assertEquals(1, result.getInsertedKeys().size());
				assertEquals(Long.valueOf(2), box.getValue(CODE));

				box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 3").build();
				result = datastore.save(TEST2, box, DefaultWriteOption.BRING_BACK_GENERATED_IDS);

				assertNotNull(result);
				assertEquals(1, result.getAffectedCount());
				assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

				assertEquals(1, result.getInsertedKeys().size());
				assertEquals(Long.valueOf(3), box.getValue(CODE));

			});
		});
	}

	@Test
	public void testCustomFunction() {
		test(datastore -> {
			String result = datastore.query().withExpressionResolver(new IfNullFunctionResolver()).target(NAMED_TARGET)
					.filter(KEY.eq(1L)).findOne(new IfNullFunction<>(STR, "(fallback)")).orElse(null);
			assertNotNull(result);
			assertEquals("One", result);
		});
	}

	@Test
	public void testCustomFunctionExpression() {
		test(datastore -> {
			Long result = datastore.query().withExpressionResolver(IfNullFunctionExpression.RESOLVER)
					.target(NAMED_TARGET).filter(new IfNullFunctionExpression<>(DBL, 12.3d).gt(12d)).findOne(KEY)
					.orElse(null);
			assertNotNull(result);
			assertEquals(Long.valueOf(2), result);

			final IfNullFunctionExpression<Double> TEST_EXPR = new IfNullFunctionExpression<>(DBL, 12.3d);

			Double dbl = datastore.query().withExpressionResolver(IfNullFunctionExpression.RESOLVER)
					.target(NAMED_TARGET).filter(TEST_EXPR.gt(12d)).findOne(TEST_EXPR).orElse(null);
			assertNotNull(dbl);
			assertEquals(Double.valueOf(12.3d), dbl);
		});
	}

}
