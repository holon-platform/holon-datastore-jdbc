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
package com.holonplatform.datastore.jdbc.test.config;

import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreExpressionResolver;

@SuppressWarnings("serial")
public class TestJdbcDatastoreExpressionResolver implements JdbcDatastoreExpressionResolver<KeyOne, QueryFilter> {

	@Override
	public Optional<QueryFilter> resolve(KeyOne expression, SQLCompositionContext context)
			throws InvalidExpressionException {
		return Optional.of(PathProperty.create("keycode", long.class).eq(1L));
	}

	@Override
	public Class<? extends KeyOne> getExpressionType() {
		return KeyOne.class;
	}

	@Override
	public Class<? extends QueryFilter> getResolvedType() {
		return QueryFilter.class;
	}

}
