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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.query.ConstantExpressionProjection;
import com.holonplatform.core.internal.query.QueryProjectionVisitor;
import com.holonplatform.core.internal.query.QueryProjectionVisitor.VisitableQueryProjection;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.CountAllProjection;
import com.holonplatform.core.query.FunctionExpression;
import com.holonplatform.core.query.PropertySetProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jdbc.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreUtils;
import com.holonplatform.datastore.jdbc.internal.converters.BeanResultSetConverter;
import com.holonplatform.datastore.jdbc.internal.converters.PropertyBoxResultSetConverter;
import com.holonplatform.datastore.jdbc.internal.converters.QueryExpressionResultSetConverter;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.ProjectionContext;

/**
 * {@link VisitableQueryProjection} expression resolver.
 * 
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQueryProjectionResolver implements ExpressionResolver<VisitableQueryProjection, ProjectionContext>,
		QueryProjectionVisitor<ProjectionContext, JdbcResolutionContext> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends VisitableQueryProjection> getExpressionType() {
		return VisitableQueryProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(VisitableQueryProjection expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional
				.ofNullable((ProjectionContext) expression.accept(this, JdbcResolutionContext.checkContext(context)));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.datastore.DataTarget,
	 * java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(DataTarget<T> projection, JdbcResolutionContext context) {
		throw new UnsupportedOperationException("DataTarget projection is not supported by the JDBC Datastore");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.property.PathProperty,
	 * java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(PathProperty<T> projection, JdbcResolutionContext context) {
		DefaultProjectionContext<T> ctx = new DefaultProjectionContext<>(context);
		final String alias = ctx.addSelection(
				JdbcDatastoreUtils.resolveExpression(context, projection, SQLToken.class, context).getValue());
		ctx.setConverter(new QueryExpressionResultSetConverter<>(context.getDialect(), projection, alias));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.internal.query.
	 * ConstantExpressionProjection, java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(ConstantExpressionProjection<T> projection, JdbcResolutionContext context) {
		final String sql = SQLValueSerializer.serializeValue(projection.getValue(), null);
		DefaultProjectionContext<T> ctx = new DefaultProjectionContext<>(context);
		String alias = ctx.addSelection(sql, false);
		ctx.setConverter(new QueryExpressionResultSetConverter<>(context.getDialect(), projection, alias));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * FunctionExpression, java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(FunctionExpression<T> projection, JdbcResolutionContext context) {
		DefaultProjectionContext<T> ctx = new DefaultProjectionContext<>(context);
		final String alias = ctx.addSelection(
				JdbcDatastoreUtils.resolveExpression(context, projection, SQLToken.class, context).getValue());
		ctx.setConverter(new QueryExpressionResultSetConverter<>(context.getDialect(), projection, alias));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * PropertySetProjection, java.lang.Object)
	 */
	@Override
	public ProjectionContext visit(PropertySetProjection projection, JdbcResolutionContext context) {

		final DefaultProjectionContext<PropertyBox> ctx = new DefaultProjectionContext<>(context);
		final Map<String, Property<?>> propertySelection = new LinkedHashMap<>();

		for (Property<?> property : projection.getPropertySet()) {
			if (QueryExpression.class.isAssignableFrom(property.getClass())) {
				final String alias = ctx.addSelection(JdbcDatastoreUtils
						.resolveExpression(context, (QueryExpression<?>) property, SQLToken.class, context).getValue());
				propertySelection.put(alias, property);
			}
		}

		ctx.setConverter(new PropertyBoxResultSetConverter(context.getDialect(), projection.getPropertySet(),
				propertySelection));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.BeanProjection,
	 * java.lang.Object)
	 */
	@Override
	public <T> ProjectionContext visit(BeanProjection<T> projection, JdbcResolutionContext context) {

		final DefaultProjectionContext<T> ctx = new DefaultProjectionContext<>(context);
		final BeanPropertySet<T> bps = BeanIntrospector.get().getPropertySet(projection.getBeanClass());
		final Map<String, Path<?>> pathSelection = new LinkedHashMap<>();

		List<Path> selection = projection.getSelection().map(s -> Arrays.asList(s))
				.orElse(bps.stream().map(p -> (Path) p).collect(Collectors.toList()));
		for (Path<?> path : selection) {
			if (QueryExpression.class.isAssignableFrom(path.getClass())) {
				final String alias = ctx.addSelection(JdbcDatastoreUtils
						.resolveExpression(context, (QueryExpression<?>) path, SQLToken.class, context).getValue());
				pathSelection.put(alias, path);
			}
		}

		ctx.setConverter(new BeanResultSetConverter<>(context.getDialect(), bps, pathSelection));
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryProjectionVisitor#visit(com.holonplatform.core.query.
	 * CountAllProjection, java.lang.Object)
	 */
	@Override
	public ProjectionContext visit(CountAllProjection projection, JdbcResolutionContext context) {
		return visit(FunctionExpression.count(Path.of("*", Object.class)), context);
	}

}
