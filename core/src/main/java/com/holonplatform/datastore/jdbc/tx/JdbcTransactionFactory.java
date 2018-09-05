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

import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.datastore.transaction.TransactionStatus.TransactionException;

/**
 * Factory to create and configure new {@link JdbcTransaction} implementation using a {@link Connection} and a
 * {@link TransactionConfiguration} definition.
 *
 * @since 5.1.0
 */
@FunctionalInterface
public interface JdbcTransactionFactory {

	/**
	 * Build a new {@link JdbcTransaction}.
	 * @param connection Transaction {@link Connection} (not null)
	 * @param configuration Transaction configuration (not null)
	 * @return A new {@link JdbcTransaction} (not null)
	 * @throws TransactionException If an error occurred
	 */
	JdbcTransaction createTransaction(Connection connection, TransactionConfiguration configuration)
			throws TransactionException;

	/**
	 * Get the default {@link JdbcTransactionFactory}.
	 * @return the default {@link JdbcTransactionFactory}
	 */
	static JdbcTransactionFactory getDefault() {
		return (connection, configuration) -> JdbcTransaction.create(connection, configuration);
	}

}
