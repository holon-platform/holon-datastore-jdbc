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
package com.holonplatform.datastore.jdbc.test;

import static com.holonplatform.datastore.jdbc.test.data.TestProperties.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestProperties.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.data.KeyIs;
import com.holonplatform.datastore.jdbc.test.function.IfNullFunction;
import com.holonplatform.datastore.jdbc.test.function.IfNullFunctionResolver;
import com.holonplatform.jdbc.DatabasePlatform;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestJdbcDatastoreH2.Config.class)
public class TestJdbcDatastoreH2 extends AbstractJdbcDatastoreTest {

	@Configuration
	@EnableTransactionManagement
	protected static class Config {

		@Bean
		public DataSource dataSource() {
			return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("datastore")
					.addScript("h2/schema.sql").addScript("h2/data.sql").build();
		}

		@Bean
		public PlatformTransactionManager transactionManager() {
			return new DataSourceTransactionManager(dataSource());
		}

		@Bean
		public JdbcDatastore datastore() {
			return JdbcDatastore.builder().dataSource(dataSource()).database(DatabasePlatform.H2)
					.withExpressionResolver(KeyIs.RESOLVER).withExpressionResolver(new IfNullFunctionResolver())
					.traceEnabled(true).build();
		}

	}

	private static long ms;

	@BeforeClass
	public static void before() {
		ms = System.currentTimeMillis();
	}

	@AfterClass
	public static void after() {
		System.err.println(System.currentTimeMillis() - ms);
	}

	@Autowired
	private Datastore datastore;

	@Override
	protected Datastore getDatastore() {
		return datastore;
	}

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);

	private final static DataTarget<String> TEST2 = DataTarget.named("test2");

	@Test
	@Transactional
	public void testAutoIncrement() {
		PropertyBox box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 1").build();
		OperationResult result = getDatastore().save(TEST2, box);

		assertNotNull(result);
		assertEquals(1, result.getAffectedCount());
		assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

		assertEquals(1, result.getInsertedKeys().size());

		assertEquals(Long.valueOf(1), result.getInsertedKeys().values().iterator().next());
		assertEquals("CODE", result.getInsertedKeys().keySet().iterator().next().getName());

		// bring back ids

		box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 2").build();
		result = getDatastore().insert(TEST2, box, DefaultWriteOption.BRING_BACK_GENERATED_IDS);

		assertNotNull(result);
		assertEquals(1, result.getAffectedCount());
		assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

		assertEquals(1, result.getInsertedKeys().size());
		assertEquals(Long.valueOf(2), box.getValue(CODE));

		box = PropertyBox.builder(CODE, TEXT).set(TEXT, "Auto increment 3").build();
		result = getDatastore().save(TEST2, box, DefaultWriteOption.BRING_BACK_GENERATED_IDS);

		assertNotNull(result);
		assertEquals(1, result.getAffectedCount());
		assertEquals(OperationType.INSERT, result.getOperationType().orElse(null));

		assertEquals(1, result.getInsertedKeys().size());
		assertEquals(Long.valueOf(3), box.getValue(CODE));
	}

	@Test
	public void testCustomFunction() {
		String result = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L))
				.findOne(new IfNullFunction<>(STR, "(fallback)")).orElse(null);
		assertNotNull(result);
		assertEquals("One", result);
	}

	private static final DataTarget<?> R_TARGET = DataTarget.named("test_recur");
	private static final PathProperty<String> R_NAME = PathProperty.create("name", String.class);
	private static final PathProperty<String> R_PARENT = PathProperty.create("parent", String.class);

	@Test
	public void testRecur() {

		List<String> parents = new ArrayList<>();
		findParents(parents, "test3");

		assertEquals(2, parents.size());

	}

	private void findParents(List<String> parents, String name) {
		if (name != null) {
			RelationalTarget<?> group_alias_1 = RelationalTarget.of(R_TARGET).alias("g1");
			RelationalTarget<?> group_alias_2 = RelationalTarget.of(R_TARGET).alias("g2");

			QueryFilter f1 = group_alias_1.property(R_PARENT).isNotNull();
			QueryFilter f2 = group_alias_2.property(R_NAME).eq(group_alias_1.property(R_PARENT));

			RelationalTarget<?> target = group_alias_1.innerJoin(group_alias_2).on(f1.and(f2)).add();

			List<String> group_parents = datastore.query().target(target)
					.filter(group_alias_1.property(R_NAME).eq(name)).list(group_alias_2.property(R_NAME));

			if (!group_parents.isEmpty()) {
				parents.addAll(group_parents);
				for (String p : group_parents) {
					findParents(parents, p);
				}
			}
		}
	}

}
