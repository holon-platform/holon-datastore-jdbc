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
package com.holonplatform.datastore.jdbc.internal.support;

import java.io.Serializable;
import java.util.Optional;

import com.holonplatform.core.temporal.TemporalType;

/**
 * SQL statement parameter representation.
 * 
 * @since 5.0.0
 */
public interface ParameterValue extends Serializable {

	/**
	 * Get the value type
	 * @return the value type
	 */
	Class<?> getType();

	/**
	 * Get the {@link TemporalType}, if available and applicable
	 * @return Optional value temporal type
	 */
	Optional<TemporalType> getTemporalType();

	/**
	 * Get the parameter value
	 * @return the parameter value
	 */
	Object getValue();

	/**
	 * Create a new {@link ParameterValue}.
	 * @param type Value type
	 * @param value Parameter value
	 * @return A new {@link ParameterValue} instance
	 */
	static ParameterValue create(Class<?> type, Object value) {
		return new DefaultParameterValue(type, value);
	}

	/**
	 * Create a new {@link ParameterValue}.
	 * @param type Value type
	 * @param value Parameter value
	 * @param temporalType Value temporal type
	 * @return A new {@link ParameterValue} instance
	 */
	static ParameterValue create(Class<?> type, Object value, TemporalType temporalType) {
		DefaultParameterValue pv = new DefaultParameterValue(type, value);
		pv.setTemporalType(temporalType);
		return pv;
	}

}
