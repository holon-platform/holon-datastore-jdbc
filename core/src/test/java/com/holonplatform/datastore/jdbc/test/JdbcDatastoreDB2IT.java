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

import javax.sql.DataSource;

import org.junit.BeforeClass;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.expression.KeyIsFilter;
import com.holonplatform.jdbc.DataSourceBuilder;

public class JdbcDatastoreDB2IT extends AbstractJdbcDatastoreIT {

	private static Datastore datastore;

	@BeforeClass
	public static void initDatastore() {

		final DataSource dataSource = DataSourceBuilder.build("db2/datasource.properties");
		// initSQL(dataSource, "db2/schema.sql", "db2/data.sql");

		datastore = JdbcDatastore.builder().dataSource(dataSource).withExpressionResolver(KeyIsFilter.RESOLVER)
				.traceEnabled(true).build();

	}

	@Override
	protected Datastore getDatastore() {
		return datastore;
	}

}
