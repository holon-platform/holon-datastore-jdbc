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
package com.holonplatform.datastore.jdbc.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

@SuppressWarnings({ "unused", "serial", "rawtypes" })
public class ExampleJdbcDatastoreFunction {

	// tag::function1[]
	public class IfNull<T> implements QueryFunction<T, T> {

		private final TypedExpression<T> nullableValue; // <1>
		private final TypedExpression<T> fallbackValue; // <2>

		public IfNull(TypedExpression<T> nullableValue, TypedExpression<T> fallbackValue) {
			super();
			this.nullableValue = nullableValue;
			this.fallbackValue = fallbackValue;
		}

		public IfNull(QueryExpression<T> nullableValue, T fallbackValue) { // <3>
			this(nullableValue, ConstantExpression.create(fallbackValue));
		}

		@Override
		public Class<? extends T> getType() {
			return nullableValue.getType();
		}

		@Override
		public void validate() throws InvalidExpressionException { // <4>
			if (nullableValue == null) {
				throw new InvalidExpressionException("Missing nullable expression");
			}
			if (fallbackValue == null) {
				throw new InvalidExpressionException("Missing fallback expression");
			}
		}

		@Override
		public List<TypedExpression<? extends T>> getExpressionArguments() { // <5>
			return Arrays.asList(nullableValue, fallbackValue);
		}

	}
	// end::function1[]

	// tag::function2[]
	public class IfNullResolver implements ExpressionResolver<IfNull, SQLFunction> {

		@Override
		public Optional<SQLFunction> resolve(IfNull expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(SQLFunction.create(args -> { // <1>
				StringBuilder sb = new StringBuilder();
				sb.append("IFNULL(");
				sb.append(args.get(0));
				sb.append(",");
				sb.append(args.get(1));
				sb.append(")");
				return sb.toString();
			}));
		}

		@Override
		public Class<? extends IfNull> getExpressionType() {
			return IfNull.class;
		}

		@Override
		public Class<? extends SQLFunction> getResolvedType() {
			return SQLFunction.class;
		}

	}
	// end::function2[]

	public void ifnull() {
		// tag::function3[]
		final StringProperty STR = StringProperty.create("str");
		final DataTarget<?> TARGET = DataTarget.named("test");

		Datastore datastore = JdbcDatastore.builder() //
				.withExpressionResolver(new IfNullResolver()) // <1>
				.build();

		Stream<String> values = datastore.query(TARGET).stream(new IfNull<>(STR, "(fallback)")); // <2>
		// end::function3[]
	}

}
