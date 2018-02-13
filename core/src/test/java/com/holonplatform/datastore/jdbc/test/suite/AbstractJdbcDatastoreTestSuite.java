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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.test.suite.database.DerbyTest;
import com.holonplatform.datastore.jdbc.test.suite.database.H2Test;
import com.holonplatform.datastore.jdbc.test.suite.database.HSQLTest;
import com.holonplatform.datastore.jdbc.test.suite.database.MariaDBTest;
import com.holonplatform.datastore.jdbc.test.suite.database.MySQLTest;
import com.holonplatform.datastore.jdbc.test.suite.database.PostgreSQLTest;
import com.holonplatform.datastore.jdbc.test.suite.database.SQLServerTest;
import com.holonplatform.datastore.jdbc.test.suite.database.SQLiteTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ QueryProjectionTest.class, QueryFilterTest.class, QuerySortTest.class, QueryAggregationTest.class,
		QueryRestrictionsTest.class, QueryJoinsTest.class, SubQueryTest.class, RefreshTest.class, InsertTest.class,
		UpdateTest.class, SaveTest.class, DeleteTest.class, ClobTest.class, BlobTest.class, BulkInsertTest.class,
		BulkUpdateTest.class, BulkDeleteTest.class, BulkUpdateAliasTest.class, BulkDeleteAliasTest.class,
		AggregationFunctionsTest.class, StringFunctionsTest.class, TemporalFunctionsTest.class,
		DataTargetResolverTest.class, CustomExpressionsTest.class, H2Test.class, HSQLTest.class, DerbyTest.class,
		MySQLTest.class, MariaDBTest.class, PostgreSQLTest.class, SQLServerTest.class, SQLiteTest.class })
public abstract class AbstractJdbcDatastoreTestSuite {

	public static Datastore datastore;
	
	public static boolean rightJoinTest = true;
	
	public static boolean updateAliasTest = true;

}
