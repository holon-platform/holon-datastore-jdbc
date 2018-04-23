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
import java.util.function.Function;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

/**
 * {@link SQLFunction} implementation using a {@link Function} callback.
 * 
 * @since 5.1.0
 */
public class CallbackSQLFunction implements SQLFunction {

	private final Function<List<String>, String> callback;

	public CallbackSQLFunction(Function<List<String>, String> callback) {
		super();
		ObjectUtils.argumentNotNull(callback, "Callback function must be not null");
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLFunction#serialize(java.util.List)
	 */
	@Override
	public String serialize(List<String> arguments) throws InvalidExpressionException {
		return callback.apply(arguments);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

}
