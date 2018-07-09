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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.holonplatform.core.Expression;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.transaction.Transaction.TransactionException;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.datastore.transaction.TransactionalOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.AbstractDatastore;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.ConnectionHandler;
import com.holonplatform.datastore.jdbc.composer.ConnectionOperation;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;
import com.holonplatform.datastore.jdbc.composer.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.composer.dialect.DefaultDialect;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.resolvers.SQLContextExpressionResolver;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreExpressionResolver;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcBulkDelete;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcBulkInsert;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcBulkUpdate;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcDelete;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcInsert;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcQuery;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcRefresh;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcSave;
import com.holonplatform.datastore.jdbc.internal.operations.JdbcUpdate;
import com.holonplatform.datastore.jdbc.internal.resolvers.OperationIdentifierResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.PrimaryKeyResolver;
import com.holonplatform.datastore.jdbc.internal.support.JdbcOperationUtils;
import com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransaction;
import com.holonplatform.datastore.jdbc.internal.transaction.JdbcTransactionProvider;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.JdbcConnectionHandler;
import com.holonplatform.jdbc.JdbcConnectionHandler.ConnectionType;

/**
 * Default {@link JdbcDatastore} implementation.
 *
 * @since 5.0.0
 */
public class DefaultJdbcDatastore extends AbstractDatastore<JdbcDatastoreCommodityContext>
		implements JdbcDatastore, JdbcDatastoreCommodityContext {

	private static final long serialVersionUID = -1701596812043351551L;

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = JdbcDatastoreLogger.create();

	/**
	 * Current local {@link JdbcTransaction}
	 */
	private static final ThreadLocal<Stack<JdbcTransaction>> CURRENT_TRANSACTION = ThreadLocal
			.withInitial(() -> new Stack<>());

	/**
	 * Shared connection
	 */
	private static final ThreadLocal<Connection> SHARED_CONNECTION = new ThreadLocal<>();

	/**
	 * Data source configuration
	 */
	private DataSourceConfigProperties configuration;

	/**
	 * DataSource
	 */
	private DataSource dataSource;

	/**
	 * Connection handler
	 */
	private JdbcConnectionHandler connectionHandler = JdbcConnectionHandler.create();

	/**
	 * Transaction provider
	 */
	private JdbcTransactionProvider transactionProvider = JdbcTransactionProvider.getDefault();

	/**
	 * Database
	 */
	private DatabasePlatform database;

	/**
	 * Dialect
	 */
	protected SQLDialect dialect;

	/**
	 * Identifier resolution strategy
	 */
	private IdentifierResolutionStrategy identifierResolutionStrategy = IdentifierResolutionStrategy.AUTO;

	/**
	 * Whether to auto-initialize the Datastore at DataSource/Dialect setup
	 */
	private final boolean autoInitialize;

	/**
	 * Whether the datastore was initialized
	 */
	private boolean initialized = false;

	/**
	 * Constructor with auto initialization.
	 */
	public DefaultJdbcDatastore() {
		this(true);
	}

	/**
	 * Constructor.
	 * @param autoInitialize Whether to initialize the Datastore at DataSource/Dialect setup
	 */
	public DefaultJdbcDatastore(boolean autoInitialize) {
		super(JdbcDatastoreCommodityFactory.class, JdbcDatastoreExpressionResolver.class);
		this.autoInitialize = autoInitialize;

		// operation identifiers and primary key resolvers
		addExpressionResolver(new OperationIdentifierResolver(this));
		addExpressionResolver(new PrimaryKeyResolver(this));

		// default resolvers
		addExpressionResolvers(SQLContextExpressionResolver.getDefaultResolvers());

		// register operation commodities
		registerCommodity(JdbcRefresh.FACTORY);
		registerCommodity(JdbcInsert.FACTORY);
		registerCommodity(JdbcUpdate.FACTORY);
		registerCommodity(JdbcSave.FACTORY);
		registerCommodity(JdbcDelete.FACTORY);
		registerCommodity(JdbcBulkInsert.FACTORY);
		registerCommodity(JdbcBulkUpdate.FACTORY);
		registerCommodity(JdbcBulkDelete.FACTORY);
		registerCommodity(JdbcQuery.FACTORY);
		registerCommodity(JdbcQuery.LOCK_FACTORY);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.AbstractDatastore#getCommodityContext()
	 */
	@Override
	protected JdbcDatastoreCommodityContext getCommodityContext() throws CommodityConfigurationException {
		return this;
	}

	/**
	 * Whether to initialize the Datastore at DataSource/dialect setup.
	 * @return the autoInitialize <code>true</code> if auto-initialize is enabled
	 */
	protected boolean isAutoInitialize() {
		return autoInitialize;
	}

	/**
	 * Initialize the datastore if it is not already initialized.
	 * @param classLoader ClassLoader to use to load default factories and resolvers
	 */
	public void initialize(ClassLoader classLoader) {
		if (!initialized) {

			DatabaseMetadataPlatform initData = null;

			// auto detect platform if not setted
			if (getDatabase().orElse(DatabasePlatform.NONE) == DatabasePlatform.NONE) {
				try {
					initData = withConnection(ConnectionType.INIT, c -> {
						final DatabaseMetaData databaseMetaData = c.getMetaData();
						DatabaseMetadataPlatform dmp = new DatabaseMetadataPlatform();
						dmp.metadata = databaseMetaData;
						dmp.platform = DatabasePlatform.fromUrl(databaseMetaData.getURL());
						return dmp;
					});
				} catch (Exception e) {
					LOGGER.warn("Failed to inspect database metadata", e);
				}

				if (initData != null && initData.platform != null && initData.platform != DatabasePlatform.NONE) {
					// set database platform if detected
					setDatabase(initData.platform);
				}
			}

			// init dialect
			final SQLDialect dialect = getDialect(false);
			LOGGER.debug(() -> "Datastore JDBC dialect: [" + ((dialect != null) ? dialect.getClass().getName() : null)
					+ "]");
			try {
				dialect.init(new JdbcDatastoreDialectContext((initData != null) ? initData.metadata : null));
			} catch (SQLException e) {
				throw new IllegalStateException("Cannot initialize dialect [" + dialect.getClass().getName() + "]", e);
			}

			// default factories and resolvers
			loadExpressionResolvers(classLoader);
			loadCommodityFactories(classLoader);

			initialized = true;
		}
	}

	/**
	 * Checks whether to auto-initialize the Datastore, if {@link #isAutoInitialize()} is <code>true</code> and the
	 * Datastore wasn't already initialized.
	 */
	protected void checkInitialize() {
		if (isAutoInitialize()) {
			initialize(ClassUtils.getDefaultClassLoader());
		}
	}

	/**
	 * Get the {@link JdbcTransactionProvider} to use to create a new JDBC transaction.
	 * @return the transaction provider
	 */
	protected JdbcTransactionProvider getTransactionProvider() {
		return transactionProvider;
	}

	/**
	 * Set the {@link JdbcTransactionProvider} to use to create a new JDBC transaction.
	 * @param transactionProvider the transaction provider to set (not null)
	 */
	public void setTransactionProvider(JdbcTransactionProvider transactionProvider) {
		ObjectUtils.argumentNotNull(transactionProvider, "JdbcTransactionProvider must be not null");
		this.transactionProvider = transactionProvider;
	}

	/**
	 * Get the data source configuration properties.
	 * @return Optional data source configuration properties
	 */
	public Optional<DataSourceConfigProperties> getConfiguration() {
		return Optional.ofNullable(configuration);
	}

	/**
	 * Set the data source configuration properties.
	 * @param configuration the data source configuration properties to set (not null)
	 * @param buildDataSource Whether to build and set a {@link DataSource} using given configuration properties.
	 */
	public void setConfiguration(DataSourceConfigProperties configuration, boolean buildDataSource) {
		ObjectUtils.argumentNotNull(configuration, "DataSource configuration must be not null");
		this.configuration = configuration;
		if (!getDatabase().isPresent()) {
			setDatabase(configuration.getDatabasePlatform());
		}
		if (buildDataSource) {
			setDataSource(DataSourceBuilder.create().build(configuration));
		}
	}

	/**
	 * Set the {@link DataSource} to be used by this datastore to perform database operations.
	 * @param dataSource the {@link DataSource} to set (not null)
	 */
	public void setDataSource(DataSource dataSource) {
		ObjectUtils.argumentNotNull(dataSource, "DataSource must be not null");
		this.dataSource = dataSource;
		// initialization
		checkInitialize();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext#getDataSource()
	 */
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Get the {@link JdbcConnectionHandler} which is used for Datastore JDBC connections handling.
	 * @return the connection handler
	 */
	protected JdbcConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	/**
	 * Set the {@link JdbcConnectionHandler} to be used for Datastore JDBC connections handling.
	 * @param connectionHandler The connection handler to set (not null)
	 */
	public void setConnectionHandler(JdbcConnectionHandler connectionHandler) {
		ObjectUtils.argumentNotNull(connectionHandler, "JdbcConnectionHandler must be not null");
		this.connectionHandler = connectionHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ConfigurableJdbcDatastore#getDatabase()
	 */
	@Override
	public Optional<DatabasePlatform> getDatabase() {
		return Optional.ofNullable(database);
	}

	/**
	 * Set the database platform
	 * @param database the database platform to set
	 */
	public void setDatabase(DatabasePlatform database) {
		ObjectUtils.argumentNotNull(database, "Database platform must be not null");
		this.database = database;
		LOGGER.debug(() -> "Set database platform [" + database.name() + "]");
		// try to setup a suitable dialect
		if (dialect == null) {
			SQLDialect.detect(database).ifPresent(d -> setDialect(d));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#getDialect()
	 */
	@Override
	public SQLDialect getDialect() {
		return getDialect(true);
	}

	/**
	 * Set the SQL dialect
	 * @param dialect the dialect to set
	 */
	public void setDialect(SQLDialect dialect) {
		this.dialect = dialect;

		if (dialect != null) {
			LOGGER.debug(() -> "Set dialect [" + dialect.getClass().getName() + "]");
		}

		// check init
		if (dataSource != null) {
			checkInitialize();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.context.JdbcExecutionContext#getIdentifierResolutionStrategy()
	 */
	@Override
	public IdentifierResolutionStrategy getIdentifierResolutionStrategy() {
		return identifierResolutionStrategy;
	}

	/**
	 * Set the {@link IdentifierResolutionStrategy}.
	 * @param identifierResolutionStrategy the identifier resolution strategy to set (not null)
	 */
	public void setIdentifierResolutionStrategy(IdentifierResolutionStrategy identifierResolutionStrategy) {
		ObjectUtils.argumentNotNull(identifierResolutionStrategy, "IdentifierResolutionStrategy must be not null");
		this.identifierResolutionStrategy = identifierResolutionStrategy;
	}

	/**
	 * Get the JDBC dialect of the datastore.
	 * @param checkInitialize <code>true</code> to check if datastore is initialized
	 * @return Datastore dialect
	 */
	protected SQLDialect getDialect(boolean checkInitialize) {
		if (dialect == null) {
			dialect = new DefaultDialect();
		}

		if (checkInitialize) {
			checkInitialize();
		}

		return dialect;
	}

	/**
	 * Execute given <code>operation</code> with a JDBC {@link Connection} handled by current
	 * {@link JdbcConnectionHandler} and return the operation result.
	 * @param connectionType The connection type (not null)
	 * @param operation The operation to execute (not null)
	 * @return Operation result
	 */
	@SuppressWarnings("resource")
	protected <R> R withConnection(ConnectionType connectionType, ConnectionOperation<R> operation) {
		ObjectUtils.argumentNotNull(operation, "Operation must be not null");

		Connection connection = null;
		try {
			// check shared connection
			if (SHARED_CONNECTION.get() != null) {
				return operation.execute(SHARED_CONNECTION.get());
			}

			// if a transaction is active, use current transaction connection
			JdbcTransaction tx = getCurrentTransaction().orElse(null);
			if (tx != null) {
				return operation.execute(tx.getConnection());
			}

			// get a connection from connection handler
			return operation.execute(connection = obtainConnection(connectionType));

		} catch (DataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new DataAccessException("Failed to execute operation", e);
		} finally {
			// check active transaction: avoid connection release if present
			if (connection != null) {
				// release connection
				try {
					releaseConnection(connection, connectionType);
				} catch (SQLException e) {
					throw new DataAccessException("Failed to release the connection", e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext#withSharedConnection(java.util.
	 * function.Supplier)
	 */
	@Override
	public <R> R withSharedConnection(Supplier<R> operations) {
		ObjectUtils.argumentNotNull(operations, "Operations must be not null");

		if (SHARED_CONNECTION.get() != null) {
			return operations.get();
		}

		return withConnection(connection -> {
			try {
				SHARED_CONNECTION.set(connection);
				return operations.get();
			} finally {
				SHARED_CONNECTION.remove();
			}
		});
	}

	/**
	 * Obtain a new {@link Connection} using current {@link JdbcConnectionHandler}.
	 * @param connectionType Connection type
	 * @return The connection
	 * @throws SQLException If an error occurred
	 */
	private Connection obtainConnection(ConnectionType connectionType) throws SQLException {
		// check DataSource
		final DataSource dataSource = getDataSource();
		if (dataSource == null) {
			throw new IllegalStateException("A DataSource is not available. Check Datastore configuration.");
		}
		// get connection from handler
		Connection connection = getConnectionHandler().getConnection(dataSource, connectionType);
		if (connection == null) {
			throw new IllegalStateException(
					"The connection handler [" + getConnectionHandler() + "] returned a null connection");
		}
		return connection;
	}

	/**
	 * Release (finalize) given {@link Connection} using current {@link JdbcConnectionHandler}.
	 * @param connection The connection to release
	 * @param connectionType Connection type
	 * @throws SQLException If an error occurred
	 */
	private void releaseConnection(Connection connection, ConnectionType connectionType) throws SQLException {
		if (connection != null) {
			getConnectionHandler().releaseConnection(connection, getDataSource(), connectionType);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.JdbcDatastore#withConnection(com.holonplatform.datastore.jdbc.JdbcDatastore.
	 * ConnectionOperation)
	 */
	@Override
	public <R> R withConnection(ConnectionOperation<R> operation) {
		return withConnection(ConnectionType.DEFAULT, operation);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDatastore#withTransaction(com.holonplatform.core.datastore.transaction.
	 * TransactionalOperation, com.holonplatform.core.datastore.transaction.TransactionConfiguration)
	 */
	@Override
	public <R> R withTransaction(TransactionalOperation<R> operation,
			TransactionConfiguration transactionConfiguration) {
		ObjectUtils.argumentNotNull(operation, "TransactionalOperation must be not null");

		final JdbcTransaction tx = beginTransaction(transactionConfiguration);

		try {
			// execute operation
			return operation.execute(tx);
		} catch (Exception e) {
			// check rollback transaction
			if (tx.getConfiguration().isRollbackOnError()) {
				tx.setRollbackOnly();
			}
			if (e instanceof DataAccessException) {
				throw (DataAccessException) e;
			}
			throw new DataAccessException("Failed to execute operation", e);
		} finally {
			try {
				finalizeTransaction();
			} catch (Exception e) {
				throw new DataAccessException("Failed to finalize transaction", e);
			}
		}

	}

	/**
	 * Get the current transaction, if active.
	 * @return Optional current transaction
	 */
	private static Optional<JdbcTransaction> getCurrentTransaction() {
		return (CURRENT_TRANSACTION.get().isEmpty()) ? Optional.empty() : Optional.of(CURRENT_TRANSACTION.get().peek());
	}

	/**
	 * If a transaction is active, remove the transaction from current trasactions stack and return the transaction
	 * itself.
	 * @return The removed current transaction, if it was present
	 */
	private static Optional<JdbcTransaction> removeCurrentTransaction() {
		return (CURRENT_TRANSACTION.get().isEmpty()) ? Optional.empty() : Optional.of(CURRENT_TRANSACTION.get().pop());
	}

	/**
	 * Start a new transaction.
	 * @param configuration Transaction configuration
	 * @return The current transaction or a new one if no transaction is active
	 * @throws TransactionException Error starting a new transaction
	 */
	private JdbcTransaction beginTransaction(TransactionConfiguration configuration) throws TransactionException {
		try {
			// create a new transaction
			JdbcTransaction tx = createTransaction(obtainConnection(ConnectionType.DEFAULT),
					(configuration != null) ? configuration : TransactionConfiguration.getDefault());
			// start transaction
			tx.start();
			// stack transaction
			return CURRENT_TRANSACTION.get().push(tx);
		} catch (Exception e) {
			throw new TransactionException("Failed to start a transaction", e);
		}
	}

	/**
	 * Build a new {@link JdbcTransaction} using current {@link JdbcTransactionProvider}.
	 * @param connection The connection to use (not null)
	 * @param configuration Configuration (not null)
	 * @return A new {@link JdbcTransaction}
	 * @throws TransactionException If an error occurred
	 */
	protected JdbcTransaction createTransaction(Connection connection, TransactionConfiguration configuration) {
		return getTransactionProvider().createTransaction(connection, configuration);
	}

	/**
	 * Finalize current transaction, if present.
	 * @return <code>true</code> if a transaction was active and has been finalized
	 * @throws TransactionException Error during transaction finalization
	 */
	private boolean finalizeTransaction() throws TransactionException {
		return removeCurrentTransaction().map(tx -> {
			try {
				// finalize transaction
				tx.end();
				return true;
			} catch (Exception e) {
				throw new TransactionException("Failed to finalize transaction", e);
			} finally {
				// close connection
				try {
					releaseConnection(tx.getConnection(), ConnectionType.DEFAULT);
				} catch (SQLException e) {
					throw new TransactionException("Failed to release the connection", e);
				}
			}
		}).orElse(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.context.JdbcExecutionContext#prepareStatement(com.holonplatform.datastore.jdbc.
	 * composer.expression.SQLStatement, java.sql.Connection)
	 */
	@Override
	public PreparedStatement prepareStatement(SQLStatement statement, Connection connection) {
		ObjectUtils.argumentNotNull(statement, "SQLStatement must be not null");
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");

		try {
			PreparedStatement stmt = connection.prepareStatement(statement.getSql());

			// configure
			getStatementConfigurator().configureStatement(this, stmt, statement);

			return stmt;
		} catch (SQLException e) {
			throw new DataAccessException("Failed to prepare JDBC statement for statement [" + statement + "]", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.context.JdbcOperationContext#prepareInsertStatement(com.holonplatform.datastore.
	 * jdbc.composer.expression.SQLStatement, java.sql.Connection,
	 * com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey)
	 */
	@SuppressWarnings("resource")
	@Override
	public PreparedStatement prepareInsertStatement(SQLStatement statement, Connection connection,
			SQLPrimaryKey primaryKey) {
		ObjectUtils.argumentNotNull(statement, "SQLStatement must be not null");
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");

		try {
			PreparedStatement stmt;

			String[] pkNames = null;
			if (primaryKey != null && primaryKey.getPaths() != null && primaryKey.getPaths().length > 0
					&& getDialect().supportsGetGeneratedKeys()) {
				pkNames = new String[primaryKey.getPaths().length];
				for (int i = 0; i < primaryKey.getPaths().length; i++) {
					pkNames[i] = getDialect().getColumnName(JdbcOperationUtils.getPathName(primaryKey.getPaths()[i]));
				}

				if (getDialect().supportGetGeneratedKeyByName()) {
					stmt = connection.prepareStatement(statement.getSql(), pkNames);
				} else {
					stmt = connection.prepareStatement(statement.getSql(), Statement.RETURN_GENERATED_KEYS);
				}
			} else {
				stmt = connection.prepareStatement(statement.getSql());
			}

			// configure
			getStatementConfigurator().configureStatement(this, stmt, statement);

			return stmt;
		} catch (SQLException e) {
			throw new DataAccessException("Failed to prepare JDBC statement for statement [" + statement + "]", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.context.StatementExecutionContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String sql) {
		if (isTraceEnabled()) {
			LOGGER.info("(TRACE) SQL: [" + sql + "]");
		} else {
			LOGGER.debug(() -> "SQL: [" + sql + "]");
		}
	}

	// ------- Dialect context

	class JdbcDatastoreDialectContext implements SQLDialectContext {

		private final DatabaseMetaData databaseMetaData;

		public JdbcDatastoreDialectContext(DatabaseMetaData databaseMetaData) {
			super();
			this.databaseMetaData = databaseMetaData;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#addExpressionResolver(com.holonplatform.
		 * core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void addExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			DefaultJdbcDatastore.this.addExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport#removeExpressionResolver(com.
		 * holonplatform.core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> void removeExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			DefaultJdbcDatastore.this.removeExpressionResolver(expressionResolver);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.composer.SQLDialectContext#getValueSerializer()
		 */
		@Override
		public SQLValueSerializer getValueSerializer() {
			return DefaultJdbcDatastore.this.getValueSerializer();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.composer.SQLDialectContext#getValueDeserializer()
		 */
		@Override
		public SQLValueDeserializer getValueDeserializer() {
			return DefaultJdbcDatastore.this.getValueDeserializer();
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.composer.SQLDialectContext#getDatabaseMetaData()
		 */
		@Override
		public Optional<DatabaseMetaData> getDatabaseMetaData() {
			return Optional.ofNullable(databaseMetaData);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.composer.SQLDialectContext#getConnectionProvider()
		 */
		@Override
		public Optional<ConnectionHandler> getConnectionProvider() {
			return Optional.of(new ConnectionHandler() {

				@Override
				public <R> R withConnection(ConnectionOperation<R> operation) {
					return DefaultJdbcDatastore.this.withConnection(ConnectionType.INIT, operation);
				}
			});
		}

	}

	private class DatabaseMetadataPlatform {

		DatabaseMetaData metadata;

		DatabasePlatform platform;

	}

	// ------- Builder

	/**
	 * Base {@link JdbcDatastore} builder.
	 *
	 * @param <D> Concrete JdbcDatastore type
	 * @param <I> Concrete datastore instance
	 */
	public static abstract class AbstractBuilder<D extends JdbcDatastore, I extends DefaultJdbcDatastore>
			implements JdbcDatastore.Builder<D> {

		/**
		 * Datastore instance
		 */
		protected final I datastore;

		public AbstractBuilder(I datastore) {
			super();
			this.datastore = datastore;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#dataContextId(java.lang.String)
		 */
		@Override
		public JdbcDatastore.Builder<D> dataContextId(String dataContextId) {
			datastore.setDataContextId(dataContextId);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#traceEnabled(boolean)
		 */
		@Override
		public JdbcDatastore.Builder<D> traceEnabled(boolean trace) {
			datastore.setTraceEnabled(trace);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.ExpressionResolver.ExpressionResolverBuilder#withExpressionResolver(com.holonplatform.
		 * core.ExpressionResolver)
		 */
		@Override
		public <E extends Expression, R extends Expression> JdbcDatastore.Builder<D> withExpressionResolver(
				ExpressionResolver<E, R> expressionResolver) {
			datastore.addExpressionResolver(expressionResolver);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#dataSource(javax.sql.DataSource)
		 */
		@Override
		public JdbcDatastore.Builder<D> dataSource(DataSource dataSource) {
			ObjectUtils.argumentNotNull(dataSource, "DataSource must be not null");
			datastore.setDataSource(dataSource);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#dataSource(com.holonplatform.jdbc.
		 * DataSourceConfigProperties)
		 */
		@Override
		public JdbcDatastore.Builder<D> dataSource(DataSourceConfigProperties configuration) {
			ObjectUtils.argumentNotNull(configuration, "DataSource configuration must be not null");
			datastore.setConfiguration(configuration, datastore.getDataSource() == null);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#database(com.holonplatform.jdbc.DatabasePlatform)
		 */
		@Override
		public JdbcDatastore.Builder<D> database(DatabasePlatform database) {
			datastore.setDatabase(database);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#dialect(com.holonplatform.datastore.jdbc.JdbcDialect)
		 */
		@Override
		public JdbcDatastore.Builder<D> dialect(SQLDialect dialect) {
			ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
			datastore.setDialect(dialect);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#dialect(java.lang.String)
		 */
		@Override
		public JdbcDatastore.Builder<D> dialect(String dialectClassName) {
			ObjectUtils.argumentNotNull(dialectClassName, "Dialect class name must be not null");
			try {
				datastore.setDialect((SQLDialect) Class.forName(dialectClassName).newInstance());
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to istantiate dialect class [" + dialectClassName + "]", e);
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#autoCommit(boolean)
		 */
		@Deprecated
		@Override
		public JdbcDatastore.Builder<D> autoCommit(boolean autoCommit) {
			// noop
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#connectionHandler(com.holonplatform.datastore.jdbc.
		 * JdbcConnectionHandler)
		 */
		@Override
		public JdbcDatastore.Builder<D> connectionHandler(JdbcConnectionHandler connectionHandler) {
			datastore.setConnectionHandler(connectionHandler);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#identifierResolutionStrategy(com.holonplatform.
		 * datastore.jdbc.config.IdentifierResolutionStrategy)
		 */
		@Override
		public JdbcDatastore.Builder<D> identifierResolutionStrategy(
				IdentifierResolutionStrategy identifierResolutionStrategy) {
			datastore.setIdentifierResolutionStrategy(identifierResolutionStrategy);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#registerCommodity(com.holonplatform.datastore.jdbc.
		 * config.JdbcDatastoreCommodityFactory)
		 */
		@Override
		public <C extends DatastoreCommodity> JdbcDatastore.Builder<D> withCommodity(
				JdbcDatastoreCommodityFactory<C> commodityFactory) {
			datastore.registerCommodity(commodityFactory);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#configuration(com.holonplatform.core.datastore.
		 * DatastoreConfigProperties)
		 */
		@Override
		public JdbcDatastore.Builder<D> configuration(DatastoreConfigProperties configuration) {
			ObjectUtils.argumentNotNull(configuration, "Datastore configuration must be not null");
			datastore.setTraceEnabled(configuration.isTrace());
			String dialect = configuration.getDialect();
			if (dialect != null) {
				return dialect(dialect);
			}
			return this;
		}

	}

	/**
	 * Default {@link JdbcDatastore} builder.
	 */
	public static class DefaultBuilder extends AbstractBuilder<JdbcDatastore, DefaultJdbcDatastore> {

		/**
		 * Constructor
		 */
		public DefaultBuilder() {
			super(new DefaultJdbcDatastore(false));
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.Datastore.Builder#build()
		 */
		@Override
		public JdbcDatastore build() {
			datastore.initialize(ClassUtils.getDefaultClassLoader());
			return datastore;
		}

	}

}
