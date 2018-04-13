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

import java.sql.Connection;

/**
 * Represents an operation to be executed using a Datastore managed JDBC {@link Connection}.
 * <p>
 * Differently from {@link ConnectionOperation}, the operation execution does not return any result.
 * </p>
 * 
 * @since 5.1.0
 * 
 * @see ConnectionOperation
 */
@FunctionalInterface
public interface ConnectionRunnable {

	/**
	 * Execute an operation using a managed JDBC {@link Connection}.
	 * <p>
	 * The {@link Connection} lifecycle should be managed by the connection provider, so the connection should not be
	 * closed from whithin the connection operation method.
	 * </p>
	 * @param connection The JDBC Connection
	 * @throws Exception If an error occurred
	 */
	void execute(Connection connection) throws Exception;

}
