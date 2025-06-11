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
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.holonplatform.core.beans.DataPath;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.beans.BeanDatastore;
import com.holonplatform.core.datastore.beans.BeanDatastore.BeanOperationResult;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunction;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunctionExpression;
import com.holonplatform.datastore.jdbc.test.expression.IfNullFunctionResolver;
import com.holonplatform.datastore.jdbc.test.expression.MyEqualPredicate;
import com.holonplatform.datastore.jdbc.test.expression.MyEqualPredicateResolver;
import com.holonplatform.jdbc.DatabasePlatform;

public class H2Test extends AbstractDatabaseSuiteTest {

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);
	private final static DataTarget<String> TEST2 = DataTarget.named("test2");

	private final static PathProperty<Long> A_CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> A_ZIP = PathProperty.create("zip", String.class);
	private final static PropertySet<?> A_SET = PropertySet.of(A_CODE, A_ZIP);
	private final static DataTarget<?> A_TARGET = DataTarget.named("test_alias");

	@Override
	protected DatabasePlatform forDatabasePlatform() {
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
					.filter(KEY.eq(1L)).findOne(new IfNullFunction<>(STR1, "(fallback)")).orElse(null);
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

	@Test
	public void testBeanAutoIncrement() {
		test(datastore -> {
			inTransaction(() -> {

				final BeanDatastore beanDatastore = BeanDatastore.of(datastore);

				Test2 value = new Test2();
				value.setValue("Auto increment 4");

				BeanOperationResult<Test2> result = beanDatastore.insert(value);
				assertEquals(1, result.getAffectedCount());
				assertTrue(result.getResult().isPresent());

				assertEquals(Long.valueOf(4), result.getResult().get().getKey());
				assertEquals("Auto increment 4", result.getResult().get().getValue());

			});
		});
	}

	@Test
	public void testExpressionContext() {
		test(datastore -> {
			List<Long> res = getDatastore().query().withExpressionResolver(new MyEqualPredicateResolver())
					.target(NAMED_TARGET).filter(new MyEqualPredicate<>(STR1, "Two")).list(KEY);
			assertEquals(1, res.size());
			assertEquals(Long.valueOf(2L), res.get(0));
		});
	}

	/*
	@Test
	public void testLockException() {
		test(datastore -> {
			inTransaction(() -> {
				// try lock
				Long key = datastore.create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L)).lock().findOne(KEY)
						.orElse(null);
				assertNotNull(key);

				// try lock different thread
				ExecutorService es = Executors.newSingleThreadExecutor();

				final Future<Long> locked = es.submit(() -> {
					return inTransaction(() -> {
						return datastore.create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L)).lock()
								.findOne(KEY).orElse(null);
					});
				});

				try {
					locked.get();
				} catch (InterruptedException | ExecutionException fe) {
					assertNotNull(fe.getCause());
					assertTrue(fe.getCause() instanceof LockAcquisitionException);
				}

				es.shutdown();
				try {
					es.awaitTermination(30, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

			});
		});
	}*/

	@Test
	public void testAlias() {
		test(datastore -> {
			PropertyBox value = getDatastore().query(A_TARGET).filter(A_CODE.eq(1L)).findOne(A_SET).orElse(null);
			assertNotNull(value);
			assertEquals("Test1", value.getValue(A_ZIP));
		});
	}

	public static final class Test2 {

		@DataPath("code")
		private Long key;

		@DataPath("text")
		private String value;

		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (key ^ (key >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Test2 other = (Test2) obj;
			if (key != other.key)
				return false;
			return true;
		}

	}

}
