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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;

import java.util.Optional;

/**
 * Default {@link PrimaryKeysCache} implementation.
 *
 * @since 5.0.0
 */
public class DefaultPrimaryKeysCache implements PrimaryKeysCache {

	private static final long serialVersionUID = 8124696080416427573L;

	/**
	 * Max cache size
	 */
	private final static int MAX_CACHE_SIZE = 1000;

	/**
	 * Cache map
	 */
	@SuppressWarnings("serial")
	private final LinkedHashMap<String, TablePrimaryKey> cache = new LinkedHashMap<String, TablePrimaryKey>(16, 0.75f,
			true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, TablePrimaryKey> eldest) {
			return size() > MAX_CACHE_SIZE;
		}

	};

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.PrimaryKeysCache#get(java.lang.String)
	 */
	@Override
	public Optional<TablePrimaryKey> get(String tableName) {
		return Optional.ofNullable(cache.get(tableName));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.PrimaryKeysCache#put(java.lang.String,
	 * com.holonplatform.datastore.jdbc.internal.TablePrimaryKey)
	 */
	@Override
	public void put(String tableName, TablePrimaryKey primaryKey) {
		cache.put(tableName, primaryKey);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.PrimaryKeysCache#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

}
