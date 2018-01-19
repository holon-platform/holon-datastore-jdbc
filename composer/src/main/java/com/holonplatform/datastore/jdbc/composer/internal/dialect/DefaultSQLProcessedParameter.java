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

import java.util.Optional;
import java.util.function.Function;

import com.holonplatform.datastore.jdbc.composer.SQLDialect.SQLProcessedParameter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;

/**
 * Default {@link SQLProcessedParameter} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLProcessedParameter implements SQLProcessedParameter {

	private final SQLParameter parameter;

	private final Function<String, String> parameterSerializer;

	/**
	 * Constructor.
	 * @param parameter Parameter (not null)
	 */
	public DefaultSQLProcessedParameter(SQLParameter parameter) {
		this(parameter, null);
	}

	/**
	 * Constructor.
	 * @param parameter Parameter (not null)
	 * @param parameterSerializer Optional parameter serializer
	 */
	public DefaultSQLProcessedParameter(SQLParameter parameter, Function<String, String> parameterSerializer) {
		super();
		this.parameter = parameter;
		this.parameterSerializer = parameterSerializer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect.SQLProcessedParameter#getParameter()
	 */
	@Override
	public SQLParameter getParameter() {
		return parameter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLDialect.SQLProcessedParameter#getParameterSerializer()
	 */
	@Override
	public Optional<Function<String, String>> getParameterSerializer() {
		return Optional.ofNullable(parameterSerializer);
	}

}
