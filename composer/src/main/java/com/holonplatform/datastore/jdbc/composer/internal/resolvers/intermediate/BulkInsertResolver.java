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
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.bulk.BulkInsertConfiguration;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterizableExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link BulkInsertConfiguration} resolver.
 * 
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum BulkInsertResolver implements SQLContextExpressionResolver<BulkInsertConfiguration, SQLStatement> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends BulkInsertConfiguration> getExpressionType() {
		return BulkInsertConfiguration.class;
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
	public Optional<SQLStatement> resolve(BulkInsertConfiguration expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(expression.getTarget(), RelationalTarget.class);

		// build a statement context
		final SQLStatementCompositionContext operationContext = SQLStatementCompositionContext.asChild(context, target,
				AliasMode.UNSUPPORTED);

		final StringBuilder operation = new StringBuilder();

		operation.append("INSERT INTO");
		operation.append(" ");

		// target
		operation.append(operationContext.resolveOrFail(target, SQLExpression.class).getValue());

		final List<String> paths = new ArrayList<>();
		final List<String> values = new ArrayList<>();

		// check single or multi value
		final boolean singleValue = expression.getValues().size() == 1;

		if (singleValue) {
			// single value
			final Map<Path<?>, TypedExpression<?>> pathValues = expression.getValues().get(0);
			// use operation paths if available
			Path<?>[] operationPaths = expression.getOperationPaths()
					.orElse(pathValues.keySet().toArray(new Path<?>[0]));
			for (Path<?> path : operationPaths) {
				final TypedExpression<?> pathExpression = pathValues.get(path);
				if (pathExpression != null) {
					paths.add(context.resolveOrFail(path, SQLExpression.class).getValue());
					values.add(context
							.resolveOrFail(SQLParameterizableExpression.create(pathExpression), SQLExpression.class)
							.getValue());
				}
			}
		} else {
			// multi value, operation paths are required
			Path<?>[] operationPaths = expression.getOperationPaths()
					.orElseThrow(() -> new InvalidExpressionException("Missing bulk insert operation paths"));
			for (Path<?> path : operationPaths) {
				paths.add(context.resolveOrFail(path, SQLExpression.class).getValue());
				values.add("?"); // TODO non parameter expressions such as CURRENT_DATE?
			}
		}

		operation.append(" (");
		operation.append(paths.stream().collect(Collectors.joining(",")));
		operation.append(") VALUES (");
		operation.append(values.stream().collect(Collectors.joining(",")));
		operation.append(")");

		if (singleValue) {
			// prepare SQL and return SQLStatement
			return Optional.of(context.prepareStatement(operation.toString()));
		} else {
			return Optional.of(SQLStatement.create(operation.toString()));
		}
	}

}
