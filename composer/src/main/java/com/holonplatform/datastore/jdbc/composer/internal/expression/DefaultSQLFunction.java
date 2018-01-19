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

import java.util.List;
import java.util.stream.Collectors;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

/**
 * Default {@link SQLFunction} implementation.
 *
 * @since 5.0.0
 */
public class DefaultSQLFunction implements SQLFunction {

	/**
	 * Function name
	 */
	private final String name;

	/**
	 * Whether to use parentheses even if function has no arguments
	 */
	private final boolean parenthesesIfNoArguments;

	/**
	 * Constructor
	 * @param name Function name (not null)
	 * @param parenthesesIfNoArguments Whether parentheses are required if there are no arguments
	 */
	public DefaultSQLFunction(String name, boolean parenthesesIfNoArguments) {
		super();
		ObjectUtils.argumentNotNull(name, "Function name must be not null");
		this.name = name;
		this.parenthesesIfNoArguments = parenthesesIfNoArguments;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getName() == null) {
			throw new InvalidExpressionException("Null function name");
		}
	}

	/**
	 * Get the function name
	 * @return The function name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get whether parentheses are required if there are no arguments.
	 * @return <code>true</code> if parentheses are required if there are no arguments, <code>false</code> otherwise
	 */
	protected boolean hasParenthesesIfNoArguments() {
		return parenthesesIfNoArguments;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.SQLFunction#serialize(java.util.List)
	 */
	@Override
	public String serialize(List<String> arguments) {
		final StringBuilder sb = new StringBuilder();
		// function name
		sb.append(getName());
		// arguments
		if (arguments != null && !arguments.isEmpty()) {
			sb.append("(");
			sb.append(arguments.stream().collect(Collectors.joining(",")));
			sb.append(")");
		} else {
			if (hasParenthesesIfNoArguments()) {
				sb.append("()");
			}
		}
		return sb.toString();
	}

}
