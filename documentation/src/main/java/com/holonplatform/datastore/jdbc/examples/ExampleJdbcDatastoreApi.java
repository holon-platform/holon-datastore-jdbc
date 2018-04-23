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

import com.holonplatform.datastore.jdbc.JdbcDatastore;

@SuppressWarnings("unused")
public class ExampleJdbcDatastoreApi {

	public void connection() {
		// tag::connection[]
		JdbcDatastore datastore = getJdbcDatastore();

		datastore.withConnection(connection -> { // <1>
			// do something using the provided JDBC connection
			connection.createStatement();
		});

		String result = datastore.withConnection(connection -> { // <2>
			// do something using the provided JDBC connection and return a result
			return null;
		});
		// end::connection[]
	}

	private static JdbcDatastore getJdbcDatastore() {
		return null;
	}

}
