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

import java.util.Collections;
import java.util.List;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.expressions.SQLFunction;

/**
 * Default {@link FunctionInvocation} implementation.
 *
 * @since 5.1.0
 */
public class DefaultFunctionInvocation implements FunctionInvocation {

	private final SQLFunction function;
	private final List<String> arguments;

	public DefaultFunctionInvocation(SQLFunction function, List<String> arguments) {
		super();
		ObjectUtils.argumentNotNull(function, "SQLFunction must be not null");
		this.function = function;
		this.arguments = (arguments == null) ? Collections.emptyList() : arguments;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getFunction() == null) {
			throw new InvalidExpressionException("Null SQLFunction");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.FunctionInvocation#getFunction()
	 */
	@Override
	public SQLFunction getFunction() {
		return function;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.FunctionInvocation#getArguments()
	 */
	@Override
	public List<String> getArguments() {
		return arguments;
	}

}
