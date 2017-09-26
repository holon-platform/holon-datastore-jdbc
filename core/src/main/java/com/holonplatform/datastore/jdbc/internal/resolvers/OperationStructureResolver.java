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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;

/**
 * {@link OperationStructure} expression resolver.
 *
 * @since 5.0.0
 */
public enum OperationStructureResolver implements ExpressionResolver<OperationStructure, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends OperationStructure> getExpressionType() {
		return OperationStructure.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLToken> getResolvedType() {
		return SQLToken.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(OperationStructure expression, ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JdbcResolutionContext context = JdbcResolutionContext.checkContext(resolutionContext);
		final ResolutionQueryClause previous = context.getResolutionQueryClause().orElse(null);

		// from
		final RelationalTarget<?> target;
		try {
			context.setResolutionQueryClause(ResolutionQueryClause.FROM);

			// relational target
			target = JdbcDatastoreUtils.resolveExpression(context, expression.getTarget(), RelationalTarget.class,
					context);

			context.setTarget(target);

		} finally {
			context.setResolutionQueryClause(previous);
		}

		// configure statement

		final StringBuilder operation = new StringBuilder();

		// check type
		final OperationType type = expression.getOperationType();

		switch (type) {
		case DELETE:
			if (context.getDialect().deleteStatementTargetRequired()) {
				operation.append("DELETE");
				context.getTargetAlias(target).ifPresent(a -> {
					operation.append(" ");
					operation.append(a);
				});
				operation.append(" FROM");
			} else {
				operation.append("DELETE FROM");
			}
			break;
		case INSERT:
			operation.append("INSERT INTO");
			break;
		case UPDATE:
			operation.append("UPDATE");
			break;
		default:
			break;
		}

		operation.append(" ");

		// target
		operation.append(JdbcDatastoreUtils.resolveExpression(context, target, SQLToken.class, context).getValue());

		// values
		if (type == OperationType.INSERT || type == OperationType.UPDATE) {

			final Map<Path<?>, Object> pathValues = expression.getValues();
			final List<String> paths = new ArrayList<>(pathValues.size());
			final List<String> values = new ArrayList<>(pathValues.size());

			try {
				context.setResolutionQueryClause(ResolutionQueryClause.SET);
				// resolve path and value
				for (Entry<Path<?>, Object> entry : pathValues.entrySet()) {
					paths.add(resolveExpression(entry.getKey(), context));
					values.add(resolvePathValue(entry.getKey(), entry.getValue(), context, true));
				}
			} finally {
				context.setResolutionQueryClause(previous);
			}

			// configure statement
			if (type == OperationType.INSERT) {

				operation.append(" (");
				operation.append(paths.stream().collect(Collectors.joining(",")));
				operation.append(") VALUES (");
				operation.append(values.stream().collect(Collectors.joining(",")));
				operation.append(")");

			} else if (type == OperationType.UPDATE) {

				operation.append(" SET ");
				for (int i = 0; i < paths.size(); i++) {
					if (i > 0) {
						operation.append(",");
					}
					operation.append(paths.get(i));
					operation.append("=");
					operation.append(values.get(i));
				}
			}

		}

		// filter
		if (type != OperationType.INSERT) {
			expression.getFilter().ifPresent(f -> {
				operation.append(" WHERE ");
				operation.append(JdbcDatastoreUtils.resolveExpression(context, f, SQLToken.class, context).getValue());
			});
		}

		// return SQL statement
		return Optional.of(SQLToken.create(operation.toString()));
	}

	/**
	 * Resolve given {@link Expression} to obtain the corresponding SQL expression.
	 * @param expression Expression to resolve
	 * @param context Resolution context
	 * @param clause Resolution clause
	 * @return SQL expression
	 * @throws InvalidExpressionException If expression cannot be resolved
	 */
	private static String resolveExpression(Expression expression, JdbcResolutionContext context) {
		SQLToken token = context.resolve(expression, SQLToken.class, context)
				.orElseThrow(() -> new InvalidExpressionException("Failed to resolve expression [" + expression + "]"));
		token.validate();
		return token.getValue();
	}

	/**
	 * Resolve a value associated to a {@link Path} to obtain the corresponding SQL expression.
	 * @param path Path
	 * @param value Value
	 * @param context Resolution context
	 * @param clause Resolution clause
	 * @param allowNull if <code>true</code>, null values are allowed and returned as <code>NULL</code> keyword
	 * @return SQL expression
	 * @throws InvalidExpressionException If expression cannot be resolved
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String resolvePathValue(Path<?> path, Object value, JdbcResolutionContext context,
			boolean allowNull) {

		if (value != null && "?".equals(value)) {
			return "?";
		}

		QueryExpression<?> expression = (QueryExpression.class.isAssignableFrom(path.getClass()))
				? QueryUtils.asConstantExpression((QueryExpression) path, value) : ConstantExpression.create(value);

		return JdbcDatastoreUtils.resolveExpression(context, expression, SQLToken.class, context).getValue();
	}

}
