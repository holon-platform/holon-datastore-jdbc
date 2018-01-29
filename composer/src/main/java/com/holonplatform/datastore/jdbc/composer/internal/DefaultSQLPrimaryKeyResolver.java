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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.util.Optional;
import java.util.Set;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.composer.SQLPrimaryKeyResolver;

/**
 * Default {@link SQLPrimaryKeyResolver} using {@link PropertyBox} identifier properties to provide the primary key, if
 * available.
 * 
 * @since 5.1.0
 */
public enum DefaultSQLPrimaryKeyResolver implements SQLPrimaryKeyResolver {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLPrimaryKeyResolver#getPrimaryKey(com.holonplatform.core.datastore.
	 * DataTarget, com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public Optional<Path<?>[]> getPrimaryKey(DataTarget<?> target, PropertyBox propertyBox) {
		if (propertyBox != null) {
			Set<Path<?>> identifiers = PathPropertyBoxAdapter.create(propertyBox).getPathIdentifiers();
			if (!identifiers.isEmpty()) {
				return Optional.of(identifiers.toArray(new Path<?>[identifiers.size()]));
			}
		}
		return Optional.empty();
	}

}
