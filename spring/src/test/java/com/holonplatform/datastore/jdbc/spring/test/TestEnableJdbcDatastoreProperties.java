/*
 * Copyright 2000-2016 Holon TDCN.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.spring.EnableDataSource;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEnableJdbcDatastoreProperties.Config.class)
public class TestEnableJdbcDatastoreProperties {

	@Configuration
	@PropertySource("test3.properties")
	@EnableTransactionManagement
	@EnableDataSource(enableTransactionManager = true)
	@EnableJdbcDatastore
	protected static class Config {

	}

	final static DataTarget<String> NAMED_TARGET = DataTarget.named("test1");

	final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	final static PathProperty<String> STR = PathProperty.create("strv", String.class);

	@Autowired
	private JdbcDatastore datastore;

	@Transactional
	@Test
	public void testDatastore() {

		assertNotNull(datastore);

		assertEquals(DatabasePlatform.H2, ((JdbcDatastoreCommodityContext) datastore).getDatabase().orElse(null));
		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDialect() instanceof TestDialect);

		datastore.save(NAMED_TARGET, PropertyBox.builder(KEY, STR).set(KEY, 7L).set(STR, "Test ds (7)").build());

		Optional<Long> found = datastore.query().target(NAMED_TARGET).filter(KEY.eq(7L)).findOne(KEY);
		assertTrue(found.isPresent());

	}

}
