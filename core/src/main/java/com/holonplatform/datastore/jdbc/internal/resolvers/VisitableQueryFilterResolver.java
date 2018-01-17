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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.internal.query.QueryFilterVisitor;
import com.holonplatform.core.internal.query.QueryFilterVisitor.VisitableQueryFilter;
import com.holonplatform.core.internal.query.filter.AndFilter;
import com.holonplatform.core.internal.query.filter.BetweenFilter;
import com.holonplatform.core.internal.query.filter.EqualFilter;
import com.holonplatform.core.internal.query.filter.GreaterFilter;
import com.holonplatform.core.internal.query.filter.InFilter;
import com.holonplatform.core.internal.query.filter.LessFilter;
import com.holonplatform.core.internal.query.filter.NotEqualFilter;
import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.core.internal.query.filter.NotInFilter;
import com.holonplatform.core.internal.query.filter.NotNullFilter;
import com.holonplatform.core.internal.query.filter.NullFilter;
import com.holonplatform.core.internal.query.filter.OperationQueryFilter;
import com.holonplatform.core.internal.query.filter.OrFilter;
import com.holonplatform.core.internal.query.filter.StringMatchFilter;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.StringFunction.Lower;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * JDBC {@link VisitableQueryFilter} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQueryFilterResolver implements ExpressionResolver<VisitableQueryFilter, SQLToken>,
		QueryFilterVisitor<SQLToken, JdbcResolutionContext> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends VisitableQueryFilter> getExpressionType() {
		return VisitableQueryFilter.class;
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
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLToken> resolve(VisitableQueryFilter expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional.ofNullable(expression.accept(this, JdbcResolutionContext.checkContext(context)));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NullFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(NullFilter filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IS NULL");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotNullFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(NotNullFilter filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IS NOT NULL");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * EqualFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(EqualFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append("=");
		sb.append(resolveRightOperand(filter, context));
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotEqualFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(NotEqualFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append("<>");
		sb.append(resolveRightOperand(filter, context));
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * GreaterFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(GreaterFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? ">=" : ">");
		sb.append(resolveRightOperand(filter, context));
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LessFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(LessFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? "<=" : "<");
		sb.append(resolveRightOperand(filter, context));
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * InFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(InFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IN (");
		sb.append(resolveRightOperand(filter, context));
		sb.append(")");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotInFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(NotInFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" NOT IN (");
		sb.append(resolveRightOperand(filter, context));
		sb.append(")");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * BetweenFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLToken visit(BetweenFilter<T> filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" BETWEEN ");
		sb.append(resolve(ConstantExpression.create(filter.getFromValue()), context));
		sb.append(" AND ");
		sb.append(resolve(ConstantExpression.create(filter.getToValue()), context));
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LikeFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(StringMatchFilter filter, JdbcResolutionContext context) {

		// right operand
		if (!filter.getRightOperand().isPresent()) {
			throw new InvalidExpressionException("Invalid StringMatchFilter right operand");
		}
		if (!(filter.getRightOperand().get() instanceof ConstantExpression)) {
			throw new InvalidExpressionException(
					"Invalid right operand expression for StringMatchFilter: [" + filter.getRightOperand().get() + "]");
		}
		Object resolved = ((ConstantExpression<?,?>) filter.getRightOperand().get()).getModelValue();
		if (resolved == null) {
			throw new InvalidExpressionException(
					"Invalid right operand value for StringMatchFilter: [" + resolved + "]");
		}

		String value = resolved.toString();

		// escape
		boolean escape = context.getDialect().supportsLikeEscapeClause();
		if (escape) {
			value = value.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
		}

		// add wildcards
		switch (filter.getMatchMode()) {
		case CONTAINS:
			value = "%" + value + "%";
			break;
		case ENDS_WITH:
			value = "%" + value;
			break;
		case STARTS_WITH:
			value = value + "%";
			break;
		default:
			break;
		}

		// check ignore case
		QueryExpression<String> left = (filter.isIgnoreCase()) ? Lower.create(filter.getLeftOperand())
				: filter.getLeftOperand();
		if (filter.isIgnoreCase()) {
			value = value.toLowerCase();
		}

		final String path = resolve(left, context);

		final StringBuilder sb = new StringBuilder();
		sb.append(path);

		sb.append(" LIKE ");
		sb.append(JdbcDatastoreUtils
				.resolveExpression(context, ConstantExpression.create(value), SQLToken.class, context).getValue());

		if (escape) {
			sb.append(" ESCAPE '!'");
		}

		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * AndFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(AndFilter filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") AND (", context));
		sb.append(")");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * OrFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(OrFilter filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") OR (", context));
		sb.append(")");
		return SQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotFilter, java.lang.Object)
	 */
	@Override
	public SQLToken visit(NotFilter filter, JdbcResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("NOT (");
		sb.append(JdbcDatastoreUtils.resolveExpression(context, filter.getComposition().get(0), SQLToken.class, context)
				.getValue());
		sb.append(")");
		return SQLToken.create(sb.toString());
	}

	/**
	 * Resolve a list of filters into {@link SQLToken}s and returns the resolved tokens joined with given
	 * <code>separator</code>.
	 * @param filters Filters to resolve
	 * @param separator Token separator
	 * @param context Resolution context
	 * @return The resolved tokens joined with given <code>separator</code>
	 * @throws InvalidExpressionException Failed to resolve a filter
	 */
	private static String resolveFilterList(List<QueryFilter> filters, String separator, JdbcResolutionContext context)
			throws InvalidExpressionException {
		List<String> resolved = new LinkedList<>();
		filters.forEach(f -> {
			resolved.add(JdbcDatastoreUtils.resolveExpression(context, f, SQLToken.class, context).getValue());
		});
		return resolved.stream().collect(Collectors.joining(separator));
	}

	/**
	 * Resolve given expression as {@link SQLToken} and return the SQL value
	 * @param expression Expression to resolve
	 * @param context Resolution context
	 * @return SQL value
	 * @throws InvalidExpressionException Failed to resolve the expression
	 */
	private static String resolve(Expression expression, JdbcResolutionContext context)
			throws InvalidExpressionException {
		return JdbcDatastoreUtils.resolveExpression(context, expression, SQLToken.class, context).getValue();
	}

	/**
	 * Resolve the right operand of a {@link OperationQueryFilter} and return the SQL value
	 * @param filter Filter
	 * @param context Resolution context
	 * @return SQL value
	 * @throws InvalidExpressionException Failed to resolve the expression
	 */
	private static String resolveRightOperand(OperationQueryFilter<?> filter, JdbcResolutionContext context)
			throws InvalidExpressionException {
		QueryExpression<?> operand = filter.getRightOperand()
				.orElseThrow(() -> new InvalidExpressionException("Missing right operand in filter [" + filter + "]"));
		return JdbcDatastoreUtils.resolveExpression(context, operand, SQLToken.class, context).getValue();
	}

}
