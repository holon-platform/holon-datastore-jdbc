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
package com.holonplatform.datastore.jdbc.test.expression;

import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.query.QueryFilter;

@SuppressWarnings("serial")
public class MyEqualPredicate<T> implements QueryFilter {

	private final PathProperty<T> property;
	private final T value;

	public MyEqualPredicate(PathProperty<T> property, T value) {
		super();
		this.property = property;
		this.value = value;
	}

	public PathProperty<T> getProperty() {
		return property;
	}

	public T getValue() {
		return value;
	}

	@Override
	public void validate() throws InvalidExpressionException {
		if (property == null) {
			throw new InvalidExpressionException("Null property");
		}
	}

}
