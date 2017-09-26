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

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public abstract class AbstractDatastoreIntegrationTest extends AbstractJdbcDatastoreTest {

	protected static void initSQL(DataSource dataSource, String... scripts) {
		try {
			ResourceLoader rl = new DefaultResourceLoader();
			ResourceDatabasePopulator rbp = new ResourceDatabasePopulator();
			for (String script : scripts) {
				rbp.addScripts(rl.getResource(script));
			}
			try (Connection connection = dataSource.getConnection()) {
				rbp.populate(connection);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to init SQL scripts", e);
		}
	}

}
