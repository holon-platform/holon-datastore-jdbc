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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.Optional;

import com.holonplatform.core.temporal.TemporalType;

/**
 * Default {@link LiteralValue} implementation.
 *
 * @since 5.0.0
 */
public class DefaultLiteralValue implements LiteralValue {

	private static final long serialVersionUID = -2035716965294417477L;

	/**
	 * Value
	 */
	private final Object value;

	/**
	 * Value type
	 */
	private final Class<?> type;

	/**
	 * Optional temporal value type
	 */
	private final TemporalType temporalType;

	/**
	 * Constructor
	 * @param value Value
	 * @param type Value type
	 * @param temporalType Optional temporal value type
	 */
	public DefaultLiteralValue(Object value, Class<?> type, TemporalType temporalType) {
		super();
		this.value = value;
		this.type = type;
		this.temporalType = temporalType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ExpressionValue#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ExpressionValue#getType()
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ExpressionValue#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LiteralValue expression [value=" + value + ", type=" + type + "]";
	}

}
