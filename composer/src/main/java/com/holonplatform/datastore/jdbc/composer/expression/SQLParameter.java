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

import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLParameter;

/**
 * SQL statement parameter definition.
 *
 * @since 5.1.0
 */
public interface SQLParameter extends Expression {

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
	 * Create a new {@link SQLParameter} using given parameter value.
	 * @param value Parameter value
	 * @param type Value type
	 * @return A new {@link SQLParameter}
	 */
	static SQLParameter create(Object value, Class<?> type) {
		return new DefaultSQLParameter(value, type, null);
	}

	/**
	 * Create a new {@link SQLParameter} using given parameter value and providing temporal type.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param type Value type
	 * @param temporalType Parameter value temporal type
	 * @return A new {@link SQLParameter}
	 */
	static SQLParameter create(Object value, Class<?> type, TemporalType temporalType) {
		return new DefaultSQLParameter(value, type, temporalType);
	}

	/**
	 * Create a new {@link SQLParameter} using given parameter value. The value type will be the <code>value</code>
	 * class, or {@link Void} if value is null.
	 * @param value Parameter value
	 * @return A new {@link SQLParameter}
	 */
	static SQLParameter create(Object value) {
		return new DefaultSQLParameter(value, (value != null) ? value.getClass() : Void.class, null);
	}

	/**
	 * Create a new {@link SQLParameter} using given parameter value and providing temporal type. The value type will be
	 * the <code>value</code> class, or {@link Void} if value is null.
	 * @param <T> Parameter value type
	 * @param value Parameter value
	 * @param temporalType Parameter value temporal type
	 * @return A new {@link SQLParameter}
	 */
	static SQLParameter create(Object value, TemporalType temporalType) {
		return new DefaultSQLParameter(value, (value != null) ? value.getClass() : Void.class, temporalType);
	}

	/**
	 * Create a new {@link SQLParameter} which represents a <code>null</code> parameter value.
	 * @return A new {@link SQLParameter}
	 */
	static SQLParameter ofNull() {
		return new DefaultSQLParameter(null, Void.class, null);
	}

}
