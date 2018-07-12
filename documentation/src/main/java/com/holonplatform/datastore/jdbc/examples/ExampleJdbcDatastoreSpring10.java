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
package com.holonplatform.datastore.jdbc.examples;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jdbc.spring.SpringJdbcConnectionHandler;
import com.holonplatform.spring.EnableDatastoreConfiguration;

public class ExampleJdbcDatastoreSpring10 {

	// tag::config[]
	@Configuration
	@EnableDataSource(enableTransactionManager = true)
	@EnableTransactionManagement
	@EnableDatastoreConfiguration // <1>
	class Config {

		@Bean
		public Datastore jdbcDatastore(DataSource dataSource) {
			return JdbcDatastore.builder().dataSource(dataSource)
					.connectionHandler(SpringJdbcConnectionHandler.create()) // <2>
					.build();
		}

	}
	// end::config[]

}
