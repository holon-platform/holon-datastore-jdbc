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
import java.sql.SQLException;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.transaction.AbstractTransaction;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;

/**
 * A JDBC {@link Transaction}.
 *
 * @since 5.1.0
 */
public class DefaultJdbcTransaction extends AbstractTransaction implements JdbcTransaction {

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	private final Connection connection;

	private final TransactionConfiguration configuration;

	private boolean wasAutoCommit;

	private boolean active;

	/**
	 * Constructor
	 * @param connection JDBC connection (not null)
	 * @param configuration Transaction configuration (not null)
	 */
	public DefaultJdbcTransaction(Connection connection, TransactionConfiguration configuration) {
		super();
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");
		ObjectUtils.argumentNotNull(configuration, "TransactionConfiguration must be not null");
		this.connection = connection;
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransaction#start()
	 */
	@Override
	public void start() throws TransactionException {
		// disable auto-commit
		try {
			if (getConnection().getAutoCommit()) {
				wasAutoCommit = true;
				getConnection().setAutoCommit(false);
			}
		} catch (SQLException e) {
			throw new TransactionException("Failed to set connection auto-commit", e);
		}
		// configure connection
		if (getConfiguration().getTransactionIsolation().isPresent()) {
			try {
				getConnection().setTransactionIsolation(getConfiguration().getTransactionIsolation().get().getLevel());
			} catch (SQLException e) {
				throw new TransactionException("Failed to configure connection transaction isolation level", e);
			}
		}
		active = true;
		LOGGER.debug(() -> "Jdbc transaction started");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransaction#end()
	 */
	@Override
	public void end() throws TransactionException {
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
			try {
				getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				throw new TransactionException("Failed to set connection auto-commit", e);
			}
		}
		LOGGER.debug(() -> "Jdbc transaction finalized");
	}

	/**
	 * Get the JDBC connection.
	 * @return the connection
	 */
	@Override
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	@Override
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
				LOGGER.debug(() -> "Jdbc transaction committed");
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
			LOGGER.debug(() -> "Jdbc transaction rolled back");
		} catch (Exception e) {
			throw new TransactionException("Failed to rollback the transaction", e);
		}
		setCompleted();
	}

}
