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

import java.util.Optional;
import java.util.function.Function;

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;

/**
 * Default {@link SQLParameterDefinition} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLParameterDefinition implements SQLParameterDefinition {

	/**
	 * Parameter value
	 */
	private final Object value;

	/**
	 * Optional temporal type
	 */
	private final TemporalType temporalType;

	/**
	 * Serializer
	 */
	private final Function<String, String> parameterSerializer;

	public DefaultSQLParameterDefinition(Object value, TemporalType temporalType,
			Function<String, String> parameterSerializer) {
		super();
		this.value = value;
		this.temporalType = temporalType;
		this.parameterSerializer = parameterSerializer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition#getType()
	 */
	@Override
	public Class<?> getType() {
		return (value != null) ? value.getClass() : Void.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition#getParameterSerializer()
	 */
	@Override
	public Optional<Function<String, String>> getParameterSerializer() {
		return Optional.ofNullable(parameterSerializer);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLParameterDefinition [value=" + value + ", temporalType=" + temporalType + "]";
	}

}