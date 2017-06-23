/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.datastore.jdbc;

import com.holonplatform.core.query.QuerySort;
import com.holonplatform.datastore.jdbc.internal.OrderBySort;

/**
 * A {@link QuerySort} which uses SQL order by directives to express sorts.
 * 
 * @since 5.0.0
 */
public interface JdbcOrderBySort extends QuerySort {

	/**
	 * Get the order by directives as sql.
	 * @return Ordering sql
	 */
	String getSQL();

	/**
	 * Create a {@link JdbcOrderBySort} using given sql sort directives. For example <code>col1 asc, col2 desc</code>.
	 * @param sql Sql sort directives (not null)
	 * @return New {@link JdbcOrderBySort}
	 */
	static JdbcOrderBySort create(String sql) {
		return new OrderBySort(sql);
	}

}
