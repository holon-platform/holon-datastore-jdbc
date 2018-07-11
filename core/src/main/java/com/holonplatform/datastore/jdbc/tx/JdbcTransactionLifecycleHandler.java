/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.tx;

/**
 * Handler for {@link JdbcTransaction} lifecycle management.
 *
 * @since 5.2.0
 */
public interface JdbcTransactionLifecycleHandler {

	/**
	 * A transaction has started.
	 * @param transaction The transaction which started
	 */
	void transactionStarted(JdbcTransaction transaction);

	/**
	 * A transaction is ended.
	 * @param transaction The transaction which ended
	 */
	void transactionEnded(JdbcTransaction transaction);

}
