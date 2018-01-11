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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holonplatform.datastore.jdbc.JdbcDatastore.ConnectionOperation;

/**
 * JDBC {@link StatementExecutionContext}.
 *
 * @since 5.1.0
 */
public interface JdbcStatementExecutionContext extends StatementExecutionContext {

	/**
	 * Get the {@link SQLStatementConfigurator}.
	 * @return the statement configurator
	 */
	SQLStatementConfigurator<PreparedStatement> getStatementConfigurator();

	/**
	 * Create a {@link PreparedStatement} using given prepared sql and set any parameter value using the
	 * {@link SQLStatementConfigurator}.
	 * @param connection Connection
	 * @param sql Prepared sql
	 * @return The configured {@link PreparedStatement}
	 * @throws SQLException If an error occurred
	 */
	PreparedStatement createStatement(Connection connection, PreparedSql sql) throws SQLException;

	/**
	 * Execute given operation with a Datastore managed connection.
	 * @param <R> Operation result type
	 * @param operation Operation to execute
	 * @return Operation result
	 */
	<R> R withConnection(ConnectionOperation<R> operation);

}
