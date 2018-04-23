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

/**
 * JDBC connection handler.
 * <p>
 * The concrete handler should manage connection lifecycle, including connection retrieving and releasing.
 * </p>
 *
 * @since 5.1.0
 */
public interface ConnectionHandler {

	/**
	 * Execute given {@link ConnectionOperation} using a managed connection.
	 * @param <R> Operation result type
	 * @param operation Operation to execute (not null)
	 * @return Operation result
	 */
	<R> R withConnection(ConnectionOperation<R> operation);

	/**
	 * Execute given {@link ConnectionRunnable} operation using a managed connection.
	 * @param operation Operation to execute (not null)
	 */
	default void withConnection(ConnectionRunnable operation) {
		withConnection(connection -> {
			operation.execute(connection);
			return null;
		});
	}

}
