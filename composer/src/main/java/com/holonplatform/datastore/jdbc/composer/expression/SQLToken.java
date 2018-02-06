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

import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLToken;

/**
 * A {@link TypedExpression} which represents a SQL token, i.e. a SQL statement part which has to be serialized as is
 * using {@link #getValue()}.
 *
 * @since 5.1.0
 */
public interface SQLToken extends TypedExpression<String> {

	/**
	 * Get the SQL token.
	 * @return SQL token value
	 */
	String getValue();

	/**
	 * Create a new {@link SQLToken}.
	 * @param value SQL token value
	 * @return A new {@link SQLToken} with given value
	 */
	static SQLToken create(String value) {
		return new DefaultSQLToken(value);
	}

}
