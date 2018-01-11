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
package com.holonplatform.datastore.jdbc.internal.context;

import java.util.List;

import com.holonplatform.datastore.jdbc.expressions.SQLParameterDefinition;

/**
 * SQL statement configurator.
 * 
 * @param <S> Statement type
 *
 * @since 5.1.0
 */
public interface SQLStatementConfigurator<S> {

	/**
	 * Configure given <code>statement</code>, setting any parameter value.
	 * @param statement Statement to configure
	 * @param sql The precompiled SQL statement
	 * @param parameterValues Parameter values to set (if any), in the right sequence
	 * @throws StatementConfigurationException If an error occurred
	 */
	void configureStatement(S statement, String sql, List<SQLParameterDefinition> parameterValues)
			throws StatementConfigurationException;

}
