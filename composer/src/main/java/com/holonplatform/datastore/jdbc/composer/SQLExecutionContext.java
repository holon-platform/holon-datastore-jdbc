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

import java.sql.Connection;
import java.util.Optional;

import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLExecutionContext;

/**
 * SQL operation execution context.
 *
 * @since 5.1.0
 */
public interface SQLExecutionContext extends SQLContext {

	/**
	 * Get the {@link Connection} used by current operation execution, if available.
	 * @return Optional current connection
	 */
	Optional<Connection> getConnection();

	/**
	 * Create a new {@link SQLExecutionContext} using given {@link SQLContext} and providing th current
	 * {@link Connection}.
	 * @param context SQL context (not null)
	 * @param connection Operation execution connection
	 * @return A new {@link SQLExecutionContext}
	 */
	static SQLExecutionContext create(SQLContext context, Connection connection) {
		return new DefaultSQLExecutionContext(context, connection);
	}

	/**
	 * Create a new {@link SQLExecutionContext} using given {@link SQLContext}.
	 * @param context SQL context (not null)
	 * @return A new {@link SQLExecutionContext}
	 */
	static SQLExecutionContext create(SQLContext context) {
		return new DefaultSQLExecutionContext(context, null);
	}

}
