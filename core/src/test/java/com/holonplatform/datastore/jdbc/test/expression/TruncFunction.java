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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

public class TruncFunction<T> implements QueryFunction<T, T> {

	private final TypedExpression<T> argument;

	public TruncFunction(TypedExpression<T> argument) {
		super();
		this.argument = argument;
	}

	@Override
	public Class<? extends T> getType() {
		return argument.getType();
	}

	@Override
	public void validate() throws InvalidExpressionException {
		if (argument == null) {
			throw new InvalidExpressionException("Null argument");
		}
	}

	@Override
	public List<TypedExpression<? extends T>> getExpressionArguments() {
		return Collections.singletonList(argument);
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	public static final class Resolver implements ExpressionResolver<TruncFunction, SQLFunction> {

		@Override
		public Optional<SQLFunction> resolve(TruncFunction expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(SQLFunction.create(args -> {
				StringBuilder sb = new StringBuilder();
				sb.append("trunc(");
				sb.append(args.get(0));
				sb.append(")");
				return sb.toString();
			}));
		}

		@Override
		public Class<? extends TruncFunction> getExpressionType() {
			return TruncFunction.class;
		}

		@Override
		public Class<? extends SQLFunction> getResolvedType() {
			return SQLFunction.class;
		}

	}

}
