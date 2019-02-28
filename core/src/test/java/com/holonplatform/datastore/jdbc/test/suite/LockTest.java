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
package com.holonplatform.datastore.jdbc.test.suite;

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.lock.LockQuery;
import com.holonplatform.jdbc.DatabasePlatform;

public class LockTest extends AbstractJdbcDatastoreSuiteTest {

	private static boolean executeLockTest(DatabasePlatform platform) {
		return platform != DatabasePlatform.DERBY && platform != DatabasePlatform.HSQL
				&& platform != DatabasePlatform.SQLITE && platform != DatabasePlatform.H2;
	}

	@Test
	public void testLockMode() {

		assertTrue(getDatastore().hasCommodity(LockQuery.class));

		if (executeLockTest(getDatabasePlatform())) {

			inTransaction(() -> {
				PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(PROPERTIES)
						.orElse(null);
				assertNotNull(value);

				value = getDatastore().create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L)).lock()
						.findOne(PROPERTIES).orElse(null);
				assertNotNull(value);
			});

		}

	}

	@Test
	public void testLockFail() {

		if (executeLockTest(getDatabasePlatform())) {

			assertTrue(getDatastore().hasCommodity(LockQuery.class));

			inTransaction(() -> {
				// try lock
				boolean succeded = getDatastore().create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L))
						.tryLock();
				assertTrue(succeded);

				// try lock same em
				succeded = getDatastore().create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L)).tryLock();
				assertTrue(succeded);

				// try lock different thread
				ExecutorService es = Executors.newSingleThreadExecutor();

				final Future<Boolean> locked = es.submit(() -> {
					return inTransaction(() -> {
						return getDatastore().create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L))
								.tryLock(0);
					});
				});

				try {
					assertFalse(locked.get());
				} catch (InterruptedException | ExecutionException e1) {
					throw new RuntimeException(e1);
				}

				es.shutdown();
				try {
					es.awaitTermination(30, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

			});

		}
	}

}
