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
package com.holonplatform.datastore.jdbc.expressions;

import java.util.Optional;
import java.util.function.Function;

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultSQLParameterDefinition;

/**
 * SQL statement parameter definition.
 *
 * @since 5.1.0
 */
public interface SQLParameterDefinition {

	/**
	 * Get the value type
	 * @return the value type, {@link Void} if value is null
	 */
	Class<?> getType();

	/**
	 * Get the value {@link TemporalType}, if available and applicable
	 * @return Optional value temporal type
	 */
	Optional<TemporalType> getTemporalType();

	/**
	 * Get the parameter value
	 * @return the parameter value
	 */
	Object getValue();

	/**
	 * Get the function to apply when the parameter is serialized in the SQL statement.
	 * @return Optional function to apply when the parameter is serialized in the SQL statement
	 */
	Optional<Function<String, String>> getParameterSerializer();

	/**
	 * Create a new {@link SQLParameterDefinition} using given parameter value.
	 * @param value Parameter value
	 * @return A new {@link SQLParameterDefinition}
	 */
	static SQLParameterDefinition create(Object value) {
		return new DefaultSQLParameterDefinition(value, null, null);
	}

	/**
	 * Create a new {@link SQLParameterDefinition} using given parameter value and providing temporal type.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param temporalType Parameter value temporal type
	 * @return A new {@link SQLParameterDefinition}
	 */
	static SQLParameterDefinition create(Object value, TemporalType temporalType) {
		return new DefaultSQLParameterDefinition(value, temporalType, null);
	}

	/**
	 * Create a new {@link SQLParameterDefinition}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param parameterSerializer The function to apply when the parameter is serialized in the SQL statement
	 * @return A new {@link SQLParameterDefinition}
	 */
	static SQLParameterDefinition create(Object value, Function<String, String> parameterSerializer) {
		return new DefaultSQLParameterDefinition(value, null, parameterSerializer);
	}

	/**
	 * Create a new {@link SQLParameterDefinition}.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param temporalType Parameter value temporal type
	 * @param parameterSerializer The function to apply when the parameter is serialized in the SQL statement
	 * @return A new {@link SQLParameterDefinition}
	 */
	static SQLParameterDefinition create(Object value, TemporalType temporalType,
			Function<String, String> parameterSerializer) {
		return new DefaultSQLParameterDefinition(value, temporalType, parameterSerializer);
	}

	/**
	 * Create a new {@link SQLParameterDefinition} which represents a <code>null</code> parameter value.
	 * @return A new {@link SQLParameterDefinition}
	 */
	static SQLParameterDefinition ofNull() {
		return new DefaultSQLParameterDefinition(null, null, null);
	}

}
