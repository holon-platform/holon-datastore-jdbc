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
package com.holonplatform.datastore.jdbc.composer.expression;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Path;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLPrimaryKey;

/**
 * Represents a database table primary key.
 * <p>
 * Each column of the primary key is represented by a {@link Path}.
 * </p>
 *
 * @since 5.1.0
 */
public interface SQLPrimaryKey extends Expression {

	/**
	 * Get the primary key {@link Path}s.
	 * @return the primary key (not null and not empty)
	 */
	Path<?>[] getPaths();

	/**
	 * Create a new {@link SQLPrimaryKey} using given {@link Path}s.
	 * @param paths Primary key paths
	 * @return A new {@link SQLPrimaryKey}
	 */
	static SQLPrimaryKey create(Path<?>... paths) {
		return new DefaultSQLPrimaryKey(paths);
	}

}
