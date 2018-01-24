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
package com.holonplatform.datastore.jdbc.internal;

import java.sql.Connection;
import java.sql.SQLException;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.datastore.transaction.AbstractTransaction;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * A JDBC {@link Transaction}.
 *
 * @since 5.1.0
 */
public class JdbcTransaction extends AbstractTransaction {

	private final Connection connection;

	private final TransactionConfiguration configuration;

	private boolean wasAutoCommit;

	private boolean active;

	/**
	 * Constructor
	 * @param connection JDBC connection (not null)
	 * @param configuration Transaction configuration (not null)
	 */
	public JdbcTransaction(Connection connection, TransactionConfiguration configuration) {
		super();
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");
		ObjectUtils.argumentNotNull(configuration, "TransactionConfiguration must be not null");
		this.connection = connection;
		this.configuration = configuration;
	}

	/**
	 * Start the transaction, configuring the connection.
	 * @throws SQLException If an error occurred
	 */
	public void start() throws SQLException {
		// disable auto-commit
		if (getConnection().getAutoCommit()) {
			wasAutoCommit = true;
			getConnection().setAutoCommit(false);
		}
		// configure connection
		if (getConfiguration().getTransactionIsolation().isPresent()) {
			getConnection().setTransactionIsolation(getConfiguration().getTransactionIsolation().get().getLevel());
		}
		active = true;
	}

	/**
	 * Finalize the transaction.
	 * @throws SQLException If an error occurred
	 */
	public void end() throws SQLException {
		// check completed
		if (!isCompleted()) {
			if (isRollbackOnly()) {
				rollback();
			} else {
				if (getConfiguration().isAutoCommit()) {
					commit();
				}
			}
		}
		// restore auto-commit
		if (wasAutoCommit) {
			getConnection().setAutoCommit(true);
		}
	}

	/**
	 * Get the JDBC connection.
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	public TransactionConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Get whether the transaction is active.
	 * @return whether the transaction is active
	 */
	protected boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#commit()
	 */
	@Override
	public void commit() throws TransactionException {
		if (!isActive()) {
			throw new IllegalTransactionStatusException("Cannot commit the transaction: the transaction is not active");
		}
		if (isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot commit the transaction: the transaction is already completed");
		}
		try {
			// check rollback only
			if (isRollbackOnly()) {
				rollback();
			} else {
				getConnection().commit();
			}
		} catch (Exception e) {
			throw new TransactionException("Failed to commit the transaction", e);
		}
		setCompleted();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#rollback()
	 */
	@Override
	public void rollback() throws TransactionException {
		if (!isActive()) {
			throw new IllegalTransactionStatusException(
					"Cannot rollback the transaction: the transaction is not active");
		}
		if (isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot rollback the transaction: the transaction is already completed");
		}
		try {
			getConnection().rollback();
		} catch (Exception e) {
			throw new TransactionException("Failed to commit the transaction", e);
		}
		setCompleted();
	}

}
