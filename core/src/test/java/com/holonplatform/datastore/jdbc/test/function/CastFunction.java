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
package com.holonplatform.datastore.jdbc.test.function;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;
import com.holonplatform.datastore.jdbc.composer.expression.SQLToken;

public class CastFunction<T> implements QueryFunction<T, Object> {

	private final TypedExpression<T> argument;
	private final String dataType;

	public CastFunction(TypedExpression<T> argument, String dataType) {
		super();
		this.argument = argument;
		this.dataType = dataType;
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
		if (dataType == null) {
			throw new InvalidExpressionException("Null data type");
		}
	}

	@Override
	public List<TypedExpression<? extends Object>> getExpressionArguments() {
		return Arrays.asList(argument, SQLToken.create(dataType));
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	public static final class Resolver implements ExpressionResolver<CastFunction, SQLFunction> {

		@Override
		public Optional<SQLFunction> resolve(CastFunction expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(SQLFunction.create(args -> {
				StringBuilder sb = new StringBuilder();
				sb.append("cast(");
				sb.append(args.get(0));
				sb.append(" as ");
				sb.append(args.get(1));
				sb.append(")");
				return sb.toString();
			}));
		}

		@Override
		public Class<? extends CastFunction> getExpressionType() {
			return CastFunction.class;
		}

		@Override
		public Class<? extends SQLFunction> getResolvedType() {
			return SQLFunction.class;
		}

	}

}
