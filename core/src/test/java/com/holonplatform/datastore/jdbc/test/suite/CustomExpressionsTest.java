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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.DAT;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.ENM;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;
import com.holonplatform.datastore.jdbc.test.data.TestEnum;
import com.holonplatform.datastore.jdbc.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jdbc.test.expression.StrKeySort;

public class CustomExpressionsTest extends AbstractJdbcDatastoreSuiteTest {

	@Test
	public void testQueryFilter() {
		inTransaction(() -> {

			long count = getDatastore().query().target(NAMED_TARGET).filter(new KeyIsFilter(1)).count();
			assertEquals(1, count);

			Optional<String> str = getDatastore().query().target(NAMED_TARGET).filter(new KeyIsFilter(1)).findOne(STR1);
			assertEquals("One", str.get());

			OperationResult result = getDatastore().bulkUpdate(NAMED_TARGET).set(ENM, TestEnum.THIRD)
					.filter(new KeyIsFilter(1)).execute();
			assertEquals(1, result.getAffectedCount());

			result = getDatastore().bulkUpdate(NAMED_TARGET).set(ENM, TestEnum.FIRST).filter(new KeyIsFilter(1))
					.execute();
			assertEquals(1, result.getAffectedCount());

			Optional<PropertyBox> pb = getDatastore().query().target(NAMED_TARGET).filter(new KeyIsFilter(2))
					.findOne(PROPERTIES);
			assertEquals(TestEnum.SECOND, pb.get().getValue(ENM));

			result = getDatastore().bulkUpdate(NAMED_TARGET).filter(new KeyIsFilter(2)).setNull(DAT).execute();
			assertEquals(1, result.getAffectedCount());

			pb = getDatastore().query().target(NAMED_TARGET).filter(new KeyIsFilter(1)).findOne(PROPERTIES);
			assertEquals("One", pb.get().getValue(STR1));

		});
	}

	@SuppressWarnings("serial")
	@Test
	public void testQueryFilterExpression() {
		final ExpressionResolver<KeyIsFilter, SQLExpression> SQL_RESOLVER = ExpressionResolver.create(KeyIsFilter.class,
				SQLExpression.class, (kis, ctx) -> Optional.of(SQLExpression.create("keycode > " + kis.getValue())));

		Optional<String> str = getDatastore().query().withExpressionResolver(SQL_RESOLVER).target(NAMED_TARGET)
				.filter(new KeyIsFilter(1)).findOne(STR1);
		assertEquals("Two", str.get());

		final ExpressionResolver<KeyIsFilter, SQLExpression> SQL_RESOLVER_ALIAS = new SQLExpressionResolver<KeyIsFilter>() {

			@Override
			public Optional<SQLExpression> resolve(KeyIsFilter expression, SQLCompositionContext context)
					throws InvalidExpressionException {
				String path = context.isStatementCompositionContext().flatMap(ctx -> ctx.getAliasOrRoot(KEY))
						.map(alias -> alias + ".keycode").orElse("keycode");
				return Optional.of(SQLExpression.create(path + " > " + expression.getValue()));
			}

			@Override
			public Class<? extends KeyIsFilter> getExpressionType() {
				return KeyIsFilter.class;
			}
		};

		str = getDatastore().query().withExpressionResolver(SQL_RESOLVER_ALIAS).target(NAMED_TARGET)
				.filter(new KeyIsFilter(1)).findOne(STR1);
		assertEquals("Two", str.get());
	}

	@Test
	public void testQuerySort() {
		List<Long> res = getDatastore().query().withExpressionResolver(StrKeySort.RESOLVER).target(NAMED_TARGET)
				.sort(new StrKeySort()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));
	}

	@Test
	public void testQuerySortExpression() {
		List<Long> res = getDatastore().query().withExpressionResolver(StrKeySort.SQL_RESOLVER).target(NAMED_TARGET)
				.sort(new StrKeySort()).list(KEY);
		assertEquals(2, res.size());
		assertEquals(Long.valueOf(2), res.get(0));
	}

}
