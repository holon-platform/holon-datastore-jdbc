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
package com.holonplatform.datastore.jdbc.internal.tx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.transaction.AbstractTransaction;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;
import com.holonplatform.datastore.jdbc.tx.JdbcTransaction;
import com.holonplatform.datastore.jdbc.tx.JdbcTransactionLifecycleHandler;

/**
 * A JDBC {@link Transaction}.
 *
 * @since 5.1.0
 */
public class DefaultJdbcTransaction extends AbstractTransaction implements JdbcTransaction {

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	private final Connection connection;

	private final TransactionConfiguration configuration;

	private final boolean endTransactionWhenCompleted;

	private boolean wasAutoCommit;

	private boolean active;

	private final List<JdbcTransactionLifecycleHandler> handlers = new LinkedList<>();

	/**
	 * Constructor.
	 * @param connection Transaction {@link Connection} (not null)
	 * @param configuration Transaction configuration (not null)
	 * @param endTransactionWhenCompleted Whether the transaction should be finalized when completed (i.e. when the
	 *        transaction is committed or rollbacked)
	 */
	public DefaultJdbcTransaction(Connection connection, TransactionConfiguration configuration,
			boolean endTransactionWhenCompleted) {
		super();
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");
		ObjectUtils.argumentNotNull(configuration, "TransactionConfiguration must be not null");
		this.connection = connection;
		this.configuration = configuration;
		this.endTransactionWhenCompleted = endTransactionWhenCompleted;
	}

	/**
	 * Get whether the transaction should be finalized when completed (i.e. when the transaction is committed or
	 * rollbacked).
	 * @return whether the transaction should be finalized when completed
	 */
	protected boolean isEndTransactionWhenCompleted() {
		return endTransactionWhenCompleted;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.ManageableTransaction#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#getConfiguration()
	 */
	@Override
	public TransactionConfiguration getConfiguration() {
		return configuration;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.tx.JdbcTransaction#addLifecycleHandler(com.holonplatform.datastore.jdbc.tx.
	 * JdbcTransactionLifecycleHandler)
	 */
	@Override
	public void addLifecycleHandler(JdbcTransactionLifecycleHandler handler) {
		ObjectUtils.argumentNotNull(handler, "Transaction lifecycle handler must be not null");
		if (!handlers.contains(handler)) {
			handlers.add(handler);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.tx.JdbcTransaction#removeLifecycleHandler(com.holonplatform.datastore.jdbc.tx.
	 * JdbcTransactionLifecycleHandler)
	 */
	@Override
	public void removeLifecycleHandler(JdbcTransactionLifecycleHandler handler) {
		if (handler != null) {
			handlers.remove(handler);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransaction#start()
	 */
	@Override
	public synchronized void start() throws TransactionException {
		// check not already started
		if (isActive()) {
			throw new IllegalTransactionStatusException(
					"The transaction is already started and bound to connection [" + getConnection() + "]");
		}
		try {
			// disable auto-commit
			if (getConnection().getAutoCommit()) {
				wasAutoCommit = true;
				getConnection().setAutoCommit(false);
			}
		} catch (SQLException e) {
			throw new TransactionException("Failed to configure transaction connection [" + getConnection() + "]", e);
		}
		// configure connection
		if (getConfiguration().getTransactionIsolation().isPresent()) {
			try {
				getConnection().setTransactionIsolation(getConfiguration().getTransactionIsolation().get().getLevel());
			} catch (SQLException e) {
				throw new TransactionException(
						"Failed to configure connection transaction isolation level [" + getConnection() + "]", e);
			}
		}

		// set as active
		active = true;

		// fire lifecycle handlers
		handlers.forEach(handler -> handler.transactionStarted(this));

		LOGGER.debug(() -> "Jdbc transaction started");
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransaction#end()
	 */
	@Override
	public synchronized void end() throws TransactionException {

		// check active
		if (!isActive()) {
			throw new IllegalTransactionStatusException("The transaction is not active");
		}

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
				throw new TransactionException("Failed to set connection auto-commit [" + connection + "]", e);
			}
		}

		// set as not active
		active = false;

		// fire lifecycle handlers
		handlers.forEach(handler -> handler.transactionEnded(this));

		LOGGER.debug(() -> "Jdbc transaction finalized");

	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#commit()
	 */
	@Override
	public synchronized void commit() throws TransactionException {

		// check active
		if (!isActive()) {
			throw new IllegalTransactionStatusException("Cannot commit the transaction: the transaction is not active");
		}

		// check completed
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
		} catch (SQLException e) {
			throw new TransactionException("Failed to commit the transaction", e);
		}
		setCompleted();

		// check finalize
		if (isEndTransactionWhenCompleted()) {
			end();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.transaction.Transaction#rollback()
	 */
	@Override
	public synchronized void rollback() throws TransactionException {

		// check active
		if (!isActive()) {
			throw new IllegalTransactionStatusException("Cannot commit the transaction: the transaction is not active");
		}

		// check completed
		if (isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot rollback the transaction: the transaction is already completed");
		}
		try {
			getConnection().rollback();
			LOGGER.debug(() -> "Jdbc transaction rolled back");
		} catch (SQLException e) {
			throw new TransactionException("Failed to rollback the transaction", e);
		}
		setCompleted();

		// check finalize
		if (isEndTransactionWhenCompleted()) {
			end();
		}
	}

}
