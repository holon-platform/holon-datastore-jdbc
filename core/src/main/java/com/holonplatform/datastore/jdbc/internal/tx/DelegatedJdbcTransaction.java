/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.internal.tx;

import java.sql.Connection;

import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.datastore.transaction.AbstractDelegatedTransaction;
import com.holonplatform.datastore.jdbc.tx.JdbcTransaction;

/**
 * A {@link JdbcTransaction} which delegates its operations to another transaction.
 *
 * @since 5.2.0
 */
public class DelegatedJdbcTransaction extends AbstractDelegatedTransaction<JdbcTransaction> implements JdbcTransaction {

	public DelegatedJdbcTransaction(JdbcTransaction delegate) {
		super(delegate);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return getDelegate().getConnection();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#getConfiguration()
	 */
	@Override
	public TransactionConfiguration getConfiguration() {
		return getDelegate().getConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#commit()
	 */
	@Override
	public boolean commit() throws TransactionException {
		return getDelegate().commit();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#rollback()
	 */
	@Override
	public void rollback() throws TransactionException {
		getDelegate().rollback();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#start()
	 */
	@Override
	public void start() throws TransactionException {
		throw new IllegalTransactionStatusException("A delegated transaction should not be started");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#end()
	 */
	@Override
	public void end() throws TransactionException {
		throw new IllegalTransactionStatusException("A delegated transaction should not be ended");
	}

}
