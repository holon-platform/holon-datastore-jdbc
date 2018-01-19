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
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLLiteral;

/**
 * SQL literal value.
 * 
 * @param <T> Value type
 *
 * @since 5.1.0
 */
public interface SQLLiteral<T> extends TypedExpression<T> {

	/**
	 * Get the parameter value
	 * @return the parameter value
	 */
	T getValue();

	/**
	 * Create a new {@link SQLLiteral} value.
	 * @param <T> Value type
	 * @param value Literal value
	 * @return A new {@link SQLLiteral}
	 */
	static <T> SQLLiteral<T> create(T value) {
		return new DefaultSQLLiteral<>(value);
	}

	/**
	 * Create a new {@link SQLLiteral} value.
	 * @param <T> Value type
	 * @param value Literal value
	 * @param temporalType Value temporal type
	 * @return A new {@link SQLLiteral}
	 */
	static <T> SQLLiteral<T> create(T value, TemporalType temporalType) {
		return new DefaultSQLLiteral<>(value, temporalType);
	}

}
