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

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterValue;

/**
 * Default {@link SQLParameterValue} implementation.
 * 
 * @param <T> Parameter value type
 *
 * @since 5.1.0
 */
public class DefaultSQLParameterValue<T> implements SQLParameterValue<T> {

	private final T value;

	private final Class<? extends T> type;
	
	private final TemporalType temporalType;

	private final String sql;
	
	/**
	 * Constructor using default <code>?</code> SQL parameter placeholder.
	 * @param value Parameter value
	 * @param type Parameter type (not null)
	 */
	public DefaultSQLParameterValue(T value, Class<? extends T> type) {
		this(value, type, null, "?");
	}

	/**
	 * Constructor using default <code>?</code> SQL parameter placeholder.
	 * @param value Parameter value
	 * @param type Parameter type (not null)
	 * @param temporalType Optional temporal type
	 */
	public DefaultSQLParameterValue(T value, Class<? extends T> type, TemporalType temporalType) {
		this(value, type, temporalType, "?");
	}
	
	/**
	 * Constructor.
	 * @param value Parameter value
	 * @param type Parameter type (not null)
	 * @param sql SQL representation
	 */
	public DefaultSQLParameterValue(T value, Class<? extends T> type, String sql) {
		this(value, type, null, "?");
	}

	/**
	 * Constructor.
	 * @param value Parameter value
	 * @param type Parameter type (not null)
	 * @param temporalType Optional temporal type
	 * @param sql SQL representation
	 */
	public DefaultSQLParameterValue(T value, Class<? extends T> type, TemporalType temporalType, String sql) {
		super();
		ObjectUtils.argumentNotNull(type, "Parameter value type must be not null");
		ObjectUtils.argumentNotNull(sql, "Parameter SQL representation must be not null");
		this.value = value;
		this.type = type;
		this.temporalType = temporalType;
		this.sql = sql;
	}

	/* (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return Optional.ofNullable(temporalType);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLParameterValue#getSql()
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLParameterValue#getValue()
	 */
	@Override
	public T getValue() {
		return value;
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
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

}
