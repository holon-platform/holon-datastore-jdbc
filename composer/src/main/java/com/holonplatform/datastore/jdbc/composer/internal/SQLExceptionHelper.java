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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.sql.SQLException;
import java.util.Optional;

/**
 * {@link SQLException} codes extraction helper.
 *
 * @since 5.2.0
 */
public class SQLExceptionHelper {

	/**
	 * Get the vendor-specific error code.
	 * @param sqlException The exception from which to extract the code
	 * @return The error code
	 */
	public static int getErrorCode(SQLException sqlException) {
		int errorCode = sqlException.getErrorCode();
		SQLException nested = sqlException.getNextException();
		while (errorCode == 0 && nested != null) {
			errorCode = nested.getErrorCode();
			nested = nested.getNextException();
		}
		return errorCode;
	}

	/**
	 * Get the SQL state.
	 * @param sqlException The exception from which to extract the state
	 * @return Optional SQL state
	 */
	public static Optional<String> getSqlState(SQLException sqlException) {
		String sqlState = sqlException.getSQLState();
		SQLException nested = sqlException.getNextException();
		while (sqlState == null && nested != null) {
			sqlState = nested.getSQLState();
			nested = nested.getNextException();
		}
		return Optional.ofNullable(sqlState);
	}

}
