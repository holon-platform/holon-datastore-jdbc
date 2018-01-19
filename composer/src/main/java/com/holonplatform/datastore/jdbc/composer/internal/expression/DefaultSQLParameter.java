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
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;

/**
 * Default {@link SQLParameter} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLParameter implements SQLParameter {

	/**
	 * Parameter value
	 */
	private final Object value;

	/**
	 * Value type
	 */
	private final Class<?> type;

	/**
	 * Optional temporal type
	 */
	private final TemporalType temporalType;

	public DefaultSQLParameter(Object value, Class<?> type, TemporalType temporalType) {
		super();
		this.value = value;
		this.type = (type != null) ? type : ((value != null) ? value.getClass() : Void.class);
		this.temporalType = temporalType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition#getType()
	 */
	@Override
	public Class<?> getType() {
		return type;
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
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
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
