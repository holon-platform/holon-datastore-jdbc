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

import java.util.Arrays;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;

/**
 * Default {@link SQLStatement} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLStatement implements SQLStatement {

	private final String sql;

	private final SQLParameter<?>[] parameters;

	/**
	 * Constructor
	 * @param sql SQL statement (not null)
	 * @param parameters Optional SQL statement parameters
	 */
	public DefaultSQLStatement(String sql, SQLParameter<?>[] parameters) {
		super();
		ObjectUtils.argumentNotNull(sql, "SQL statement must be not null");
		this.sql = sql;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLStatement#getSql()
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLStatement#getParameters()
	 */
	@Override
	public SQLParameter<?>[] getParameters() {
		return (parameters != null) ? parameters : new SQLParameter[0];
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSql() == null) {
			throw new InvalidExpressionException("Null SQL statement");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLStatement [sql=" + sql + ", parameters=" + Arrays.toString(parameters) + "]";
	}

}
