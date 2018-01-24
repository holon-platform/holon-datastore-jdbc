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

import com.holonplatform.core.Expression;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLStatement;

/**
 * SQL statement expression, with statement parameters support.
 *
 * @since 5.1.0
 */
public interface SQLStatement extends Expression {

	/**
	 * Get the statement SQL.
	 * @return the statement SQL (not null)
	 */
	String getSql();

	/**
	 * Get the optional statement parameters.
	 * @return the statement parameters, an empty array if none
	 */
	SQLParameter<?>[] getParameters();

	/**
	 * Create a new {@link SQLStatement}.
	 * @param sql SQL statement (not null)
	 * @param parameters Optional statement parameters
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	static SQLStatement create(String sql, SQLParameter... parameters) {
		return new DefaultSQLStatement(sql, parameters);
	}

}
