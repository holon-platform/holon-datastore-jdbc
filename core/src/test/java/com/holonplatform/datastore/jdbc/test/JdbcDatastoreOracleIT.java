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
package com.holonplatform.datastore.jdbc.test;

import javax.sql.DataSource;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.data.KeyIs;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JdbcDatastoreOracleIT.Config.class)
@DirtiesContext
public class JdbcDatastoreOracleIT extends AbstractDatastoreIntegrationTest {

	@Configuration
	@EnableTransactionManagement
	protected static class Config {

		@Bean
		public DataSource dataSource() {
			DataSource ds = DataSourceBuilder.create().build(
					DataSourceConfigProperties.builder().withPropertySource("oracle/datasource.properties").build());
			// init
			initSQL(ds, "oracle/schema.sql", "oracle/data.sql");
			return ds;
		}

		@Bean
		public PlatformTransactionManager transactionManager() {
			return new DataSourceTransactionManager(dataSource());
		}

		@Bean
		public JdbcDatastore datastore() {
			return JdbcDatastore.builder().dataSource(dataSource()).database(DatabasePlatform.ORACLE)
					.withExpressionResolver(KeyIs.RESOLVER)
					// .traceEnabled(true)
					.build();
		}

	}

	@Autowired
	private Datastore datastore;

	@Override
	protected Datastore getDatastore() {
		return datastore;
	}

}
