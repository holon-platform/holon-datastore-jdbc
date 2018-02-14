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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.test.init.KeyIsResolver;
import com.holonplatform.datastore.jdbc.test.config.DatabasePlatformCommodity;
import com.holonplatform.datastore.jdbc.test.suite.AbstractJdbcDatastoreTestSuite;
import com.holonplatform.jdbc.spring.EnableDataSource;

//@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSpringJdbcDatastore.Config.class)
public class TestSpringJdbcDatastore {

	@Configuration
	@ComponentScan(basePackageClasses = KeyIsResolver.class)
	@PropertySource("testops.properties")
	@EnableTransactionManagement
	@EnableDataSource(enableTransactionManager = true, dataContextId = "ops")
	@EnableJdbcDatastore(dataContextId = "ops")
	protected static class Config {

	}

	@Autowired
	private Datastore datastoreBean;

	@Test
	public void testSuite() {
		AbstractJdbcDatastoreTestSuite.datastore = datastoreBean;

		// test commodity detection
		Assert.assertTrue(datastoreBean.hasCommodity(DatabasePlatformCommodity.class));

		JUnitCore.runClasses(SpringJdbcDatastoreTestSuite.class);
	}

	public static class SpringJdbcDatastoreTestSuite extends AbstractJdbcDatastoreTestSuite {

	}

}
