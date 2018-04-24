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
package com.holonplatform.datastore.jdbc.composer;

import com.holonplatform.core.query.QuerySort;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultOrderBySort;

/**
 * A {@link QuerySort} which uses SQL order by directives to express sorts.
 * 
 * @since 5.0.0
 */
public interface OrderBySort extends QuerySort {

	/**
	 * Get the order by directives as sql.
	 * <p>
	 * NOTE: The <code>ORDER BY</code> clause string should not be included.
	 * </p>
	 * @return Ordering sql
	 */
	String getSQL();

	/**
	 * Create a {@link OrderBySort} using given <code>sql</code> sort directives. For example
	 * <code>col1 asc, col2 desc</code>.
	 * @param sql Sql sort directives (not null)
	 * @return A new {@link OrderBySort}
	 */
	static OrderBySort create(String sql) {
		return new DefaultOrderBySort(sql);
	}

}
