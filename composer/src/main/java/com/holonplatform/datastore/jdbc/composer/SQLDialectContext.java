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
package com.holonplatform.datastore.jdbc.composer;

import java.sql.DatabaseMetaData;
import java.util.Optional;

import com.holonplatform.core.ExpressionResolver.ExpressionResolverSupport;

/**
 * Context which can be used for {@link SQLDialect} initialization.
 *
 * @since 5.1.0
 */
public interface SQLDialectContext extends ExpressionResolverSupport {

	/**
	 * Get the {@link SQLValueSerializer} if this context.
	 * @return The {@link SQLValueSerializer}
	 */
	SQLValueSerializer getValueSerializer();

	/**
	 * Get the {@link SQLValueDeserializer} if this context.
	 * @return the {@link SQLValueDeserializer}
	 */
	SQLValueDeserializer getValueDeserializer();

	/**
	 * Get the database metadata information, if available.
	 * @return Optional database metadata information
	 */
	Optional<DatabaseMetaData> getDatabaseMetaData();

	/**
	 * Get the JDBC connection provider, if available.
	 * @return Optional the JDBC connection provider
	 */
	Optional<ConnectionHandler> getConnectionProvider();

	/**
	 * Get the database metadata information, using {@link #getDatabaseMetaData()} if available or try to obtain it from
	 * a JDBC connection if a {@link ConnectionHandler} is available.
	 * @return Optional database metadata information
	 */
	default Optional<DatabaseMetaData> getOrRetrieveDatabaseMetaData() {
		Optional<DatabaseMetaData> metadata = getDatabaseMetaData();
		if (metadata.isPresent()) {
			return metadata;
		}
		return getConnectionProvider()
				.map(connectionProvider -> connectionProvider.withConnection(c -> c.getMetaData()));
	}

}
