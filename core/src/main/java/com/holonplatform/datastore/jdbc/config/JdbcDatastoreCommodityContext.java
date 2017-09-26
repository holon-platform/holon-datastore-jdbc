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
package com.holonplatform.datastore.jdbc.config;

import java.util.Optional;

import javax.sql.DataSource;

import com.holonplatform.core.datastore.DatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.jdbc.DatabasePlatform;

/**
 * JDBC Datastore {@link DatastoreCommodityContext}.
 *
 * @since 5.0.0
 */
public interface JdbcDatastoreCommodityContext extends JdbcDatastore, DatastoreCommodityContext {

	/**
	 * Get the {@link DataSource} bound to the datastore.
	 * @return Datastore DataSource
	 */
	DataSource getDataSource();

	/**
	 * Get the database type to which {@link DataSource} is connected.
	 * @return The {@link DatabasePlatform} of the {@link DataSource} database, empty if not available
	 */
	Optional<DatabasePlatform> getDatabase();

	/**
	 * Get the {@link JdbcDialect} bound to the datastore.
	 * @return Datastore dialect
	 */
	JdbcDialect getDialect();

	/**
	 * Get whether to trace Datastore operations.
	 * @return the trace <code>true</code> if tracing is enabled, <code>false</code> otherwise
	 */
	boolean isTraceEnabled();

}
