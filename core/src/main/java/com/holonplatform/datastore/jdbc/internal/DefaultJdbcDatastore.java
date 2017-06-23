/*
 * Copyright 2000-2016 Holon TDCN.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.AbstractDatastore;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QueryResults.QueryExecutionException;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreExpressionResolver;
import com.holonplatform.datastore.jdbc.dialect.DB2Dialect;
import com.holonplatform.datastore.jdbc.dialect.DefaultDialect;
import com.holonplatform.datastore.jdbc.dialect.DerbyDialect;
import com.holonplatform.datastore.jdbc.dialect.H2Dialect;
import com.holonplatform.datastore.jdbc.dialect.HANADialect;
import com.holonplatform.datastore.jdbc.dialect.HSQLDialect;
import com.holonplatform.datastore.jdbc.dialect.InformixDialect;
import com.holonplatform.datastore.jdbc.dialect.MariaDBDialect;
import com.holonplatform.datastore.jdbc.dialect.MySQLDialect;
import com.holonplatform.datastore.jdbc.dialect.OracleDialect;
import com.holonplatform.datastore.jdbc.dialect.PostgreSQLDialect;
import com.holonplatform.datastore.jdbc.dialect.SQLServerDialect;
import com.holonplatform.datastore.jdbc.dialect.SQLiteDialect;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure;
import com.holonplatform.datastore.jdbc.internal.expressions.SQLToken;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;
import com.holonplatform.datastore.jdbc.internal.pk.PrimaryKeyInspector;
import com.holonplatform.datastore.jdbc.internal.pk.PrimaryKeysCache;
import com.holonplatform.datastore.jdbc.internal.resolvers.ConstantExpressionResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.DataTargetResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.ExistFilterResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.LiteralValueResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.NotExistFilterResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.OperationStructureResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.OrderBySortResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.PathFunctionResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.PathResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.PrimaryKeyResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.PropertyConstantExpressionResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.QueryAggregationResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.QueryFilterResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.QueryProjectionResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.QuerySortResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.QueryStructureResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.RelationalTargetResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.SubQueryResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.VisitableQueryFilterResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.VisitableQueryProjectionResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.VisitableQuerySortResolver;
import com.holonplatform.datastore.jdbc.internal.resolvers.WhereFilterResolver;
import com.holonplatform.datastore.jdbc.internal.support.PreparedSql;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;

/**
 * Default {@link JdbcDatastore} implementation.
 *
 * @since 5.0.0
 */
