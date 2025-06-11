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

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.datastore.operation.common.DeleteOperationConfiguration;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext.AliasMode;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * {@link DeleteOperationConfiguration} resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum DeleteOperationConfigurationResolver
		implements SQLContextExpressionResolver<DeleteOperationConfiguration, SQLStatement> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends DeleteOperationConfiguration> getExpressionType() {
		return DeleteOperationConfiguration.class;
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
	public Optional<SQLStatement> resolve(DeleteOperationConfiguration expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(expression.getTarget(), RelationalTarget.class);

		// build a statement context
		final SQLStatementCompositionContext operationContext = SQLStatementCompositionContext.asChild(context, target,
				context.getDialect().deleteStatementAliasSupported() ? AliasMode.AUTO : AliasMode.UNSUPPORTED);

		final StringBuilder operation = new StringBuilder();

		if (operationContext.getDialect().deleteStatementTargetRequired()) {
			operation.append("DELETE");
			operationContext.getAlias(target, false).ifPresent(a -> {
				operation.append(" ");
				operation.append(a);
			});
			operation.append(" FROM");
		} else {
			operation.append("DELETE FROM");
		}
		operation.append(" ");

		// target
		operation.append(operationContext.resolveOrFail(target, SQLExpression.class).getValue());

		// filter
		expression.getFilter().ifPresent(f -> {
			operation.append(" WHERE ");
			operation.append(operationContext.resolveOrFail(f, SQLExpression.class).getValue());
		});

		// prepare SQL and return SQLStatement
		return Optional.of(operationContext.prepareStatement(operation.toString()));
	}

}
