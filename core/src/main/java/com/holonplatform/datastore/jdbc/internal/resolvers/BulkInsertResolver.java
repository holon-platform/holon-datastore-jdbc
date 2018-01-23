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
package com.holonplatform.datastore.jdbc.internal.resolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

@Priority(Integer.MAX_VALUE)
public enum BulkInsertResolver implements ExpressionResolver<BulkInsert, SQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(BulkInsert expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JdbcResolutionContext context = JdbcResolutionContext.checkContext(resolutionContext);

		// target
		final RelationalTarget<?> target = context.resolveExpression(expression.getConfiguration().getTarget(),
				RelationalTarget.class);
		context.setTarget(target);

		final StringBuilder operation = new StringBuilder();

		operation.append("INSERT INTO");
		operation.append(" ");

		// target
		operation.append(context.resolveExpression(target, SQLToken.class).getValue());

		// values (the first map only)
		final Map<Path<?>, TypedExpression<?>> pathValues = expression.getConfiguration().getValues().get(0);

		boolean singleValue = expression.getConfiguration().getValues().size() == 1;

		final List<String> paths = new ArrayList<>(pathValues.size());
		final List<String> values = new ArrayList<>(pathValues.size());

		expression.getConfiguration().getOperationPaths().map(ops -> Arrays.asList(ops).stream())
				.orElse(pathValues.keySet().stream()).forEach(path -> {
					if (singleValue) {
						// single value
						TypedExpression<?> pathExpression = pathValues.get(path);
						if (pathExpression != null) {
							paths.add(context.resolveExpression(path, SQLToken.class).getValue());
							values.add(context.resolveExpression(pathExpression, SQLToken.class).getValue());
						}
					} else {
						// multi value
						paths.add(context.resolveExpression(path, SQLToken.class).getValue());
						values.add("?");
					}
				});

		operation.append(" (");
		operation.append(paths.stream().collect(Collectors.joining(",")));
		operation.append(") VALUES (");
		operation.append(values.stream().collect(Collectors.joining(",")));
		operation.append(")");

		return Optional.of(SQLToken.create(operation.toString()));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends BulkInsert> getExpressionType() {
		return BulkInsert.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLToken> getResolvedType() {
		return SQLToken.class;
	}

}
