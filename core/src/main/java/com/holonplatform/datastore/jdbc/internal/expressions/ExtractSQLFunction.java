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

import java.util.List;

import com.holonplatform.datastore.jdbc.expressions.SQLFunction;

/**
 * EXTRACT function.
 * 
 * @since 5.1.0
 */
public class ExtractSQLFunction implements SQLFunction {

	private static final long serialVersionUID = 7678038450875263298L;

	private final String partName;

	public ExtractSQLFunction(String partName) {
		super();
		this.partName = partName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.expressions.SQLFunction#serialize(java.util.List)
	 */
	@Override
	public String serialize(List<String> arguments) throws InvalidExpressionException {
		if (arguments == null || arguments.size() != 1) {
			throw new InvalidExpressionException("EXTRACT function requires one argument");
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("EXTRACT(");
		sb.append(partName);
		sb.append(" FROM ");
		sb.append(arguments.get(0));
		sb.append(")");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
	}

}
