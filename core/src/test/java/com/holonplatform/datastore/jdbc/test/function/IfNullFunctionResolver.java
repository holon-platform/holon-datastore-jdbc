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
package com.holonplatform.datastore.jdbc.test.function;

import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

@SuppressWarnings({ "serial", "rawtypes" })
public class IfNullFunctionResolver implements ExpressionResolver<IfNullFunction, SQLFunction> {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<SQLFunction> resolve(IfNullFunction expression, ResolutionContext context)
			throws InvalidExpressionException {
		return Optional.of(SQLFunction.create(args -> {
			StringBuilder sb = new StringBuilder();
			sb.append("IFNULL(");
			sb.append(args.get(0));
			sb.append(",");
			sb.append(args.get(1));
			sb.append(")");
			return sb.toString();
		}));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends IfNullFunction> getExpressionType() {
		return IfNullFunction.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLFunction> getResolvedType() {
		return SQLFunction.class;
	}

}
