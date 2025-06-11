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
package com.holonplatform.datastore.jdbc.composer.internal.dialect;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;

@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum ReaderToStringParameterResolver implements SQLContextExpressionResolver<SQLParameter, SQLParameter> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends SQLParameter> getExpressionType() {
		return SQLParameter.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends SQLParameter> getResolvedType() {
		return SQLParameter.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jdbc.composer.SQLCompositionContext)
	 */
	@SuppressWarnings("resource")
	@Override
	public Optional<SQLParameter> resolve(SQLParameter expression, SQLCompositionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		if (expression.getValue() != null && expression.getValue() instanceof Reader) {
			final Reader reader = (Reader) expression.getValue();
			try {
				return Optional.of(SQLParameter.create(ConversionUtils.readerToString(reader, false), String.class));
			} catch (IOException e) {
				throw new InvalidExpressionException("Failed to convert Reader [" + reader + "] to String", e);
			}
		}

		return Optional.empty();
	}

}
