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
package com.holonplatform.datastore.jdbc.spring.test.config2;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.core.query.QuerySort.QuerySortResolver;
import com.holonplatform.datastore.jdbc.spring.test.expression.MySort;
import com.holonplatform.spring.DatastorePostProcessor;

@Component
public class TestDatastorePostProcessor implements DatastorePostProcessor {

	@Override
	public void postProcessDatastore(Datastore datastore, String beanName) {
		datastore.addExpressionResolver(new MySortExpressionResolver());
	}

	public class MySortExpressionResolver implements QuerySortResolver<MySort> {

		private static final long serialVersionUID = 1L;

		@Override
		public Class<? extends MySort> getExpressionType() {
			return MySort.class;
		}

		@Override
		public Optional<QuerySort> resolve(MySort sort, ResolutionContext ctx) throws InvalidExpressionException {
			return Optional.of(sort.getProperty().desc());
		}

	}

}
