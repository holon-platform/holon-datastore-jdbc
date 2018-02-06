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
package com.holonplatform.datastore.jdbc.composer.expression;

import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLQuery;

/**
 * SQL query expression.
 *
 * @since 5.1.0
 */
public interface SQLQuery extends SQLStatement {

	/**
	 * Get the SQL result converter to be used with this query.
	 * @return The query {@link SQLResultConverter}
	 */
	SQLResultConverter<?> getResultConverter();

	/**
	 * Create a new {@link SQLQuery}.
	 * @param sql Query SQL statement (not null)
	 * @param resultConverter Query result converter
	 * @param parameters SQL statement parameters
	 * @return A new {@link SQLQuery}
	 */
	static SQLQuery create(String sql, SQLResultConverter<?> resultConverter, SQLParameter<?>[] parameters) {
		return new DefaultSQLQuery(sql, resultConverter, parameters);
	}

	/**
	 * Create a new {@link SQLQuery}.
	 * @param sql Query SQL statement (not null)
	 * @param resultConverter Query result converter
	 * @return A new {@link SQLQuery}
	 */
	static SQLQuery create(String sql, SQLResultConverter<?> resultConverter) {
		return new DefaultSQLQuery(sql, resultConverter);
	}

}
