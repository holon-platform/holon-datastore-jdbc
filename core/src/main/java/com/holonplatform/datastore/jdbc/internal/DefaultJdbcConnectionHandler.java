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

import javax.sql.DataSource;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jdbc.JdbcConnectionHandler;

/**
 * Default {@link JdbcConnectionHandler}.
 *
 * @since 5.1.0
 */
public class DefaultJdbcConnectionHandler implements JdbcConnectionHandler {

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcConnectionHandler#getConnection(javax.sql.DataSource,
	 * com.holonplatform.datastore.jdbc.JdbcConnectionHandler.ConnectionType)
	 */
	@Override
	public Connection getConnection(DataSource dataSource, ConnectionType connectionType) throws SQLException {
		final Connection connection = dataSource.getConnection();
		LOGGER.debug(() -> "Obtained a DataSource connection: [" + connection + "]");
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcConnectionHandler#releaseConnection(java.sql.Connection,
	 * com.holonplatform.datastore.jdbc.JdbcConnectionHandler.ConnectionType)
	 */
	@Override
	public void releaseConnection(Connection connection, ConnectionType connectionType) throws SQLException {
		connection.close();
		LOGGER.debug(() -> "Closed connection: [" + connection + "]");
	}

}
