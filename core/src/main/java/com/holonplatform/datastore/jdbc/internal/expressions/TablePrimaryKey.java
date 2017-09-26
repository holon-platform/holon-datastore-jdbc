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
package com.holonplatform.datastore.jdbc.internal.expressions;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Path;

/**
 * Represents the primary key of a table.
 *
 * @since 5.0.0
 */
public interface TablePrimaryKey extends Expression {

	/**
	 * Get the primary keys {@link Path}s.
	 * @return Primary keys array
	 */
	Path<?>[] getKeys();

	/**
	 * Create a new {@link TablePrimaryKey}.
	 * @param keys Primary key paths
	 * @return A new {@link TablePrimaryKey}
	 */
	static TablePrimaryKey create(Path<?>[] keys) {
		return new DefaultTablePrimaryKey(keys);
	}

}
