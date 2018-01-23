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
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLParameterValue;

/**
 * SQL parameter value representation.
 * 
 * @param <T> Parameter value type
 *
 * @since 5.1.0
 */
public interface SQLParameterValue<T> extends TypedExpression<T> {

	/**
	 * Return the parameter value SQL representation.
	 * @return parameter SQL representation
	 */
	default String getSql() {
		return "?";
	}

	/**
	 * Get the parameter value
	 * @return the parameter value, maybe <code>null</code>
	 */
	T getValue();

	/**
	 * Create a new {@link SQLParameterValue}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @return A new {@link SQLParameterValue}
	 */
	static <T> SQLParameterValue<T> create(T value, Class<? extends T> type) {
		return new DefaultSQLParameterValue<>(value, type);
	}

	/**
	 * Create a new {@link SQLParameterValue}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Value temporal type
	 * @return A new {@link SQLParameterValue}
	 */
	static <T> SQLParameterValue<T> create(T value, Class<? extends T> type, TemporalType temporalType) {
		return new DefaultSQLParameterValue<>(value, type, temporalType);
	}

	/**
	 * Create a new {@link SQLParameterValue}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param sql SQL parameter representation (not null)
	 * @return A new {@link SQLParameterValue}
	 */
	static <T> SQLParameterValue<T> create(T value, Class<? extends T> type, String sql) {
		return new DefaultSQLParameterValue<>(value, type, sql);
	}

	/**
	 * Create a new {@link SQLParameterValue}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Value temporal type
	 * @param sql SQL parameter representation (not null)
	 * @return A new {@link SQLParameterValue}
	 */
	static <T> SQLParameterValue<T> create(T value, Class<? extends T> type, TemporalType temporalType, String sql) {
		return new DefaultSQLParameterValue<>(value, type, temporalType, sql);
	}

}
