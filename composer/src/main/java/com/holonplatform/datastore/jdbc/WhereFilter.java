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
package com.holonplatform.datastore.jdbc;

import java.util.List;

import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.internal.DefaultWhereFilter;

/**
 * A {@link QueryFilter} which uses a SQL where predicate to express query filter conditions.
 * 
 * <p>
 * This filter supports query parameters, which must be expressed in SQL statement using the default <code>?</code>
 * placeholder. The parameter placeholders will be replaced with the values obtained from the {@link #getParameters()}
 * method.
 * </p>
 * 
 * @since 5.0.0
 */
public interface WhereFilter extends QueryFilter {

	/**
	 * Get the where predicate as sql.
	 * <p>
	 * NOTE: The <code>WHERE</code> clause string should not be included.
	 * </p>
	 * @return Predicate sql
	 */
	String getSQL();

	/**
	 * Get the optional sql parameter values.
	 * @return the sql parameter values, an empty list if none
	 */
	List<Object> getParameters();

	/**
	 * Create a {@link WhereFilter} using given <code>sql</code> predicate.
	 * @param sql Filter sql predicate (not null)
	 * @param parameters Optional parameters. The parameter values will be used to replace the SQL statement
	 *        <code>?</code> placeholders, in the order they are given.
	 * @return A new {@link WhereFilter}
	 */
	static WhereFilter create(String sql, Object... parameters) {
		DefaultWhereFilter filter = new DefaultWhereFilter(sql);
		if (parameters != null) {
			for (Object parameter : parameters) {
				filter.addParameter(parameter);
			}
		}
		return filter;
	}

}
