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

import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.query.QueryFilter;

/**
 * Represents the structure of a JDBC operation.
 *
 * @since 5.0.
 */
public interface OperationStructure extends Expression {

	/**
	 * Get the operation type.
	 * @return the operation type.
	 */
	OperationType getOperationType();

	/**
	 * Get the opration {@link DataTarget}.
	 * @return the operation target
	 */
	DataTarget<?> getTarget();

	/**
	 * Get the paths and their values associated to the operation.
	 * @return The path-value map, empty if not available
	 */
	Map<Path<?>, Object> getValues();

	/**
	 * Get the restrictions expressed as a {@link QueryFilter}.
	 * @return Optional operation filter
	 */
	Optional<QueryFilter> getFilter();

	/**
	 * Get an {@link OperationStructure} builder.
	 * @param operationType Operation type
	 * @param target Data target
	 * @return the builder
	 */
	static Builder builder(OperationType operationType, DataTarget<?> target) {
		return new DefaultOperationStructure.DefaultBuilder(operationType, target);
	}

	/**
	 * {@link OperationStructure} builder.
	 */
	public interface Builder {

		/**
		 * Add a value associated to given <code>path</code>.
		 * @param path Path (not null)
		 * @param value Value
		 * @return this
		 */
		Builder withValue(Path<?> path, Object value);

		/**
		 * Add a restriction filter.
		 * @param filter Filter to add
		 * @return this
		 */
		Builder withFilter(QueryFilter filter);

		/**
		 * Build the {@link OperationStructure}.
		 * @return OperationStructure instance
		 */
		OperationStructure build();

	}

}
