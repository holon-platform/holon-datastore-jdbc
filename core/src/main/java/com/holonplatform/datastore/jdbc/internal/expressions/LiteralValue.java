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

import java.io.Serializable;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.temporal.TemporalType;

/**
 * Represents a value which can be associated with a {@link QueryExpression}.
 *
 * @since 5.0.0
 */
public interface LiteralValue extends Expression, Serializable {

	/**
	 * Get the value
	 * @return The value
	 */
	Object getValue();

	/**
	 * If the value is a temporal value, get the {@link TemporalType} if available.
	 * @return The value {@link TemporalType}, empty is value is not a temporal value or the temporal type cannot be
	 *         detected
	 */
	Optional<TemporalType> getTemporalType();

	/**
	 * Create an {@link LiteralValue} using given value.
	 * @param value Value
	 * @param temporalType Temporal type
	 * @return A new {@link LiteralValue}
	 */
	static LiteralValue create(Object value, TemporalType temporalType) {
		return new DefaultLiteralValue(value, temporalType);
	}

}
