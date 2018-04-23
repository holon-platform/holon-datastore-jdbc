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
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterPlaceholder;

/**
 * Default {@link SQLParameterPlaceholder} implementation.
 * 
 * @param <T> Parameter value type
 *
 * @since 5.1.0
 */
public class DefaultSQLParameterPlaceholder<T> implements SQLParameterPlaceholder<T> {

	private final Class<? extends T> type;

	/**
	 * Constructor
	 * @param type Parameter value type (not null)
	 */
	public DefaultSQLParameterPlaceholder(Class<? extends T> type) {
		super();
		ObjectUtils.argumentNotNull(type, "Type must be not null");
		this.type = type;
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLParameterPlaceholder [type=" + type + "]";
	}

}
