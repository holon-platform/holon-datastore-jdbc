/*
 * Copyright 2016-2018 Axioma srl.
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

import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;

public class IfNullFunctionExpression<T> implements QueryExpression<T>, QueryProjection<T> {

	private final PathProperty<T> path;
	private final T fallbackValue;

	public IfNullFunctionExpression(PathProperty<T> path, T fallbackValue) {
		super();
		this.path = path;
		this.fallbackValue = fallbackValue;
	}

	@Override
	public Class<? extends T> getType() {
		return path.getType();
	}

	@Override
	public void validate() throws InvalidExpressionException {
	}

	public PathProperty<T> getPath() {
		return path;
	}

	public T getFallbackValue() {
		return fallbackValue;
	}

	public static final ExpressionResolver<IfNullFunctionExpression<?>, SQLExpression> RESOLVER = ExpressionResolver
			.create(IfNullFunctionExpression.class, SQLExpression.class, (fnc, ctx) -> Optional.of(
					SQLExpression.create("ifnull(" + fnc.getPath().getName() + ", " + fnc.getFallbackValue() + ")")));

}
