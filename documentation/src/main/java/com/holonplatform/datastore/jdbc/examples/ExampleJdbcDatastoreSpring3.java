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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;

public class ExampleJdbcDatastoreSpring3 {

	static
	// tag::config[]
	@Configuration class Config {

		@Configuration
		@EnableJdbcDatastore(dataContextId = "one") // <1>
		static class Config1 {

			@Bean(name = "dataSource_one")
			public DataSource dataSource() {
				return buildDataSource();
			}

		}

		@Configuration
		@EnableJdbcDatastore(dataContextId = "two") // <2>
		static class Config2 {

			@Bean(name = "dataSource_two")
			public DataSource dataSource() {
				return buildDataSource();
			}

		}

	}

	@Autowired
	@Qualifier("one")
	Datastore datastore1; // <3>

	@Autowired
	@Qualifier("two")
	Datastore datastore2;
	// end::config[]

	private static DataSource buildDataSource() {
		return null;
	}

}
