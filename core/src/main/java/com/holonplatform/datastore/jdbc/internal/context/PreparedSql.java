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
 * Represents a SQL statement prepared to be executed, including any parameter placeholder.
 *
 * @since 5.1.0
 */
public interface PreparedSql {

	/**
	 * Get the SQL string
	 * @return SQL
	 */
	String getSql();

	/**
	 * Get the parameter definitions.
	 * @return the parameter definitions in the right sequence
	 */
	List<SQLParameterDefinition> getParameters();

}
