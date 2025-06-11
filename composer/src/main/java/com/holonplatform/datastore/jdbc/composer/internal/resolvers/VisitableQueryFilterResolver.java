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
package com.holonplatform.datastore.jdbc.composer.internal.resolvers;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.ConstantConverterExpression;
import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.TypedExpression;
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
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.StringFunction.Lower;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameterizableExpression;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLExpressionResolver;

/**
 * JDBC {@link VisitableQueryFilter} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQueryFilterResolver implements SQLExpressionResolver<VisitableQueryFilter>,
		QueryFilterVisitor<SQLExpression, SQLCompositionContext> {

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
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@Override
	public Optional<SQLExpression> resolve(VisitableQueryFilter expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional.ofNullable(expression.accept(this, context));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NullFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(NullFilter filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(" IS NULL");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotNullFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(NotNullFilter filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(" IS NOT NULL");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * EqualFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(EqualFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append("=");
		sb.append(serializeRightOperand(filter, context));
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotEqualFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(NotEqualFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append("<>");
		sb.append(serializeRightOperand(filter, context));
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * GreaterFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(GreaterFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? ">=" : ">");
		sb.append(serializeRightOperand(filter, context));
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LessFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(LessFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? "<=" : "<");
		sb.append(serializeRightOperand(filter, context));
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * InFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(InFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(" IN (");
		sb.append(serializeRightOperand(filter, context));
		sb.append(")");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotInFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(NotInFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(" NOT IN (");
		sb.append(serializeRightOperand(filter, context));
		sb.append(")");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * BetweenFilter, java.lang.Object)
	 */
	@Override
	public <T> SQLExpression visit(BetweenFilter<T> filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(serialize(filter.getLeftOperand(), context));
		sb.append(" BETWEEN ");
		sb.append(serialize(
				SQLParameterizableExpression.create(ConstantConverterExpression.create(filter.getFromValue())),
				context));
		sb.append(" AND ");
		sb.append(serialize(
				SQLParameterizableExpression.create(ConstantConverterExpression.create(filter.getToValue())), context));
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LikeFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(StringMatchFilter filter, SQLCompositionContext context) {

		// check value
		String value = filter.getValue();
		if (value == null) {
			throw new InvalidExpressionException("String match filter value cannot be null");
		}

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
		TypedExpression<String> left = (filter.isIgnoreCase()) ? Lower.create(filter.getLeftOperand())
				: filter.getLeftOperand();
		if (filter.isIgnoreCase()) {
			value = value.toLowerCase();
		}

		final String path = serialize(left, context);

		final StringBuilder sb = new StringBuilder();
		sb.append(path);

		sb.append(" LIKE ");
		sb.append(context.resolveOrFail(
				SQLParameterizableExpression.create(ConstantConverterExpression.create(value, String.class)),
				SQLExpression.class).getValue());

		if (escape) {
			sb.append(" ESCAPE '!'");
		}

		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * AndFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(AndFilter filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") AND (", context));
		sb.append(")");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * OrFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(OrFilter filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") OR (", context));
		sb.append(")");
		return SQLExpression.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotFilter, java.lang.Object)
	 */
	@Override
	public SQLExpression visit(NotFilter filter, SQLCompositionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("NOT (");
		sb.append(context.resolveOrFail(filter.getComposition().get(0), SQLExpression.class).getValue());
		sb.append(")");
		return SQLExpression.create(sb.toString());
	}

	/**
	 * Resolve a list of filters into {@link SQLExpression}s and returns the resolved tokens joined with given
	 * <code>separator</code>.
	 * @param filters Filters to resolve
	 * @param separator Token separator
	 * @param context Resolution context
	 * @return The resolved tokens joined with given <code>separator</code>
	 * @throws InvalidExpressionException Failed to resolve a filter
	 */
	private static String resolveFilterList(List<QueryFilter> filters, String separator, SQLCompositionContext context)
			throws InvalidExpressionException {
		List<String> resolved = new LinkedList<>();
		filters.forEach(f -> {
			resolved.add(context.resolveOrFail(f, SQLExpression.class).getValue());
		});
		return resolved.stream().collect(Collectors.joining(separator));
	}

	/**
	 * Resolve given expression as {@link SQLExpression} and return the serialized SQL value.
	 * @param expression Expression to resolve
	 * @param context SQL context
	 * @return SQL value
	 */
	private static String serialize(Expression expression, SQLCompositionContext context)
			throws InvalidExpressionException {
		return context.resolveOrFail(expression, SQLExpression.class).getValue();
	}

	/**
	 * Resolve the right operand of a {@link OperationQueryFilter} and return the serialized SQL value.
	 * @param filter Filter
	 * @param context Resolution context
	 * @return SQL value
	 */
	private static String serializeRightOperand(OperationQueryFilter<?> filter, SQLCompositionContext context)
			throws InvalidExpressionException {
		TypedExpression<?> operand = filter.getRightOperand()
				.orElseThrow(() -> new InvalidExpressionException("Missing right operand in filter [" + filter + "]"));
		return context.resolveOrFail(SQLParameterizableExpression.create(operand), SQLExpression.class).getValue();
	}

}
