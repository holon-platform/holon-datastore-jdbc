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
 * Default {@link PreparedSql} implementation.
 * 
 * @since 5.1.0
 */
public class DefaultPreparedSql implements PreparedSql {

	private final String sql;
	private final List<SQLParameterDefinition> parameters;

	public DefaultPreparedSql(String sql, List<SQLParameterDefinition> parameters) {
		super();
		this.sql = sql;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.context.PreparedSql#getSql()
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.context.PreparedSql#getParameters()
	 */
	@Override
	public List<SQLParameterDefinition> getParameters() {
		return parameters;
	}

}