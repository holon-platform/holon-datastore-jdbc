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
package com.holonplatform.datastore.jdbc.internal.pk;

import java.io.Serializable;
import java.util.Optional;

import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;

/**
 * {@link TablePrimaryKey}s cache.
 *
 * @since 5.0.0
 */
public interface PrimaryKeysCache extends Serializable {

	/**
	 * Get a cached {@link TablePrimaryKey} value for given <code>tableName</code>, if available.
	 * @param tableName Table name
	 * @return Cached {@link TablePrimaryKey} value, empty if not available
	 */
	Optional<TablePrimaryKey> get(String tableName);

	/**
	 * Put a {@link TablePrimaryKey} value for given <code>tableName</code> in cache.
	 * @param tableName Table name
	 * @param primaryKey Primary key
	 */
	void put(String tableName, TablePrimaryKey primaryKey);

	/**
	 * Clear cache.
	 */
	void clear();

	/**
	 * Create a new {@link PrimaryKeysCache}.
	 * @return A new {@link PrimaryKeysCache}
	 */
	static PrimaryKeysCache create() {
		return new DefaultPrimaryKeysCache();
	}

}
