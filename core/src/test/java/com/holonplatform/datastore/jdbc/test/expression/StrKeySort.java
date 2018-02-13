/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.test.expression;

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;

import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;

@SuppressWarnings("serial")
public class StrKeySort implements QuerySort {

	@Override
	public void validate() throws InvalidExpressionException {
	}

	public static final ExpressionResolver<QuerySort, QuerySort> RESOLVER = ExpressionResolver.create(StrKeySort.class,
			QuerySort.class, (sort, ctx) -> Optional.of(STR.desc().and(KEY.asc())));
	
	public static final ExpressionResolver<QuerySort, SQLExpression> SQL_RESOLVER = ExpressionResolver.create(StrKeySort.class,
			SQLExpression.class, (sort, ctx) -> Optional.of(SQLExpression.create("strv desc, keycode asc")));

}
