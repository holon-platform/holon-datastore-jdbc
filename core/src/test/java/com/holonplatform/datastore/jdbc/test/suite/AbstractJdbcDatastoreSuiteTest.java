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
package com.holonplatform.datastore.jdbc.test.suite;

import java.util.function.Supplier;

import org.junit.Assert;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;
import com.holonplatform.datastore.jdbc.test.config.DatabasePlatformCommodity;
import com.holonplatform.jdbc.DatabasePlatform;

public abstract class AbstractJdbcDatastoreSuiteTest {

	protected final static Logger LOGGER = JdbcDatastoreLogger.create();

	protected Datastore getDatastore() {
		return AbstractJdbcDatastoreTestSuite.datastore;
	}

	protected void inTransaction(Runnable operation) {
		getDatastore().requireTransactional().withTransaction(tx -> {
			tx.setRollbackOnly();
			operation.run();
		});
	}

	protected <T> T inTransaction(Supplier<T> operation) {
		return getDatastore().requireTransactional().withTransaction(tx -> {
			tx.setRollbackOnly();
			return operation.get();
		});
	}

	protected DatabasePlatform getDatabasePlatform() {
		return getDatastore().create(DatabasePlatformCommodity.class).getDatabase()
				.orElseThrow(() -> new IllegalStateException("Database platform not available"));
	}

	protected void expectedException(Class<? extends Throwable> exceptionClass, Runnable operation) {
		try {
			operation.run();
			Assert.fail("Expected exception was not thrown");
		} catch (Exception e) {
			Assert.assertNotNull(e);
			if (!exceptionClass.isAssignableFrom(e.getClass())) {
				e.printStackTrace();
				Assert.fail("Expected exception: " + exceptionClass.getName() + " but was thrown: "
						+ e.getClass().getName());
			}
		}
	}

}
