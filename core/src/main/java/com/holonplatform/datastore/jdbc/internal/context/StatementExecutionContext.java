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

import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;

/**
 * SQL statements execution context.
 * 
 * @param <C> Connection type
 * 
 * @since 5.1.0
 */
public interface StatementExecutionContext extends ExpressionResolverSupport {

	/**
	 * Get the dialect.
	 * @return The dialect.
	 */
	JdbcDialect getDialect();

	/**
	 * Prepare given SQL for execution.
	 * @param sql SQL to prepare
	 * @param context Resolution context
	 * @return Prepared SQL
	 */
	PreparedSql prepareSql(String sql, JdbcResolutionContext context);

	/**
	 * Trace given SQL if tracing is enabled.
	 * @param sql SQL to trace
	 */
	void trace(String sql);

}