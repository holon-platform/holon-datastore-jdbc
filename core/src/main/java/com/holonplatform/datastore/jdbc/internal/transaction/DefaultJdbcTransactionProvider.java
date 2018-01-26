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
package com.holonplatform.datastore.jdbc.internal.transaction;

import java.sql.Connection;

import com.holonplatform.core.datastore.transaction.Transaction.TransactionException;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;

/**
 * Default {@link JdbcTransactionProvider}.
 *
 * @since 5.1.0
 */
public enum DefaultJdbcTransactionProvider implements JdbcTransactionProvider {

	INSTANCE;

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransactionProvider#createTransaction(java.sql.
	 * Connection, com.holonplatform.core.datastore.transaction.TransactionConfiguration)
	 */
	@Override
	public JdbcTransaction createTransaction(Connection connection, TransactionConfiguration configuration)
			throws TransactionException {
		LOGGER.debug(() -> "Create a new JdbcTransaction");
		return new DefaultJdbcTransaction(connection, configuration);
	}

}
