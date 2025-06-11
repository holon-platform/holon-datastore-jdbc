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
package com.holonplatform.datastore.jdbc.spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.test.config2.TestConfigCommodity;
import com.holonplatform.datastore.jdbc.spring.test.config2.TestConfigCommodityFactory;
import com.holonplatform.datastore.jdbc.spring.test.expression.MyFilter;
import com.holonplatform.datastore.jdbc.spring.test.expression.MySort;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.spring.EnableDataSource;

@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJdbcDatastoreConfiguration.Config.class)
public class TestJdbcDatastoreConfiguration {

	@Configuration
	@ComponentScan(basePackageClasses = TestConfigCommodityFactory.class)
	@PropertySource("test5.properties")
	@EnableTransactionManagement
	@EnableDataSource(enableTransactionManager = true)
	@EnableJdbcDatastore
	protected static class Config {

	}

	private final static DataTarget<String> NAMED_TARGET = DataTarget.named("testd");

	private final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	private final static PathProperty<String> STR1 = PathProperty.create("strv", String.class);

	@Autowired
	private Datastore datastore;

	@Test
	public void testCommodity() {
		TestConfigCommodity tc = datastore.create(TestConfigCommodity.class);
		assertNotNull(tc);

		assertEquals(DatabasePlatform.H2, tc.getPlatform());
	}

	@Rollback
	@Transactional
	@Test
	public void testResolvers() {

		datastore.insert(NAMED_TARGET, PropertyBox.builder(KEY, STR1).set(KEY, 100L).set(STR1, "test").build());
		datastore.insert(NAMED_TARGET, PropertyBox.builder(KEY, STR1).set(KEY, 101L).set(STR1, "ztest").build());

		Optional<Long> found = datastore.query().target(NAMED_TARGET).filter(new MyFilter(STR1)).findOne(KEY);
		assertTrue(found.isPresent());
		assertEquals(Long.valueOf(100L), found.get());

		List<Long> values = datastore.query().target(NAMED_TARGET).sort(new MySort(STR1)).list(KEY);
		assertEquals(2, values.size());
		assertEquals(Long.valueOf(101L), values.get(0));

	}

}
