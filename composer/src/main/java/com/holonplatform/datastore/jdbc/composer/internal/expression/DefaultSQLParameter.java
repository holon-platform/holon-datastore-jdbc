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
package com.holonplatform.datastore.jdbc.composer.internal.expression;

import java.util.Optional;
import java.util.function.Function;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;

/**
 * Default {@link SQLParameter} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLParameter<T> implements SQLParameter<T> {

	private final T value;

	private final Class<? extends T> type;

	private final TemporalType temporalType;

	private final Function<String, String> serializationFunction;

	/**
	 * Constructor.
	 * @param value Parameter value
	 * @param type Parameter value type (not null)
	 * @param temporalType Optional temporal type
	 * @param serializationFunction Parameter serialization function
	 */
	public DefaultSQLParameter(T value, Class<? extends T> type, TemporalType temporalType,
			Function<String, String> serializationFunction) {
		super();
		ObjectUtils.argumentNotNull(type, "Parameter type must be not null");
		this.value = value;
		this.type = type;
		this.temporalType = temporalType;
		this.serializationFunction = (serializationFunction != null) ? serializationFunction : Function.identity();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends T> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLParameter#getValue()
	 */
	@Override
	public T getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLParameter#getSerializationFunction()
	 */
	@Override
	public Function<String, String> getSerializationFunction() {
		return serializationFunction;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getType() == null) {
			throw new InvalidExpressionException("Null parameter type");
		}
		if (getSerializationFunction() == null) {
			throw new InvalidExpressionException("Null parameter serialization function");
		}
	}

}
