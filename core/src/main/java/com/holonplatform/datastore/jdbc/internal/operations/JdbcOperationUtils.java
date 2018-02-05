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
package com.holonplatform.datastore.jdbc.internal.operations;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.internal.DialectPathMatcher;

/**
 * JDBC operations utility class.
 *
 * @since 5.1.0
 */
public final class JdbcOperationUtils implements Serializable {

	private static final long serialVersionUID = -6098697007380654478L;

	private JdbcOperationUtils() {
	}

	/**
	 * Get the {@link QueryFilter} to select the row which corresponds to given <code>primaryKey</code> using the
	 * primary key values provided by given {@link PropertyBox}.
	 * @param dialect Dialect to perform path-property matching (not null)
	 * @param primaryKey Primary keys (not null)
	 * @param propertyBox Primary key values (not null)
	 * @return The primary key filter
	 * @throws DataAccessException If a primary key path has no value correspondence in given PropertyBox
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static QueryFilter getPrimaryKeyFilter(SQLDialect dialect, SQLPrimaryKey primaryKey,
			PropertyBox propertyBox) {
		ObjectUtils.argumentNotNull(primaryKey, "Primary key must be not null");
		ObjectUtils.argumentNotNull(propertyBox, "Primary key values must be not null");

		final PathPropertyBoxAdapter adapter = PathPropertyBoxAdapter.builder(propertyBox)
				.pathMatcher(new DialectPathMatcher(dialect)).build();

		List<QueryFilter> filters = new LinkedList<>();
		for (Path path : primaryKey.getPaths()) {
			Optional<Object> value = adapter.getValue(path);
			if (!value.isPresent()) {
				throw new DataAccessException("Primary key path [" + path + "] value not available in PropertyBox");
			}
			filters.add(QueryFilter.eq(path, value.get()));
		}
		return QueryFilter.allOf(filters)
				.orElseThrow(() -> new DataAccessException("Invalid primary key: no paths available"));
	}

}
