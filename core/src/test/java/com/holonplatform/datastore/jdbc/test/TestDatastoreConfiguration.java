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

import org.junit.Before;
import org.junit.Test;

import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.dialect.H2Dialect;
import com.holonplatform.datastore.jdbc.test.config.TestCommodity;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DatabasePlatform;

public class TestDatastoreConfiguration {

	private JdbcDatastore datastore;

	@Before
	public void initDatastore() {
		DataSource dataSource = DataSourceBuilder.builder().url("jdbc:h2:mem:cfgdb").username("sa").build();
		datastore = JdbcDatastore.builder().dataSource(dataSource).build();
	}

	@Test
	public void testConfig() {

		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDatabase().isPresent());
		assertEquals(DatabasePlatform.H2, ((JdbcDatastoreCommodityContext) datastore).getDatabase().get());
		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDialect() instanceof H2Dialect);

		TestCommodity tc = datastore.create(TestCommodity.class);
		assertNotNull(tc);

		tc.test();
	}

}
