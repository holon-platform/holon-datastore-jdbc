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
import com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher;

/**
 * A {@link PathMatcher} which uses checks {@link DataMappable#PATH} configuration property if available when matching
 * path names.
 * 
 * @since 5.1.0
 */
public class DataPathAwarePathMatcher implements PathMatcher {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.property.PathPropertySetAdapter.PathMatcher#match(com.holonplatform.core.Path,
	 * com.holonplatform.core.Path)
	 */
	@Override
	public boolean match(Path<?> path, Path<?> otherPath) {
		if (path != null && otherPath != null) {
			return JdbcOperationUtils.getPathName(path).equals(JdbcOperationUtils.getPathName(otherPath));
		}
		return false;
	}

}
