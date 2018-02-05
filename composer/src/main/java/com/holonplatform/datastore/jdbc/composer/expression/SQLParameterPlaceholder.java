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
package com.holonplatform.datastore.jdbc.composer.expression;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLParameterPlaceholder;

/**
 * Expression which represents a SQL parameter placeholder.
 * 
 * @param <T> Parameter value type
 *
 * @since 5.1.0
 */
public interface SQLParameterPlaceholder<T> extends TypedExpression<T> {

	/**
	 * Create a new {@link SQLParameterPlaceholder}.
	 * @param <T> Parameter value type
	 * @param type Parameter value type
	 * @return A new {@link SQLParameterPlaceholder}
	 */
	static <T> SQLParameterPlaceholder<T> create(Class<? extends T> type) {
		return new DefaultSQLParameterPlaceholder<>(type);
	}

}
