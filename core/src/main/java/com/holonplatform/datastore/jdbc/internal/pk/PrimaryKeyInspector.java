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
package com.holonplatform.datastore.jdbc.internal.pk;

import java.sql.SQLException;
import java.util.Optional;

import com.holonplatform.core.Path;

/**
 * Implemented by a class wich can inspect a table and provide its primary key, if available.
 *
 * @since 5.0.0
 */
public interface PrimaryKeyInspector {

	/**
	 * Get the <em>primary key</em> paths for given <code>tableName</code>, if available.
	 * @param tableName Table name for which to retrieve the primary key (not null)
	 * @return Table primary key paths, or empty if the primary key is not available
	 * @throws SQLException If a database error occurred
	 */
	Optional<Path<?>[]> getPrimaryKey(String tableName) throws SQLException;

}
