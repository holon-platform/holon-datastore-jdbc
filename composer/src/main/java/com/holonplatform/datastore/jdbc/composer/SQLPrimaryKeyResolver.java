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

import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLPrimaryKeyResolver;

/**
 * Interface to resolve the primary key of a {@link PropertyBox} as a set of {@link Path}s.
 *
 * @since 5.1.0
 */
public interface SQLPrimaryKeyResolver {

	/**
	 * Get the primary key {@link Path}s of given <code>propertyBox</code> in relation to given {@link DataTarget}.
	 * @param target The data target
	 * @param propertyBox The {@link PropertyBox}
	 * @return The primary key paths, or an empty Optional if the primary key cannot be resolved
	 */
	Optional<Path<?>[]> getPrimaryKey(DataTarget<?> target, PropertyBox propertyBox);

	/**
	 * Get the default {@link SQLPrimaryKeyResolver}.
	 * @return the default {@link SQLPrimaryKeyResolver}
	 */
	static SQLPrimaryKeyResolver getDefault() {
		return DefaultSQLPrimaryKeyResolver.INSTANCE;
	}

}
