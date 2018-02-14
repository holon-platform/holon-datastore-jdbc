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
package com.holonplatform.datastore.jdbc.context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Supplier;

import com.holonplatform.core.datastore.DatastoreCommodityHandler;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.datastore.jdbc.composer.ConnectionHandler;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLStatementConfigurator;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;

/**
 * JDBC datastore operations execution context.
 *
 * @since 5.1.0
 */
public interface JdbcOperationContext extends SQLContext, ConnectionHandler, DatastoreCommodityHandler {

	/**
	 * Get the {@link IdentifierResolutionStrategy}.
	 * @return the identifier resolution strategy
	 */
	IdentifierResolutionStrategy getIdentifierResolutionStrategy();

	/**
	 * Execute given operations using a shared connection.
	 * @param <R> Operation result type
	 * @param operations Operations to execute (not null)
	 * @return Operation result
	 */
	<R> R withSharedConnection(Supplier<R> operations);

	/**
	 * Get the {@link SQLStatementConfigurator}.
	 * @return the {@link SQLStatementConfigurator}
	 */
	default SQLStatementConfigurator getStatementConfigurator() {
		return SQLStatementConfigurator.getDefault();
	}

	/**
	 * Create and configure a {@link PreparedStatement} using given {@link SQLStatement} and connection.
	 * @param statement SQL statement (not null)
	 * @param connection Connection (not null)
	 * @return The JDBC statement
	 * @throws DataAccessException If an error occurred
	 */
	PreparedStatement prepareStatement(SQLStatement statement, Connection connection);
	
	PreparedStatement prepareInsertStatement(SQLStatement statement, Connection connection, SQLPrimaryKey primaryKey);

}
