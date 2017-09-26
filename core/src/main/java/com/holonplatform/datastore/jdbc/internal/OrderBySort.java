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
package com.holonplatform.datastore.jdbc.internal;

import com.holonplatform.datastore.jdbc.JdbcOrderBySort;

/**
 * Default {@link JdbcOrderBySort} implementation.
 *
 * @since 5.0.0
 */
public class OrderBySort implements JdbcOrderBySort {

	private static final long serialVersionUID = 3759888037099702997L;

	/**
	 * SQL sort expression
	 */
	private final String sql;

	/**
	 * Constructor
	 * @param sql The ordering declaration sql
	 */
	public OrderBySort(String sql) {
		super();
		this.sql = sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcOrderBySort#getSQL()
	 */
	@Override
	public String getSQL() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSQL() == null) {
			throw new InvalidExpressionException("Null sql");
		}
	}

}
