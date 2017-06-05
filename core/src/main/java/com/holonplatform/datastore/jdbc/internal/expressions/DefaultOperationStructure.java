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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.QueryFilter;

/**
 * Default {@link OperationStructure} implementation.
 *
 * @since 5.0.0
 */
public class DefaultOperationStructure implements OperationStructure {

	/**
	 * Operation type
	 */
	private final OperationType operationType;

	/**
	 * Data target
	 */
	private final DataTarget<?> target;

	/**
	 * Path values
	 */
	private final Map<Path<?>, Object> values = new LinkedHashMap<>();

	/**
	 * Restrictions
	 */
	private QueryFilter filter;

	/**
	 * Constructor
	 * @param operationType Operation type
	 * @param target Data target
	 */
	public DefaultOperationStructure(OperationType operationType, DataTarget<?> target) {
		super();
		this.operationType = operationType;
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure#getOperationType()
	 */
	@Override
	public OperationType getOperationType() {
		return operationType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure#getTarget()
	 */
	@Override
	public DataTarget<?> getTarget() {
		return target;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure#getValues()
	 */
	@Override
	public Map<Path<?>, Object> getValues() {
		return values;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure#getFilter()
	 */
	@Override
	public Optional<QueryFilter> getFilter() {
		return Optional.ofNullable(filter);
	}

	/**
	 * Add a value associated to given <code>path</code>.
	 * @param path Path (not null)
	 * @param value Value
	 */
	public void addValue(Path<?> path, Object value) {
		ObjectUtils.argumentNotNull(path, "Path must be not null");
		values.put(path, value);
	}

	/**
	 * Set the operation filter.
	 * @param filter the filter to set
	 */
	public void setFilter(QueryFilter filter) {
		this.filter = filter;
	}

	/**
	 * Add a filter to the current operation filters.
	 * @param filter The filter to add
	 */
	public void addFilter(QueryFilter filter) {
		if (filter != null) {
			if (this.filter == null) {
				this.filter = filter;
			} else {
				this.filter = this.filter.and(filter);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getOperationType() == null) {
			throw new InvalidExpressionException("Missing operation type");
		}
		if (getTarget() == null) {
			throw new InvalidExpressionException("Missing data target");
		}

		if (getOperationType() == OperationType.INSERT || getOperationType() == OperationType.UPDATE) {
			if (getValues() == null || getValues().isEmpty()) {
				throw new InvalidExpressionException("Null or empty path values");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultOperationStructure [operationType=" + operationType + ", target=" + target + ", values=" + values
				+ ", filter=" + filter + "]";
	}

	/**
	 * Default {@link Builder} implementation.
	 */
	public static class DefaultBuilder implements Builder {

		/**
		 * Instance to build
		 */
		private final DefaultOperationStructure instance;

		/**
		 * Constructor
		 * @param operationType Operation type
		 * @param target Data target
		 */
		public DefaultBuilder(OperationType operationType, DataTarget<?> target) {
			super();
			instance = new DefaultOperationStructure(operationType, target);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure.Builder#withValue(com.holonplatform.
		 * core.Path, java.lang.Object)
		 */
		@Override
		public Builder withValue(Path<?> path, Object value) {
			instance.addValue(path, value);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure.Builder#withFilter(com.holonplatform
		 * .core.query.QueryFilter)
		 */
		@Override
		public Builder withFilter(QueryFilter filter) {
			instance.addFilter(filter);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.expressions.OperationStructure.Builder#build()
		 */
		@Override
		public OperationStructure build() {
			return instance;
		}

	}

}
