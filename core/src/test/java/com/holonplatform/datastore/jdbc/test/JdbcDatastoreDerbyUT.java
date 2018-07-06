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

import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.config.DatabasePlatformCommodity;
import com.holonplatform.datastore.jdbc.test.expression.KeyIsFilter;
import com.holonplatform.datastore.jdbc.test.suite.AbstractJdbcDatastoreTestSuite;
import com.holonplatform.jdbc.DataSourceBuilder;

public class JdbcDatastoreDerbyUT extends AbstractJdbcDatastoreTestSuite {

	@BeforeClass
	public static void initDatastore() {

		final DataSource dataSource = DataSourceBuilder.builder().url("jdbc:derby:memory:datastore;create=true")
				.username("sa").withInitScriptResource("derby/schema.sql").withInitScriptResource("derby/data.sql")
				.build();

		datastore = JdbcDatastore.builder().dataSource(dataSource).withCommodity(DatabasePlatformCommodity.FACTORY)
				.withExpressionResolver(KeyIsFilter.RESOLVER).traceEnabled(true).build();

		lockTest = false;
	}

}
