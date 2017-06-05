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
package com.holonplatform.datastore.jdbc.internal.dialect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction.DefaultFunction;

/**
 * Default {@link SQLFunction}s.
 *
 * @since 5.0.0
 */
public final class DefaultSQLFunctions implements Serializable {

	private static final long serialVersionUID = 2108570677423116650L;

	/**
	 * Default functions name-function map
	 */
	private final static Map<String, SQLFunction> functions = new HashMap<>();

	static {
		functions.put(DefaultFunction.COUNT.getName(), SQLFunction.create("count", true));
		functions.put(DefaultFunction.MIN.getName(), SQLFunction.create("min", true));
		functions.put(DefaultFunction.MAX.getName(), SQLFunction.create("max", true));
		functions.put(DefaultFunction.AVG.getName(), SQLFunction.create("avg", true));
		functions.put(DefaultFunction.SUM.getName(), SQLFunction.create("sum", true));
		functions.put(DefaultFunction.LOWER.getName(), SQLFunction.create("lower", true));
		functions.put(DefaultFunction.UPPER.getName(), SQLFunction.create("upper", true));
	}

	/**
	 * Get the {@link SQLFunction} bound to given symbolic name.
	 * @param name Function symbolic name
	 * @return Optional {@link SQLFunction} bound to given symbolic name
	 */
	public static Optional<SQLFunction> getFunction(String name) {
		ObjectUtils.argumentNotNull(name, "Function name must be not null");
		return Optional.ofNullable(functions.get(name));
	}

	/**
	 * Get the {@link SQLFunction} implementation to use for given function <code>name</code>, using given dialect or
	 * falling back to default functions.
	 * @param name Function name
	 * @param dialect Jdbc dialect
	 * @return {@link SQLFunction} implementation to use for given function <code>name</code>, if available
	 */
	public static Optional<SQLFunction> getSQLFunction(String name, JdbcDialect dialect) {
		SQLFunction dialectFunction = dialect.getFunction(name);
		if (dialectFunction != null) {
			return Optional.of(dialectFunction);
		}
		return DefaultSQLFunctions.getFunction(name);
	}

}
