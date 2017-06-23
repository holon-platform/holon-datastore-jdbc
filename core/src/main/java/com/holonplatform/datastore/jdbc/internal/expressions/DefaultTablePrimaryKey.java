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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.Arrays;

import com.holonplatform.core.Path;

/**
 * Default {@link TablePrimaryKey} implementation.
 *
 * @since 5.0.0
 */
public class DefaultTablePrimaryKey implements TablePrimaryKey {

	/**
	 * Primary key paths
	 */
	private final Path<?>[] keys;

	/**
	 * Constructor
	 * @param keys Primary key paths
	 */
	public DefaultTablePrimaryKey(Path<?>[] keys) {
		super();
		this.keys = keys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.TablePrimaryKey#getKeys()
	 */
	@Override
	public Path<?>[] getKeys() {
		return keys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getKeys() == null) {
			throw new InvalidExpressionException("Null key paths");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TablePrimaryKey[keys=" + Arrays.toString(keys) + "]";
	}

}
