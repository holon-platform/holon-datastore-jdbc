/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.test.suite.database;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.test.config.DatabasePlatformCommodity;
import com.holonplatform.datastore.jdbc.test.suite.AbstractJdbcDatastoreSuiteTest;
import com.holonplatform.jdbc.DatabasePlatform;

public abstract class AbstractDatabaseSuiteTest extends AbstractJdbcDatastoreSuiteTest {

	protected abstract DatabasePlatform getDatabasePlatform();
	
	protected boolean isDatabase(DatabasePlatform platform) {
		ObjectUtils.argumentNotNull(platform, "DatabasePlatform must be not null");
		return getDatastore().create(DatabasePlatformCommodity.class).getDatabase()
				.orElseThrow(() -> new IllegalStateException("Database platform not available")) == platform;
	}
	
	protected void test(TestOperation operation) {
		final DatabasePlatform platform = getDatabasePlatform();
		if (isDatabase(platform)) {
			try {
				LOGGER.info("> Execute test operation for database: " + platform);
				operation.execute(getDatastore());
			} catch (Exception e) {
				throw new RuntimeException("Test method exception", e);
			}
		} else {
			LOGGER.info("< Skip test operation for database: " + platform);
		}
	}
	
	@FunctionalInterface
	protected interface TestOperation {

		void execute(Datastore datastore) throws Exception;

	}

}
