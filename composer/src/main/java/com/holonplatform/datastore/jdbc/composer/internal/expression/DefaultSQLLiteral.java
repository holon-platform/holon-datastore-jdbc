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

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLLiteral;

/**
 * Default {@link SQLLiteral} implementation.
 *
 * @param <T> Value type
 *
 * @since 5.1.0
 */
public class DefaultSQLLiteral<T> implements SQLLiteral<T> {

	private final T value;

	private final TemporalType temporalType;

	/**
	 * Constructor with value.
	 * @param value Literal value
	 */
	public DefaultSQLLiteral(T value) {
		this(value, null);
	}

	/**
	 * Constructor with value and {@link TemporalType}.
	 * @param value Literal value
	 * @param temporalType Value temporal type
	 */
	public DefaultSQLLiteral(T value, TemporalType temporalType) {
		super();
		this.value = value;
		this.temporalType = temporalType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLLiteral#getValue()
	 */
	@Override
	public T getValue() {
		return value;
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
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> getType() {
		return (Class<? extends T>) ((value != null) ? value.getClass() : Object.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

}
