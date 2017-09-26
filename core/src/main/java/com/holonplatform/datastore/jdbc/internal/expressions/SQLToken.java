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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.io.Serializable;

import com.holonplatform.core.Expression;

/**
 * Represents a SQL statement element.
 * 
 * @since 5.0.0
 */
public interface SQLToken extends Expression, Serializable {

	/**
	 * Get the SQL token {@link String} representation.
	 * @return SQL token value
	 */
	String getValue();

	/**
	 * Create a new {@link SQLToken} with given value.
	 * @param value SQL token value
	 * @return A new {@link SQLToken} with given value
	 */
	static SQLToken create(String value) {
		return new DefaultSQLToken(value);
	}

}
