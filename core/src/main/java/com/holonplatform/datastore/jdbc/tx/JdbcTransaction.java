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
package com.holonplatform.datastore.jdbc.tx;

import java.sql.Connection;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.datastore.transaction.TransactionStatus;
import com.holonplatform.datastore.jdbc.internal.tx.DefaultJdbcTransaction;
import com.holonplatform.datastore.jdbc.internal.tx.DelegatedJdbcTransaction;

/**
 * JDBC {@link Transaction}.
 *
 * @since 5.1.0
 */
public interface JdbcTransaction extends Transaction {

	/**
	 * Start the transaction. A {@link Connection} is obtained and configured according to the transaction
	 * configuration.
	 * @throws TransactionException If an error occurred
	 */
	void start() throws TransactionException;

	/**
	 * Finalize the transaction. The {@link Connection} bound to the transaction, if present, is released.
	 * @throws TransactionException If an error occurred
	 */
	void end() throws TransactionException;

	/**
	 * Get the JDBC connection bound to this transaction.
	 * @return The JDBC connection bound to this transaction
	 */
	Connection getConnection();

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	TransactionConfiguration getConfiguration();

	/**
	 * Create a new {@link JdbcTransaction}.
	 * @param connection Transaction {@link Connection} (not null)
	 * @param configuration Transaction configuration (not null)
	 * @return A new {@link JdbcTransaction} implementation
	 */
	static JdbcTransaction create(Connection connection, TransactionConfiguration configuration) {
		return new DefaultJdbcTransaction(connection, configuration);
	}

	/**
	 * Create a {@link JdbcTransaction} which delegates its operations and status to the given delegated transaction.
	 * <p>
	 * The delegated transaction returns <code>false</code> from the {@link TransactionStatus#isNew()} method.
	 * </p>
	 * @param delegated Delegated transaction (not null)
	 * @return A delegated {@link JdbcTransaction} implementation
	 */
	static JdbcTransaction delegate(JdbcTransaction delegated) {
		return new DelegatedJdbcTransaction(delegated);
	}

}
