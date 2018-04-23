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

import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;

@SuppressWarnings({ "rawtypes", "serial" })
public class MyEqualPredicateResolver implements ExpressionResolver<MyEqualPredicate, SQLExpression> {

	@Override
	public Optional<SQLExpression> resolve(MyEqualPredicate expression, ResolutionContext context)
			throws InvalidExpressionException {

		expression.validate();

		return SQLCompositionContext.isSQLCompositionContext(context).flatMap(ctx -> {
			StringBuilder sb = new StringBuilder();
			// path property resolution
			sb.append(ctx.resolveOrFail(expression.getProperty(), SQLExpression.class).getValue());
			// operator
			sb.append(" = ");
			// value
			sb.append(ctx.getValueSerializer().serialize(expression.getValue()));
			// return a SQLExpression
			return Optional.ofNullable(SQLExpression.create(sb.toString()));
		});
	}

	@Override
	public Class<? extends MyEqualPredicate> getExpressionType() {
		return MyEqualPredicate.class;
	}

	@Override
	public Class<? extends SQLExpression> getResolvedType() {
		return SQLExpression.class;
	}

}
