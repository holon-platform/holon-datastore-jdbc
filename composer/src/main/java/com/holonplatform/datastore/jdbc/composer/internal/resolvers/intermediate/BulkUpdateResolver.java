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
package com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.bulk.BulkUpdateConfiguration;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterizableExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link BulkUpdateConfiguration} resolver.
 * 
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum BulkUpdateResolver implements SQLContextExpressionResolver<BulkUpdateConfiguration, SQLStatement> {

	INSTANCE;

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
	public Class<? extends SQLStatement> getResolvedType() {
		return SQLStatement.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLStatement> resolve(BulkUpdateConfiguration expression, SQLCompositionContext context) {

		// validate
		expression.validate();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(expression.getTarget(), RelationalTarget.class);

		// build a statement context
		final SQLStatementCompositionContext operationContext = SQLStatementCompositionContext.asChild(context, target,
				AliasMode.UNSUPPORTED); // TODO why not AUTO? check sub queries

		final StringBuilder operation = new StringBuilder();

		operation.append("UPDATE");
		operation.append(" ");

		// target
		operation.append(operationContext.resolveOrFail(target, SQLExpression.class).getValue());

		// values
		final Map<Path<?>, TypedExpression<?>> pathValues = expression.getValues();

		final List<String> paths = new ArrayList<>(pathValues.size());
		final List<String> values = new ArrayList<>(pathValues.size());

		for (Path<?> path : pathValues.keySet()) {
			TypedExpression<?> pathExpression = pathValues.get(path);
			if (pathExpression != null) {
				paths.add(context.resolveOrFail(path, SQLExpression.class).getValue());
				values.add(
						context.resolveOrFail(SQLParameterizableExpression.create(pathExpression), SQLExpression.class)
								.getValue());
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
			operation.append(operationContext.resolveOrFail(f, SQLExpression.class).getValue());
		});

		// prepare SQL and return SQLStatement
		return Optional.of(context.prepareStatement(operation.toString()));
	}

}
