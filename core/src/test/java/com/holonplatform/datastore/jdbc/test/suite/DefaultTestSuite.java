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

public final class DefaultTestSuite {

	public static final Class<?>[] DEFAULT_SUITE = { QueryProjectionTest.class, QueryFilterTest.class,
			QuerySortTest.class, QueryAggregationTest.class, QueryRestrictionsTest.class, QueryJoinsTest.class,
			SubQueryTest.class, RefreshTest.class, InsertTest.class, UpdateTest.class, SaveTest.class, DeleteTest.class,
			ClobTest.class, BlobTest.class, BulkInsertTest.class, BulkUpdateTest.class, BulkDeleteTest.class,
			BulkUpdateAliasTest.class, BulkDeleteAliasTest.class, AggregationFunctionsTest.class,
			StringFunctionsTest.class, TemporalFunctionsTest.class, DataTargetResolverTest.class };

}
