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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLContextParametersHandler;
import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;

/**
 * Default {@link SQLContextParametersHandler} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLContextParametersHandler implements SQLContextParametersHandler {

	private static final long serialVersionUID = 3466242242359565279L;

	private final static Logger LOGGER = SQLComposerLogger.create();

	/**
	 * Named parameters
	 */
	private final Map<String, SQLParameter<?>> namedParameters = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.internal.SQLContextParametersHandler#addNamedParameter(com.
	 * holonplatform.datastore.jdbc.composer.expression.SQLParameter)
	 */
	@Override
	public <T> String addNamedParameter(SQLParameter<T> parameter) {
		ObjectUtils.argumentNotNull(parameter, "Parameter must be not null");
		synchronized (namedParameters) {
			// generate name
			final String name = generateParameterName(namedParameters.size() + 1);
			// add parameter
			namedParameters.put(name, parameter);

			LOGGER.debug(() -> "Added parameter with name " + name);

			// return the generated name
			return name;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.internal.SQLContextParametersHandler#getNamedParameters()
	 */
	@Override
	public Map<String, SQLParameter<?>> getNamedParameters() {
		return Collections.unmodifiableMap(namedParameters);
	}

	/**
	 * Generate a named parameter name. By default, the pattern <code>:[001]</code> is used.
	 * @param index Parameter index
	 * @return Parameter name
	 */
	protected String generateParameterName(int index) {
		return ":[" + String.format("%04d", index) + "]";
	}

}
