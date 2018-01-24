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
package com.holonplatform.datastore.jdbc;

import java.sql.Connection;
import java.util.function.Function;

import javax.sql.DataSource;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodityRegistrar;
import com.holonplatform.core.datastore.transaction.Transactional;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.DefaultJdbcDatastore;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.JdbcConnectionHandler;

/**
 * JDBC {@link Datastore}.
 * 
 * @since 5.0.0
 */
public interface JdbcDatastore
		extends Datastore, Transactional, DatastoreCommodityRegistrar<JdbcDatastoreCommodityContext> {

	/**
	 * Execute given <code>operation</code> using a new {@link Connection} provided by the Datastore and return the
	 * operation result.
	 * <p>
	 * The connection lifecycle if managed by Datastore, performing the appropriate closing operations at the end of the
	 * {@link Function} execution.
	 * </p>
	 * @param <R> Operation result type
	 * @param operation The operation to execute (not null)
	 * @return Operation result
	 * @throws IllegalStateException If a {@link DataSource} is not available or not initialized
	 * @throws DataAccessException If an error occurred during connection management or operation execution
	 */
	<R> R withConnection(ConnectionOperation<R> operation);

	/**
	 * Represents an operation to be executed using a Datastore managed {@link Connection}.
	 * @param <R> Operation result type
	 */
	@FunctionalInterface
	public interface ConnectionOperation<R> {

		/**
		 * Execute an operation and returns a result.
		 * @param connection JDBC Connection
		 * @return Operation result
		 * @throws Exception If an operation execution error occurred
		 */
		R execute(Connection connection) throws Exception;

	}

	// Builder

	/**
	 * Get a builder to create a {@link JdbcDatastore} instance.
	 * @return Datastore builder
	 */
	static Builder<JdbcDatastore> builder() {
		return new DefaultJdbcDatastore.DefaultBuilder();
	}

	/**
	 * {@link JdbcDatastore} builder.
	 * @param <D> {@link JdbcDatastore} type
	 */
	public interface Builder<D extends JdbcDatastore> extends Datastore.Builder<D, Builder<D>> {

		/**
		 * Set the {@link DataSource} to use.
		 * @param dataSource DataSource to set (not null)
		 * @return this
		 */
		Builder<D> dataSource(DataSource dataSource);

		/**
		 * Set the configuration property source to build the {@link DataSource} to use with the Datastore.
		 * @param configuration DataSource configuration properties (not null)
		 * @return this
		 */
		Builder<D> dataSource(DataSourceConfigProperties configuration);

		/**
		 * Set the database platform to which the DataSource is connected.
		 * <p>
		 * If {@link DataSourceConfigProperties} are provided, the database platform is obtained from the
		 * {@link DataSourceConfigProperties#PLATFORM} property or auto-detected by the datastore if the property is not
		 * specified.
		 * </p>
		 * @param database Database platform to set (not null)
		 * @return this
		 */
		Builder<D> database(DatabasePlatform database);

		/**
		 * Set the dialect to use.
		 * <p>
		 * If a {@link DatabasePlatform} is provided (using {@link #database(DatabasePlatform)} or read/detected from
		 * the DataSource configuration properties with {@link #dataSource(DataSourceConfigProperties)}), the datastore
		 * tries to autodetect the dialect to use, if available.
		 * </p>
		 * @param dialect The dialect to set (not null)
		 * @return this
		 */
		Builder<D> dialect(JdbcDialect dialect);

		/**
		 * Set the fully qualified dialect class name to use as datastore dialect.
		 * <p>
		 * If a {@link DatabasePlatform} is provided (using {@link #database(DatabasePlatform)} or read/detected from
		 * the DataSource configuration properties with {@link #dataSource(DataSourceConfigProperties)}), the datastore
		 * tries to autodetect the dialect to use, if available.
		 * </p>
		 * @param dialectClassName The dialect class name to set (not null)
		 * @return this
		 */
		Builder<D> dialect(String dialectClassName);

		/**
		 * Set whether the auto-commit mode has to be setted for connections. (default is <code>true</code>)
		 * @param autoCommit Whether to set connections auto-commit
		 * @return this
		 * @deprecated Use transaction operations to manage connection auto-commit or provide a custom
		 *             {@link JdbcConnectionHandler} for more complex situations
		 */
		@Deprecated
		Builder<D> autoCommit(boolean autoCommit);

		/**
		 * Set a custom {@link JdbcConnectionHandler} to be used for Datastore JDBC connections handling.
		 * @param connectionHandler The connection handler to set (not null)
		 * @return this
		 */
		Builder<D> connectionHandler(JdbcConnectionHandler connectionHandler);

	}

}
