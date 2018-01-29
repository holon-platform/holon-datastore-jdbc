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
package com.holonplatform.datastore.jdbc.composer.internal.expression;

import com.holonplatform.core.Path;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;

/**
 * Default {@link SQLPrimaryKey} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLPrimaryKey implements SQLPrimaryKey {

	private final Path<?>[] paths;

	public DefaultSQLPrimaryKey(Path<?>[] paths) {
		super();
		this.paths = paths;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey#getPaths()
	 */
	@Override
	public Path<?>[] getPaths() {
		return paths;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getPaths() == null || getPaths().length == 0) {
			throw new InvalidExpressionException("Null or empty primary key path array");
		}
	}

}
