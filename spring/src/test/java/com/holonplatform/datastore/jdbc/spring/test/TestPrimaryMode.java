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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.jdbc.spring.EnableDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPrimaryMode.Config.class)
public class TestPrimaryMode {

	@Configuration
	@PropertySource("test4.properties")
	@EnableTransactionManagement
	protected static class Config {

		@Configuration
		@EnableDataSource(dataContextId = "one")
		@EnableJdbcDatastore(dataContextId = "one")
		static class Config1 {
		}

		@Configuration
		@EnableDataSource(dataContextId = "two")
		@EnableJdbcDatastore(dataContextId = "two")
		static class Config2 {
		}

	}

	@Autowired
	private Datastore datastore;

	@Test
	public void testPrimary() {
		assertTrue(datastore.getDataContextId().isPresent());
		assertEquals("two", datastore.getDataContextId().get());
	}

}
