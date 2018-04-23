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

import com.holonplatform.datastore.jdbc.composer.SQLType;

/**
 * Default {@link SQLType} implementation.
 *
 * @since 5.1.0
 */
public class DefaultSQLType implements SQLType {

	private static final long serialVersionUID = 3963331833334313846L;

	/**
	 * Type id
	 */
	private final int type;

	/**
	 * Type name
	 */
	private final String name;

	/**
	 * Constructor
	 * @param type Type id
	 * @param name Optional type name
	 */
	public DefaultSQLType(int type, String name) {
		super();
		this.type = type;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLType#getType()
	 */
	@Override
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLType#getName()
	 */
	@Override
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLType [type=" + type + ", name=" + name + "]";
	}

}
