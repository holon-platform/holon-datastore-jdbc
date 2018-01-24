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

import java.util.function.Function;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLParameter;

/**
 * SQL statement parameter definition.
 * 
 * @param <T> Parameter type
 *
 * @since 5.1.0
 */
public interface SQLParameter<T> extends TypedExpression<T> {

	/**
	 * Get the parameter value
	 * @return the parameter value, maybe <code>null</code>
	 */
	T getValue();

	/**
	 * Optional function which has to be used to serialize the parameter placeholder in the SQL statement.
	 * @return Optional parameter placeholder serialization function
	 */
	default Function<String, String> getSerializationFunction() {
		return Function.identity();
	}

	/**
	 * Create a new {@link SQLParameter}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(T value, Class<? extends T> type) {
		return create(value, type, null, Function.identity());
	}

	/**
	 * Create a new {@link SQLParameter}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Value temporal type
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(T value, Class<? extends T> type, TemporalType temporalType) {
		return create(value, type, temporalType, Function.identity());
	}

	/**
	 * Create a new {@link SQLParameter}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param serializationFunction Parameter serialization function
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(T value, Class<? extends T> type,
			Function<String, String> serializationFunction) {
		return create(value, type, null, serializationFunction);
	}

	/**
	 * Create a new {@link SQLParameter}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Value temporal type
	 * @param serializationFunction Parameter serialization function
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(T value, Class<? extends T> type, TemporalType temporalType,
			Function<String, String> serializationFunction) {
		return new DefaultSQLParameter<>(value, type, temporalType, serializationFunction);
	}

}
