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
package com.holonplatform.datastore.jdbc.composer.expression;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLParameter;

/**
 * SQL statement parameter definition.
 * 
 * @param <T> Parameter type
 *
 * @since 5.1.0
 */
public interface SQLParameter<T> extends TypedExpression<T> {

	/**
	 * Get the parameter expression.
	 * @return the the parameter expression
	 */
	TypedExpression<T> getExpression();

	/**
	 * Create a new {@link SQLParameter} using given expression.
	 * @param <T> Parameter expression type
	 * @param expression Parameter expresion (not null)
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(TypedExpression<T> expression) {
		return new DefaultSQLParameter<>(expression);
	}

	/**
	 * Create a new {@link SQLParameter} using given constant value.
	 * @param value Parameter value (not null)
	 * @return A new {@link SQLParameter}
	 */
	static <T> SQLParameter<T> create(T value) {
		ObjectUtils.argumentNotNull(value, "Parameter value must be not null");
		return new DefaultSQLParameter<>(ConstantExpression.create(value));
	}

}
