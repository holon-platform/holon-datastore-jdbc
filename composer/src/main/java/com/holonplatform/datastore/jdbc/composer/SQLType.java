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

import java.io.Serializable;
import java.sql.Types;
import java.util.Optional;

import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLType;

/**
 * Represents a SQL type.
 *
 * @since 5.1.0
 */
public interface SQLType extends Serializable {

	/**
	 * Get the SQL type id.
	 * @return the SQL type id
	 * @see Types
	 */
	int getType();

	/**
	 * Get the optional type name.
	 * @return optional type name
	 */
	Optional<String> getName();

	/**
	 * Create a new {@link SQLType}.
	 * @param type Type id
	 * @return A new {@link SQLType}
	 */
	static SQLType create(int type) {
		return new DefaultSQLType(type, null);
	}

	/**
	 * Create a new {@link SQLType}.
	 * @param type Type id
	 * @param name Optional type name
	 * @return A new {@link SQLType}
	 */
	static SQLType create(int type, String name) {
		return new DefaultSQLType(type, name);
	}

}
