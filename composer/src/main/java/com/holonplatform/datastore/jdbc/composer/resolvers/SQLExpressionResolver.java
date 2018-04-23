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
package com.holonplatform.datastore.jdbc.composer.resolvers;

import com.holonplatform.core.Expression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;

/**
 * A {@link SQLContextExpressionResolver} to resolve an expression into a {@link SQLExpression}.
 * 
 * @param <E> Expression type to resolve
 *
 * @since 5.1.0
 */
public interface SQLExpressionResolver<E extends Expression> extends SQLContextExpressionResolver<E, SQLExpression> {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	default Class<? extends SQLExpression> getResolvedType() {
		return SQLExpression.class;
	}

}
