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
package com.holonplatform.datastore.jdbc.composer;

import java.util.Optional;

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLValueSerializer;

/**
 * SQL constant value serializer.
 *
 * @since 5.1.0
 */
public interface SQLValueSerializer {

	/**
	 * Serialize given value as a SQL string.
	 * @param value Value to serialize
	 * @return Serialized SQL value. A <code>null</code> value will be serialized as the <code>NULL</code> string
	 */
	default String serialize(Object value) {
		return serialize(value, null);
	}

	/**
	 * Serialize given value as a SQL string.
	 * @param value Value to serialize
	 * @param temporalType The {@link TemporalType} to use with temporal values
	 * @return Serialized SQL value. A <code>null</code> value will be serialized as the <code>NULL</code> string
	 */
	String serialize(Object value, TemporalType temporalType);

	/**
	 * Try to serialize given temporal value as a SQL string, using given <code>temporalType</code> to select a suitable
	 * serialization format.
	 * @param value Value to serialize
	 * @param temporalType Value temporal type
	 * @return Serialized SQL value, or an empty Optional if the value type is not supported by the serializer
	 */
	Optional<String> serializeTemporal(Object value, TemporalType temporalType);

	/**
	 * Get the default {@link SQLValueSerializer}.
	 * @return the default {@link SQLValueSerializer}
	 */
	static SQLValueSerializer getDefault() {
		return DefaultSQLValueSerializer.INSTANCE;
	}

}
