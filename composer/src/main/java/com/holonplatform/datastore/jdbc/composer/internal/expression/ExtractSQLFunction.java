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

import com.holonplatform.datastore.jdbc.composer.expression.SQLFunction;

/**
 * EXTRACT function.
 * 
 * @since 5.1.0
 */
public class ExtractSQLFunction implements SQLFunction {

	private final String partName;

	/**
	 * Constructor
	 * @param partName The part name to use for the EXTRACT function
	 */
	public ExtractSQLFunction(String partName) {
		super();
		this.partName = partName;
	}

	/**
	 * Get the part name to use for the EXTRACT function.
	 * @return the part name
	 */
	public String getPartName() {
		return partName;
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
		sb.append(getPartName());
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
		if (getPartName() == null) {
			throw new InvalidExpressionException("EXTRACT function part name must be not null");
		}
	}

}
