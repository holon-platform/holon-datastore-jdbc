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
package com.holonplatform.datastore.jdbc.test.expression;

import java.util.Arrays;
import java.util.List;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFunction;

public class IfNullFunction<T> implements QueryFunction<T, T> {

	private final TypedExpression<T> nullableValue;
	private final TypedExpression<T> fallbackValue;

	public IfNullFunction(TypedExpression<T> nullableValue, TypedExpression<T> fallbackValue) {
		super();
		this.nullableValue = nullableValue;
		this.fallbackValue = fallbackValue;
	}

	public IfNullFunction(QueryExpression<T> nullableValue, T fallbackValue) {
		this(nullableValue, ConstantExpression.create(fallbackValue));
	}

	@Override
	public Class<? extends T> getType() {
		return nullableValue.getType();
	}

	@Override
	public void validate() throws InvalidExpressionException {
		if (nullableValue == null) {
			throw new InvalidExpressionException("Missing nullable expression");
		}
		if (fallbackValue == null) {
			throw new InvalidExpressionException("Missing fallback expression");
		}
	}

	@Override
	public List<TypedExpression<? extends T>> getExpressionArguments() {
		return Arrays.asList(nullableValue, fallbackValue);
	}

}
