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

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQuery;

/**
 * Default {@link SQLQuery} implementation.
 * 
 * @param <T> Query result type
 *
 * @since 5.1.0
 */
public class DefaultSQLQuery<T> extends DefaultSQLStatement implements SQLQuery<T> {

	private final Class<? extends T> resultType;
	private final SQLResultConverter<T> resultConverter;

	/**
	 * Constructor
	 * @param sql SQL statement (not null)
	 * @param resultType Query result type (not null)
	 * @param resultConverter Query result converter
	 */
	public DefaultSQLQuery(String sql, Class<? extends T> resultType, SQLResultConverter<T> resultConverter) {
		this(sql, resultType, resultConverter, new SQLParameter[0]);
	}

	/**
	 * Constructor
	 * @param sql SQL statement (not null)
	 * @param resultType Query result type (not null)
	 * @param resultConverter Query result converter
	 * @param parameters SQL statement parameters
	 */
	public DefaultSQLQuery(String sql, Class<? extends T> resultType, SQLResultConverter<T> resultConverter,
			SQLParameter[] parameters) {
		super(sql, parameters);
		ObjectUtils.argumentNotNull(resultType, "Result type must be not null");
		this.resultType = resultType;
		this.resultConverter = resultConverter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends T> getType() {
		return resultType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQuery#getResultConverter()
	 */
	@Override
	public SQLResultConverter<T> getResultConverter() {
		return resultConverter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		super.validate();
		if (getResultConverter() == null) {
			throw new InvalidExpressionException("Null SQL result converter");
		}
	}

}
