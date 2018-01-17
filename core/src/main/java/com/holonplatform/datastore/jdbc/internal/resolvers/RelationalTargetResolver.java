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
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Join;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * {@link RelationalTarget} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum RelationalTargetResolver implements ExpressionResolver<RelationalTarget, SQLToken> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends RelationalTarget> getExpressionType() {
		return RelationalTarget.class;
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
	@SuppressWarnings("unchecked")
	@Override
	public Optional<SQLToken> resolve(RelationalTarget expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JdbcResolutionContext jdbcContext = JdbcResolutionContext.checkContext(context);

		final StringBuilder sb = new StringBuilder();

		// root path
		sb.append(getSQLPath(jdbcContext, expression));

		// resolve joins
		sb.append(expression.getJoins().stream().map(j -> resolveJoin((Join<?>) j, jdbcContext))
				.collect(Collectors.joining(" ")));

		return Optional.of(SQLToken.create(sb.toString().trim()));
	}

	/**
	 * Get the SQL path representation, appending alias name if available
	 * @param context Resolution context
	 * @param path Path to convert
	 * @return SQL expression
	 */
	private static String getSQLPath(JdbcResolutionContext context, final Path<?> path) {
		return context.getAlias(path).map(a -> {
			StringBuilder pb = new StringBuilder();
			pb.append(path.getName());
			pb.append(" ");
			pb.append(a);
			return pb.toString();
		}).orElse(path.getName());
	}

	/**
	 * Resolve a {@link Join} clause.
	 * @param sb String builder to use to append the resolved join SQL
	 * @param join Join to resolve
	 * @param context Resolution context
	 * @return Resolved join SQL
	 * @throws InvalidExpressionException If an error occurred
	 */
	private static String resolveJoin(Join<?> join, JdbcResolutionContext context) throws InvalidExpressionException {
		ObjectUtils.argumentNotNull(join, "Join must be not null");

		final StringBuilder sb = new StringBuilder();
		sb.append(" ");

		switch (join.getJoinType()) {
		case INNER:
			sb.append("JOIN ");
			break;
		case LEFT:
			sb.append("LEFT ");
			if (context.getDialect().useOuterInJoins()) {
				sb.append("OUTER ");
			}
			sb.append("JOIN ");
			break;
		case RIGHT:
			sb.append("RIGHT ");
			if (context.getDialect().useOuterInJoins()) {
				sb.append("OUTER ");
			}
			sb.append("JOIN ");
			break;
		default:
			break;
		}

		// join
		sb.append(getSQLPath(context, join));

		// ON condition
		join.getOn().ifPresent(o -> {
			sb.append(" ON ");
			sb.append(context.resolveExpression(o, SQLToken.class).getValue());
		});

		return sb.toString();
	}

}
