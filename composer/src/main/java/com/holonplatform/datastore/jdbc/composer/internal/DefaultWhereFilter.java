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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.util.LinkedList;
import java.util.List;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.WhereFilter;

/**
 * Default {@link WhereFilter} implementation.
 * 
 * @since 5.0.0
 */
public class DefaultWhereFilter implements WhereFilter {

	private static final long serialVersionUID = -6314461958625265966L;

	/**
	 * SQL filter expression
	 */
	private final String sql;

	/**
	 * Parameters
	 */
	private final transient List<Object> parameters = new LinkedList<>();

	/**
	 * Constructor.
	 * @param sql The SQL filter predicate
	 */
	public DefaultWhereFilter(String sql) {
		super();
		this.sql = sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcWhereFilter#getSQL()
	 */
	@Override
	public String getSQL() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcWhereFilter#getParameters()
	 */
	@Override
	public List<Object> getParameters() {
		return parameters;
	}

	/**
	 * Add a parameter value.
	 * @param parameter Parameter value to add (not null)
	 */
	public void addParameter(Object parameter) {
		ObjectUtils.argumentNotNull(parameter, "Parameter must be not null");
		parameters.add(parameter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSQL() == null) {
			throw new InvalidExpressionException("Null sql");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultWhereFilter [sql=" + sql + ", parameters=" + parameters + "]";
	}

}