public class DefaultJdbcDatastore extends AbstractDatastore<JdbcDatastoreCommodityContext>
		implements JdbcDatastoreCommodityContext, PrimaryKeyInspector {

	private static final long serialVersionUID = -1701596812043351551L;

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = JdbcDatastoreLogger.create();

	/**
	 * Data source configuration
	 */
	private DataSourceConfigProperties configuration;

	/**
	 * DataSource
	 */
	private DataSource dataSource;

	/**
	 * Database
	 */
	private DatabasePlatform database;

	/**
	 * Dialect
	 */
	protected JdbcDialect dialect;

	/**
	 * Auto-commit
	 */
	private boolean autoCommit = true;

	/**
	 * Whether to auto-initialize the Datastore at DataSource/Dialect setup
	 */
	private final boolean autoInitialize;

	/**
	 * Whether the datastore was initialized
	 */
	private boolean initialized = false;

	/**
	 * Primary keys cache
	 */
	private PrimaryKeysCache primaryKeysCache = PrimaryKeysCache.create();

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

		// register default resolvers
		addExpressionResolver(new PrimaryKeyResolver(primaryKeysCache, this));
		addExpressionResolver(RelationalTargetResolver.INSTANCE);
		addExpressionResolver(DataTargetResolver.INSTANCE);
		addExpressionResolver(PathFunctionResolver.INSTANCE);
		addExpressionResolver(PathResolver.INSTANCE);
		addExpressionResolver(ConstantExpressionResolver.INSTANCE);
		addExpressionResolver(PropertyConstantExpressionResolver.INSTANCE);
		addExpressionResolver(LiteralValueResolver.INSTANCE);
		addExpressionResolver(SubQueryResolver.INSTANCE);
		addExpressionResolver(ExistFilterResolver.INSTANCE);
		addExpressionResolver(NotExistFilterResolver.INSTANCE);
		addExpressionResolver(WhereFilterResolver.INSTANCE);
		addExpressionResolver(OrderBySortResolver.INSTANCE);
		addExpressionResolver(VisitableQueryFilterResolver.INSTANCE);
		addExpressionResolver(VisitableQuerySortResolver.INSTANCE);
		addExpressionResolver(QueryFilterResolver.INSTANCE);
		addExpressionResolver(QuerySortResolver.INSTANCE);
		addExpressionResolver(VisitableQueryProjectionResolver.INSTANCE);
		addExpressionResolver(QueryProjectionResolver.INSTANCE);
		addExpressionResolver(QueryAggregationResolver.INSTANCE);
		addExpressionResolver(QueryStructureResolver.INSTANCE);
		addExpressionResolver(OperationStructureResolver.INSTANCE);

		// Query commodity factory
		registerCommodity(new JdbcQueryFactory());
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
	 * @throws IllegalStateException If initialization fails
	 */
	public void initialize(ClassLoader classLoader) throws IllegalStateException {
		if (!initialized) {
			if (getDataSource() == null) {
				throw new IllegalStateException("Missing DataSource");
			}

			// auto detect platform if not setted
			if (getDatabase().orElse(DatabasePlatform.NONE) == DatabasePlatform.NONE) {
				// get from metadata
				DatabasePlatform platform = withConnection(c -> {
					try {
						DatabaseMetaData dbmd = c.getMetaData();
						if (dbmd != null) {
							String url = dbmd.getURL();
							if (url != null) {
								return DatabasePlatform.fromUrl(url);
							}
						}
					} catch (Exception e) {
						LOGGER.warn("Failed to inspect database metadata", e);
					}
					return null;
				});
				if (platform != DatabasePlatform.NONE) {
					setDatabase(platform);
				}
			}

			// init dialect
			final JdbcDialect dialect = getDialect(false);
			LOGGER.debug(() -> "Datastore JDBC dialect: [" + ((dialect != null) ? dialect.getClass().getName() : null)
					+ "]");
			try {
				dialect.init(this);
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
		this.primaryKeysCache.clear();
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
		this.database = database;
		LOGGER.debug(() -> "Set database platform [" + database.name() + "]");
		// try to setup a suitable dialect
		if (dialect == null) {
			getDialectForPlatform(database).ifPresent(d -> setDialect(d));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ConfigurableJdbcDatastore#getDialect()
	 */
	@Override
	public JdbcDialect getDialect() {
		return getDialect(true);
	}

	/**
	 * Set the SQL dialect
	 * @param dialect the dialect to set
	 */
	public void setDialect(JdbcDialect dialect) {
		this.dialect = dialect;
		this.primaryKeysCache.clear();

		if (dialect != null) {
			LOGGER.debug(() -> "Set dialect [" + dialect.getClass().getName() + "]");
		}

		// check init
		if (dataSource != null) {
			checkInitialize();
		}
	}

	/**
	 * Get the JDBC dialect of the datastore.
	 * @param checkInitialize <code>true</code> to check if datastore is initialized
	 * @return Datastore dialect
	 */
	protected JdbcDialect getDialect(boolean checkInitialize) {
		if (dialect == null) {
			dialect = new DefaultDialect();
		}

		if (checkInitialize) {
			checkInitialize();
		}

		return dialect;
	}

	/**
	 * Get whether the auto-commit mode has to be setted for connections.
	 * @return the autoCommit Whether the connections auto-commit mode is enabled
	 */
	public boolean isAutoCommit() {
		return autoCommit;
	}

	/**
	 * Set whether the auto-commit mode has to be setted for connections.
	 * @param autoCommit Whether to set connections auto-commit
	 */
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("resource")
	@Override
	public <R> R withConnection(ConnectionOperation<R> operation) {
		ObjectUtils.argumentNotNull(operation, "Operation must be not null");
		final DataSource dataSource = getDataSource();
		if (dataSource == null) {
			throw new IllegalStateException("A DataSource is not available. Check Datastore configuration.");
		}
		Connection connection = null;
		try {
			// get connection
			connection = getConnection(dataSource);
			// configure
			configureConnection(connection);
			// execute operation
			return operation.execute(connection);
		} catch (Exception e) {
			throw new DataAccessException("Failed to execute operation", e);
		} finally {
			// release connection
			if (connection != null) {
				try {
					releaseConnection(connection, dataSource);
				} catch (SQLException e) {
					LOGGER.warn("Failed to release the connection", e);
				}
			}
		}
	}

	/**
	 * Obtain a {@link Connection} from given <code>dataSource</code>.
	 * @param dataSource {@link DataSource} from which to obtain the connection
	 * @return A new {@link Connection}
	 * @throws SQLException If an error occurred
	 */
	protected Connection getConnection(DataSource dataSource) throws SQLException {
		Connection connection = dataSource.getConnection();
		LOGGER.debug(() -> "Obtained a DataSource connection: [" + connection + "]");
		return connection;
	}

	/**
	 * Configure a {@link Connection} obtained form the {@link DataSource}.
	 * @param connection Connection to configure
	 * @throws SQLException If an error occurred
	 */
	protected void configureConnection(Connection connection) throws SQLException {
		LOGGER.debug(() -> "Configuring connection: [" + connection + "] autocommit: [" + isAutoCommit() + "]");
		connection.setAutoCommit(isAutoCommit());
	}

	/**
	 * Release the given <code>connection</code>.
	 * @param connection Connection to release
	 * @param dataSource The {@link DataSource} from which the connection was obtained
	 * @throws SQLException If an error occurred
	 */
	protected void releaseConnection(Connection connection, DataSource dataSource) throws SQLException {
		if (connection != null) {
			LOGGER.debug(() -> "Closing connection: [" + connection + "]");
			connection.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDatastore#getPrimaryKey(java.lang.String)
	 */
	@Override
	public Optional<Path<?>[]> getPrimaryKey(String tableName) throws SQLException {
		ObjectUtils.argumentNotNull(tableName, "Table name must be not null");
		return withConnection(c -> getDialect().getPrimaryKey(tableName, c));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#refresh(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public PropertyBox refresh(DataTarget<?> target, PropertyBox propertyBox) {
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		try {
			final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(), AliasMode.AUTO);
			final TablePrimaryKey primaryKey = getTablePrimaryKey(context, target);
			return query().target(target).filter(getPrimaryKeyFilter(primaryKey, propertyBox)).findOne(propertyBox)
					.orElseThrow(() -> new DataAccessException(
							"No data found for primary key [" + printPrimaryKey(primaryKey, propertyBox) + "]"));
		} catch (InvalidExpressionException | QueryExecutionException e) {
			throw new DataAccessException("Refresh operation failed", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#save(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult save(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		boolean update = false;

		try {
			// check exist
			Optional<TablePrimaryKey> primaryKey = resolvePrimaryKey(
					JdbcResolutionContext.create(this, getDialect(), AliasMode.UNSUPPORTED), target);
			if (!primaryKey.isPresent()) {
				LOGGER.warn("(save) Cannot obtain the primary key for target [" + target
						+ "]: an INSERT operation will be performed by default");
				return insert(target, propertyBox);
			} else {
				final Path<?> singleKey = (primaryKey.get().getKeys().length == 1) ? primaryKey.get().getKeys()[0]
						: null;
				update = getOptionalPrimaryKeyFilter(primaryKey.get(), propertyBox).map(f -> {
					Query q = query().target(target).filter(f);
					if (singleKey != null) {
						return q.findOne(PathProperty.create(singleKey).count()).orElse(0L) > 0;
					}
					return q.count() > 0;
				}).orElse(false);
			}
		} catch (Exception e) {
			throw new DataAccessException("Failed to execute existence query to discern insert/update operation", e);
		}

		return update ? update(target, propertyBox, options) : insert(target, propertyBox, options);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#insert(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OperationResult insert(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(), AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.INSERT, target);
			// valid Paths with not null value
			propertyBox.stream().filter(property -> Path.class.isAssignableFrom(property.getClass()))
					.filter(path -> propertyBox.containsValue(path)).forEach(p -> {
						builder.withValue((Path) p, getPathValue((Path) p, propertyBox, false));
					});

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure insert operation", e);
		}

		// execute
		return withConnection(c -> {

			final PreparedSql preparedSql = JdbcDatastoreUtils.prepareSql(sql, context);
			trace(preparedSql.getSql());

			// primary key
			Optional<Path<?>[]> pk = Optional.empty();
			if (getDialect().supportsGetGeneratedKeys()) {
				try {
					pk = resolvePrimaryKey(context, target).map(k -> k.getKeys());
				} catch (Exception ex) {
					LOGGER.warn("Failed to obtain primary key of target [" + target + "]", ex);
				}
			}

			final Path<?>[] keys;
			final String[] pkNames;

			if (pk.isPresent() && pk.get().length > 0) {
				keys = pk.get();
				pkNames = new String[keys.length];
				for (int i = 0; i < keys.length; i++) {
					pkNames[i] = getDialect().getColumnName(keys[i].getName());
				}
			} else {
				keys = null;
				pkNames = null;
			}

			try (PreparedStatement stmt = createInsertStatement(c, context.getDialect(), preparedSql.getSql(),
					pkNames)) {

				// configure parameters
				context.getDialect().getStatementConfigurator().configureStatement(c, stmt, preparedSql.getSql(),
						preparedSql.getParameterValues());

				int inserted = stmt.executeUpdate();

				OperationResult.Builder result = OperationResult.builder().type(OperationType.INSERT)
						.affectedCount(inserted);

				if (keys != null) {
					// get generated keys
					try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							boolean bringBackGeneratedIds = JdbcDatastoreUtils.isBringBackGeneratedIds(options);
							final int columns = generatedKeys.getMetaData().getColumnCount();
							for (int i = 0; i < keys.length; i++) {
								if (i < columns) {
									final Object keyValue = generatedKeys.getObject(i + 1);
									result.withInsertedKey(keys[i], keyValue);
									if (bringBackGeneratedIds && keyValue != null) {
										// set in propertybox
										Property property = getPropertyForPath(keys[i], propertyBox);
										if (property != null) {
											// deserialize and set
											QueryExpression propertyExpression = (property instanceof QueryExpression)
													? (QueryExpression) property : null;
											propertyBox.setValue(property, getDialect().getValueDeserializer()
													.deserializeValue(propertyExpression, keyValue));
										}
									}
								}
							}
						}
					} catch (SQLException e) {
						LOGGER.warn("Failed to retrieve generated keys", e);
					}
				}

				return result.build();
			}
		});

	}

	/**
	 * Create a {@link PreparedStatement} for an INSERT operation configuring generated keys.
	 * @param connection Connection
	 * @param dialect Dialect
	 * @param sql SQL statement
	 * @param pkNames Optional primary key column names
	 * @return Configured statement
	 * @throws SQLException If an error occurred
	 */
	private PreparedStatement createInsertStatement(Connection connection, JdbcDialect dialect, String sql,
			String[] pkNames) throws SQLException {
		if (dialect.supportsGetGeneratedKeys()) {
			if (getDialect().supportGetGeneratedKeyByName() && pkNames != null && pkNames.length > 0) {
				return connection.prepareStatement(sql, pkNames);
			} else {
				return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}
		}
		return connection.prepareStatement(sql);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#update(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult update(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(), AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.UPDATE, target);
			// valid Paths
			propertyBox.stream().filter(p -> Path.class.isAssignableFrom(p.getClass())).map(p -> (Path<?>) p)
					.collect(Collectors.toList()).forEach(p -> {
						builder.withValue(p, getPathValue(p, propertyBox, false));
					});
			// primary key filter
			builder.withFilter(getPrimaryKeyFilter(getTablePrimaryKey(context, target), propertyBox));

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure update operation", e);
		}

		return withConnection(c -> {

			PreparedSql preparedSql = JdbcDatastoreUtils.prepareSql(sql, context);
			trace(preparedSql.getSql());

			try (PreparedStatement stmt = preparedSql.createStatement(c, getDialect())) {
				int result = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.UPDATE).affectedCount(result).build();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#delete(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public OperationResult delete(DataTarget<?> target, PropertyBox propertyBox, WriteOption... options) {

		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "PropertyBox must be not null");

		final JdbcResolutionContext context = JdbcResolutionContext.create(this, getDialect(),
				getDialect().deleteStatementAliasSupported() ? AliasMode.AUTO : AliasMode.UNSUPPORTED);

		final String sql;
		try {

			OperationStructure.Builder builder = OperationStructure.builder(OperationType.DELETE, target);
			// primary key filter
			builder.withFilter(getPrimaryKeyFilter(getTablePrimaryKey(context, target), propertyBox));

			// resolve OperationStructure
			sql = JdbcDatastoreUtils.resolveExpression(this, builder.build(), SQLToken.class, context).getValue();

		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Failed to configure delete operation", e);
		}

		// execute
		return withConnection(c -> {

			PreparedSql preparedSql = JdbcDatastoreUtils.prepareSql(sql, context);
			trace(preparedSql.getSql());

			try (PreparedStatement stmt = preparedSql.createStatement(c, getDialect())) {
				int deleted = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.DELETE).affectedCount(deleted).build();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkInsert(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.property.PropertySet, com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkInsert bulkInsert(DataTarget<?> target, PropertySet<?> propertySet, WriteOption... options) {
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		return new JdbcBulkInsert(this, target, getDialect(), isTraceEnabled(), propertySet);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkUpdate(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkUpdate bulkUpdate(DataTarget<?> target, WriteOption... options) {
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		return new JdbcBulkUpdate(this, target, getDialect(), isTraceEnabled());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.Datastore#bulkDelete(com.holonplatform.core.datastore.DataTarget,
	 * com.holonplatform.core.datastore.Datastore.WriteOption[])
	 */
	@Override
	public BulkDelete bulkDelete(DataTarget<?> target, WriteOption... options) {
		ObjectUtils.argumentNotNull(target, "Data target must be not null");
		return new JdbcBulkDelete(this, target, getDialect(), isTraceEnabled());
	}

	/**
	 * Try to detect a suitable {@link JdbcDialect} to use with given {@link DatabasePlatform}.
	 * @param platform DatabasePlatform
	 * @return The {@link JdbcDialect} to use with given platform, or empty if not available
	 */
	protected Optional<JdbcDialect> getDialectForPlatform(DatabasePlatform platform) {
		JdbcDialect dialect = null;
		if (platform != null) {
			switch (platform) {
			case DB2:
				dialect = new DB2Dialect();
				break;
			case DB2_AS400:
				dialect = new DB2Dialect();
				break;
			case DERBY:
				dialect = new DerbyDialect();
				break;
			case H2:
				dialect = new H2Dialect();
				break;
			case HANA:
				dialect = new HANADialect();
				break;
			case HSQL:
				dialect = new HSQLDialect();
				break;
			case INFORMIX:
				dialect = new InformixDialect();
				break;
			case MARIADB:
				dialect = new MariaDBDialect();
				break;
			case MYSQL:
				dialect = new MySQLDialect();
				break;
			case ORACLE:
				dialect = new OracleDialect();
				break;
			case POSTGRESQL:
				dialect = new PostgreSQLDialect();
				break;
			case SQLITE:
				dialect = new SQLiteDialect();
				break;
			case SQL_SERVER:
				dialect = new SQLServerDialect();
				break;
			case NONE:
			default:
				break;
			}
		}
		return Optional.ofNullable(dialect);
	}

	/**
	 * Resolve given {@link DataTarget} to obtain its parimary key as {@link TablePrimaryKey}.
	 * @param context Resolution context
	 * @param target Target to resolve
	 * @return Target primary key
	 * @throws InvalidExpressionException If data target cannot be resolved
	 */
	protected Optional<TablePrimaryKey> resolvePrimaryKey(JdbcResolutionContext context, DataTarget<?> target) {
		return resolve(target, TablePrimaryKey.class, context);
	}

	/**
	 * Get the target table primary key.
	 * @param context Resolution context
	 * @param target Data target
	 * @return Target table primary key
	 * @throws DataAccessException If the primary key cannot be retrieved
	 */
	protected TablePrimaryKey getTablePrimaryKey(JdbcResolutionContext context, DataTarget<?> target) {
		return resolvePrimaryKey(context, target).orElseThrow(
				() -> new DataAccessException("Cannot obtain the primary key for target [" + target + "]"));
	}

	/**
	 * Trace given SQL if {@link #isTraceEnabled()}.
	 * @param sql SQL to trace
	 */
	protected void trace(String sql) {
		if (isTraceEnabled()) {
			LOGGER.info("(TRACE) SQL: [" + sql + "]");
		} else {
			LOGGER.debug(() -> "SQL: [" + sql + "]");
		}
	}

	/**
	 * Get a {@link QueryFilter} using given <code>primaryKey</code> if primary key values are available from property
	 * box, matching by EQUAL operator the primary key paths whith their corresponding values read form given
	 * <code>propertyBox</code>.
	 * @param primaryKey Primary key
	 * @param propertyBox Property box which contains the primary key values
	 * @return Optional filter
	 */
	private Optional<QueryFilter> getOptionalPrimaryKeyFilter(TablePrimaryKey primaryKey, PropertyBox propertyBox) {
		List<QueryFilter> filters = new LinkedList<>();
		for (Path<?> path : primaryKey.getKeys()) {
			Property<?> property = getPropertyForPath(path, propertyBox);
			if (property == null || !propertyBox.containsValue(property)) {
				return Optional.empty();
			}
			filters.add(QueryFilter.eq(PathProperty.create(path.getName(), path.getType()),
					propertyBox.getValue(property)));
		}
		return QueryFilter.allOf(filters);
	}

	/**
	 * Get a {@link QueryFilter} using given <code>primaryKey</code>, matching by EQUAL operator the primary key paths
	 * whith their corresponding values read form given <code>propertyBox</code>.
	 * @param primaryKey Primary key
	 * @param propertyBox Property box which contains the primary key values
	 * @return The filter
	 * @throws DataAccessException If the primary key is not valid or the property box does not contain a value for a
	 *         primary key path
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private QueryFilter getPrimaryKeyFilter(TablePrimaryKey primaryKey, PropertyBox propertyBox) {
		List<QueryFilter> filters = new LinkedList<>();
		for (Path path : primaryKey.getKeys()) {
			filters.add(QueryFilter.eq(PathProperty.create(path.getName(), path.getType()),
					getPathValue(path, propertyBox, true)));
		}
		return QueryFilter.allOf(filters)
				.orElseThrow(() -> new DataAccessException("Invalid table primary key: no paths available"));
	}

	/**
	 * Get the value of given <code>path</code> from given <code>values</code> {@link PropertyBox}.
	 * @param path Path
	 * @param values Values
	 * @param required <code>true</code> is value must be present in property box
	 * @return The value which corresponds to given path contained in given property box
	 * @throws DataAccessException If the the property box does not contain a not null value for given path
	 */
	private Object getPathValue(Path<?> path, PropertyBox values, boolean required) {
		Property<?> property = getPropertyForPath(path, values);
		if (property == null) {
			throw new DataAccessException("A property which corresponds to the path " + "[" + path.getName()
					+ "] was not found in given property set");
		}
		if (required && !values.containsValue(property)) {
			throw new DataAccessException(
					"The property which corresponds to the path " + "[" + path.getName() + "] has no value");
		}
		return values.getValue(property);
	}

	/**
	 * Get the {@link Property} of given <code>propertySet</code> which corresponds to given {@link Path}, using the
	 * path name to match a {@link PathProperty} of set with the same name, if available.
	 * @param path Path for which to obtain the property
	 * @param propertySet Property set
	 * @return The property which corresponds to given {@link Path}, or <code>null</code> if not found
	 */
	private Property<?> getPropertyForPath(Path<?> path, PropertySet<?> propertySet) {
		if (path instanceof Property && propertySet.contains((Property<?>) path)) {
			return (Property<?>) path;
		}
		final String name = path.getName();
		for (Property<?> property : propertySet) {
			if (Path.class.isAssignableFrom(property.getClass())) {
				String pathAsColumn = getDialect().getColumnName(((Path<?>) property).getName());
				if (name.equals(pathAsColumn)) {
					return property;
				}
			}
		}
		return null;
	}

	/**
	 * Print given primary key paths and values.
	 * @param primaryKey Key to print
	 * @param values Key values
	 * @return String represnting the primary key paths and values
	 */
	private String printPrimaryKey(TablePrimaryKey primaryKey, PropertyBox values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < primaryKey.getKeys().length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(primaryKey.getKeys()[i].getName());
			sb.append("=");
			Property<?> property = getPropertyForPath(primaryKey.getKeys()[i], values);
			if (property != null && values.containsValue(property)) {
				sb.append(values.getValue(property));
			} else {
				sb.append("[NULL]");
			}
		}
		return sb.toString();
	}

	// Builder

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
			ObjectUtils.argumentNotNull(database, "Database platform must be not null");
			datastore.setDatabase(database);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#dialect(com.holonplatform.datastore.jdbc.JdbcDialect)
		 */
		@Override
		public JdbcDatastore.Builder<D> dialect(JdbcDialect dialect) {
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
				datastore.setDialect((JdbcDialect) Class.forName(dialectClassName).newInstance());
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to istantiate dialect class [" + dialectClassName + "]", e);
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.JdbcDatastore.Builder#autoCommit(boolean)
		 */
		@Override
		public JdbcDatastore.Builder<D> autoCommit(boolean autoCommit) {
			datastore.setAutoCommit(autoCommit);
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
