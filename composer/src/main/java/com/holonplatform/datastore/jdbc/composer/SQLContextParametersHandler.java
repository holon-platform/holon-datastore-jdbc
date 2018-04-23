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
package com.holonplatform.datastore.jdbc.composer;

import java.io.Serializable;
import java.util.Map;

import com.holonplatform.datastore.jdbc.composer.expression.SQLParameter;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLContextParametersHandler;

/**
 * Handler to generate, hold and obtain the SQL named parameters of a SQL context hierarchy.
 *
 * @since 5.1.0
 */
public interface SQLContextParametersHandler extends Serializable {

	/**
	 * Add named parameter using given {@link SQLParameter} definition.
	 * @param <T> Parameter expression type
	 * @param parameter Parameter definition (not null)
	 * @return Generated parameter name
	 */
	<T> String addNamedParameter(SQLParameter<T> parameter);

	/**
	 * Get the named parameters.
	 * @return A map of parameter names and the associated {@link SQLParameter} definition, empty if none
	 */
	Map<String, SQLParameter<?>> getNamedParameters();

	/**
	 * Create a new {@link SQLContextParametersHandler}.
	 * @return A new {@link SQLContextParametersHandler}
	 */
	static SQLContextParametersHandler create() {
		return new DefaultSQLContextParametersHandler();
	}

}
