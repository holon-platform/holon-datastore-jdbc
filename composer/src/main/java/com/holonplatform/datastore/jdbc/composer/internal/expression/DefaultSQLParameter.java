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

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;

/**
 * Default {@link SQLParameter} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLParameter<T> implements SQLParameter<T> {

	/**
	 * Parameter expression
	 */
	private final TypedExpression<T> expression;

	public DefaultSQLParameter(TypedExpression<T> expression) {
		super();
		ObjectUtils.argumentNotNull(expression, "Parameter expression must be not null");
		this.expression = expression;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLParameter#getExpression()
	 */
	@Override
	public TypedExpression<T> getExpression() {
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends T> getType() {
		return expression.getType();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getTemporalType()
	 */
	@Override
	public Optional<TemporalType> getTemporalType() {
		return expression.getTemporalType();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getExpression() == null) {
			throw new InvalidExpressionException("Null parameter expression");
		}
	}

}
