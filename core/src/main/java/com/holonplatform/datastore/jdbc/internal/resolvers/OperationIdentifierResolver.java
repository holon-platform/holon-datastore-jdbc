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

import java.util.Optional;
import java.util.Set;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.operation.commons.PropertyBoxOperationConfiguration;
import com.holonplatform.core.Path;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.support.DataPathAwarePathMatcher;

/**
 * {@link PropertyBoxOperationConfiguration} identifier resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public class OperationIdentifierResolver
		implements SQLContextExpressionResolver<PropertyBoxOperationConfiguration, SQLPrimaryKey> {

	private static final long serialVersionUID = -8564884719990404206L;

	private final JdbcOperationContext operationContext;

	public OperationIdentifierResolver(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PropertyBoxOperationConfiguration> getExpressionType() {
		return PropertyBoxOperationConfiguration.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLPrimaryKey> getResolvedType() {
		return SQLPrimaryKey.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLPrimaryKey> resolve(PropertyBoxOperationConfiguration expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		switch (operationContext.getIdentifierResolutionStrategy()) {
		case IDENTIFIER_PROPERTIES:
			return getPrimaryKeyFromIdentifiers(expression.getValue());
		case TABLE_PRIMARY_KEY:
			return context.resolve(expression.getTarget(), SQLPrimaryKey.class);
		case AUTO:
		default:
			Optional<SQLPrimaryKey> pk = getPrimaryKeyFromIdentifiers(expression.getValue());
			if (pk.isPresent()) {
				return pk;
			}
			return context.resolve(expression.getTarget(), SQLPrimaryKey.class);
		}
	}

	/**
	 * Get the {@link SQLPrimaryKey} using given property box identifier properties, if available.
	 * @param propertyBox The property box for which to obtain the identifiers
	 * @return Optional property box identifiers as a {@link SQLPrimaryKey}
	 */
	private static Optional<SQLPrimaryKey> getPrimaryKeyFromIdentifiers(PropertyBox propertyBox) {
		if (propertyBox != null) {
			final Set<Path<?>> ids = PathPropertyBoxAdapter.builder(propertyBox)
					.pathMatcher(new DataPathAwarePathMatcher()).build().getPathIdentifiers();
			if (!ids.isEmpty()) {
				return Optional.of(SQLPrimaryKey.create(ids.toArray(new Path<?>[ids.size()])));
			}
		}
		return Optional.empty();
	}

}
