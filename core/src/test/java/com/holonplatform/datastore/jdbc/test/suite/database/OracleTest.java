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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.holonplatform.core.query.lock.LockQuery;
import com.holonplatform.jdbc.DatabasePlatform;

public class OracleTest extends AbstractDatabaseSuiteTest {

	@Override
	protected DatabasePlatform forDatabasePlatform() {
		return DatabasePlatform.ORACLE;
	}

	@Test
	public void testLockWait() {
		test(datastore -> {
			inTransaction(() -> {
				boolean succeded = datastore.create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L))
						.tryLock(2500);
				assertTrue(succeded);

				succeded = datastore.create(LockQuery.class).target(NAMED_TARGET).filter(KEY.eq(1L)).tryLock(700);
				assertTrue(succeded);
			});
		});

	}

}
