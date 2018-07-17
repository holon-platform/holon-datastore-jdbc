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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.util.ArrayList;
import java.util.List;

import com.holonplatform.datastore.jdbc.composer.internal.resolvers.CollectionExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.ConstantExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.ExistFilterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.NotExistFilterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.NullExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.PathResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.QueryAggregationResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.QueryFilterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.QueryFunctionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.QuerySortResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.RelationalTargetResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLLiteralResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLOrderBySortResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLParameterPlaceholderResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLParameterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLParameterizableExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLQueryClausesResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLQueryResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLTokenResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SQLWhereFilterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.SubQueryResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.VisitableQueryFilterResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.VisitableQuerySortResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.DataTargetResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.DefaultQueryFunctionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.DeleteOperationConfigurationResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.DialectQueryFunctionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.InsertOperationConfigurationResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.QueryOperationClausesResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.QueryResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.intermediate.UpdateOperationConfigurationResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.BeanProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.ConstantExpressionProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.CountAllProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.PropertySetProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.QueryProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.SelectAllProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.internal.resolvers.projection.TypedExpressionProjectionResolver;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

/**
 * Default resolvers.
 * 
 * @since 5.1.0
 */
public class DefaultSQLExpressionResolvers {

	@SuppressWarnings("rawtypes")
	private static final List<SQLContextExpressionResolver> expressionResolvers = new ArrayList<>();

	static {
		expressionResolvers.add(NullExpressionResolver.INSTANCE);
		expressionResolvers.add(SQLParameterPlaceholderResolver.INSTANCE);
		expressionResolvers.add(SQLTokenResolver.INSTANCE);
		expressionResolvers.add(ConstantExpressionResolver.INSTANCE);
		expressionResolvers.add(CollectionExpressionResolver.INSTANCE);
		expressionResolvers.add(PathResolver.INSTANCE);
		expressionResolvers.add(RelationalTargetResolver.INSTANCE);
		expressionResolvers.add(QueryFunctionResolver.INSTANCE);
		expressionResolvers.add(QueryFilterResolver.INSTANCE);
		expressionResolvers.add(QuerySortResolver.INSTANCE);
		expressionResolvers.add(VisitableQueryFilterResolver.INSTANCE);
		expressionResolvers.add(VisitableQuerySortResolver.INSTANCE);
		expressionResolvers.add(ExistFilterResolver.INSTANCE);
		expressionResolvers.add(NotExistFilterResolver.INSTANCE);
		expressionResolvers.add(QueryAggregationResolver.INSTANCE);
		expressionResolvers.add(SQLWhereFilterResolver.INSTANCE);
		expressionResolvers.add(SQLOrderBySortResolver.INSTANCE);
		expressionResolvers.add(SQLLiteralResolver.INSTANCE);
		expressionResolvers.add(SQLParameterizableExpressionResolver.INSTANCE);
		expressionResolvers.add(SQLParameterResolver.INSTANCE);
		expressionResolvers.add(SQLQueryClausesResolver.INSTANCE);
		expressionResolvers.add(SQLQueryResolver.INSTANCE);
		expressionResolvers.add(SubQueryResolver.INSTANCE);

		expressionResolvers.add(DataTargetResolver.INSTANCE);
		expressionResolvers.add(DialectQueryFunctionResolver.INSTANCE);
		expressionResolvers.add(DefaultQueryFunctionResolver.INSTANCE);
		expressionResolvers.add(QueryOperationClausesResolver.INSTANCE);
		expressionResolvers.add(QueryResolver.INSTANCE);
		expressionResolvers.add(InsertOperationConfigurationResolver.INSTANCE);
		expressionResolvers.add(UpdateOperationConfigurationResolver.INSTANCE);
		expressionResolvers.add(DeleteOperationConfigurationResolver.INSTANCE);

		expressionResolvers.add(QueryProjectionResolver.INSTANCE);
		expressionResolvers.add(TypedExpressionProjectionResolver.INSTANCE);
		expressionResolvers.add(ConstantExpressionProjectionResolver.INSTANCE);
		expressionResolvers.add(PropertySetProjectionResolver.INSTANCE);
		expressionResolvers.add(BeanProjectionResolver.INSTANCE);
		expressionResolvers.add(CountAllProjectionResolver.INSTANCE);
		expressionResolvers.add(SelectAllProjectionResolver.INSTANCE);
	}

	/**
	 * Get the available expression resolvers.
	 * @return the expression resolvers
	 */
	@SuppressWarnings("rawtypes")
	public static List<SQLContextExpressionResolver> getExpressionresolvers() {
		return expressionResolvers;
	}

}
