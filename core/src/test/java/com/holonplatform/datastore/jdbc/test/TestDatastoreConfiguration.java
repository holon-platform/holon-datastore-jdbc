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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.dialect.H2Dialect;
import com.holonplatform.datastore.jdbc.test.config.TestCommodity;
import com.holonplatform.jdbc.DatabasePlatform;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDatastoreConfiguration.Config.class)
public class TestDatastoreConfiguration {

	@Configuration
	protected static class Config {

		@Bean
		public DataSource dataSource() {
			return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("datastoreCfg")
					.addScript("h2/schema.sql").addScript("h2/data.sql").build();
		}

		@Bean
		public JdbcDatastore datastore() {
			return JdbcDatastore.builder().dataSource(dataSource()).build();
		}

	}

	@Autowired
	private Datastore datastore;

	@Test
	public void testPlatform() {
		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDatabase().isPresent());
		assertEquals(DatabasePlatform.H2, ((JdbcDatastoreCommodityContext) datastore).getDatabase().get());
		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDialect() instanceof H2Dialect);
	}

	@Test
	public void testCommodity() {
		TestCommodity tc = datastore.create(TestCommodity.class);
		assertNotNull(tc);

		tc.test();
	}

}
