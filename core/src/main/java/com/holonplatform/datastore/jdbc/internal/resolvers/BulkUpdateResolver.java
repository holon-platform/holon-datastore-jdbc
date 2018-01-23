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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.bulk.BulkUpdateConfiguration;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

@Priority(Integer.MAX_VALUE)
public enum BulkUpdateResolver implements ExpressionResolver<BulkUpdateConfiguration, SQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(BulkUpdateConfiguration expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JdbcResolutionContext context = JdbcResolutionContext.checkContext(resolutionContext);

		// target
		final RelationalTarget<?> target = context.resolveExpression(expression.getTarget(),
				RelationalTarget.class);
		context.setTarget(target);

		final StringBuilder operation = new StringBuilder();

		operation.append("UPDATE");
		operation.append(" ");

		// target
		operation.append(context.resolveExpression(target, SQLToken.class).getValue());

		// values
		final Map<Path<?>, TypedExpression<?>> pathValues = expression.getValues();

		final List<String> paths = new ArrayList<>(pathValues.size());
		final List<String> values = new ArrayList<>(pathValues.size());
		
		for (Path<?> path : pathValues.keySet()) {
			TypedExpression<?> pathExpression = pathValues.get(path);
			if (pathExpression != null) {
				paths.add(context.resolveExpression(path, SQLToken.class).getValue());
				values.add(context.resolveExpression(pathExpression, SQLToken.class).getValue());
			}
		}

		operation.append(" SET ");
		for (int i = 0; i < paths.size(); i++) {
			if (i > 0) {
				operation.append(",");
			}
			operation.append(paths.get(i));
			operation.append("=");
			operation.append(values.get(i));
		}

		// filter
		expression.getFilter().ifPresent(f -> {
			operation.append(" WHERE ");
			operation.append(context.resolveExpression(f, SQLToken.class).getValue());
		});

		return Optional.of(SQLToken.create(operation.toString()));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends BulkUpdateConfiguration> getExpressionType() {
		return BulkUpdateConfiguration.class;
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
