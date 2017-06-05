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
package com.holonplatform.datastore.jdbc.internal.dialect;

import com.holonplatform.datastore.jdbc.JdbcDialect.LimitHandler;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;

/**
 * Default dialect {@link LimitHandler} implementation.
 *
 * @since 5.0.0
 */
public enum DefaultLimitHandler implements LimitHandler {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	@Override
	public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
		return serializedSql + ((offset > -1) ? (" OFFSET  " + offset + " FETCH FIRST " + limit + " ROWS ONLY")
				: (" FETCH FIRST " + limit + " ROWS ONLY"));
	}

}
