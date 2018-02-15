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

import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;

/**
 * Default {@link SQLExpression} representation.
 *
 * @since 5.0.0
 */
public class DefaultSQLExpression implements SQLExpression {

	/**
	 * SQL value
	 */
	private final String value;

	/**
	 * Constructor.
	 * @param value SQL value
	 */
	public DefaultSQLExpression(String value) {
		super();
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.SQLToken#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getValue() == null) {
			throw new InvalidExpressionException("Null SQL token value");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLExpression [value=" + value + "]";
	}

}
