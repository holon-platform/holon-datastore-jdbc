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
package com.holonplatform.datastore.jdbc.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.jdbc.spring.EnableDataSource;

@SuppressWarnings("unused")
public class ExampleJdbcDatastoreSpring {

	// tag::spring[]
	@EnableJdbcDatastore
	@EnableDataSource
	@PropertySource("datasource.properties")
	@Configuration
	class Config {

	}

	class MyBean {

		@Autowired
		private Datastore datastore;

	}
	// end::spring[]

}
