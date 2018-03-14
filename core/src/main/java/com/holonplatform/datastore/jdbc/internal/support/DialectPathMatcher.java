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
package com.holonplatform.datastore.jdbc.internal.support;

import com.holonplatform.core.DataMappable;
import com.holonplatform.core.Path;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;

/**
 * A {@link PathMatcher} which uses {@link SQLDialect#getColumnName(String)} to normalize path names and ignores case
 * when matching path names.
 * 
 * @since 5.1.0
 */
public class DialectPathMatcher implements PathMatcher {

	private final SQLDialect dialect;

	/**
	 * Constructor.
	 * @param dialect SQL dialect (not null)
	 */
	public DialectPathMatcher(SQLDialect dialect) {
		super();
		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		this.dialect = dialect;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher#match(com.holonplatform.core.Path,
	 * com.holonplatform.core.Path)
	 */
	@Override
	public boolean match(Path<?> path, Path<?> otherPath) {
		if (path != null && otherPath != null) {
			return getPathName(path).equalsIgnoreCase(dialect.getColumnName(getPathName(otherPath)));
		}
		return false;
	}

	/**
	 * Get the path data model name, using {@link DataMappable#getDataPath()} if path is {@link DataMappable} or
	 * returning the path name if not.
	 * @param path The path for which to obtain the data path name
	 * @return The data path name
	 */
	private static String getPathName(Path<?> path) {
		return DataMappable.isDataMappable(path).flatMap(dm -> dm.getDataPath()).orElse(path.getName());
	}

}
