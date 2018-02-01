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
package com.holonplatform.datastore.jdbc.composer;

import com.holonplatform.core.ExpressionResolver.ExpressionResolverProvider;

/**
 * SQL composer base context.
 *
 * @since 5.1.0
 */
public interface SQLContext extends ExpressionResolverProvider {

	/**
	 * Get the {@link SQLDialect} to use.
	 * @return The SQL dialect
	 */
	SQLDialect getDialect();

	/**
	 * Get the {@link SQLValueSerializer} if this context.
	 * @return The {@link SQLValueSerializer}
	 */
	default SQLValueSerializer getValueSerializer() {
		return SQLValueSerializer.getDefault();
	}

	/**
	 * Get the {@link SQLValueDeserializer} if this context.
	 * @return the {@link SQLValueDeserializer}
	 */
	default SQLValueDeserializer getValueDeserializer() {
		return SQLValueDeserializer.getDefault();
	}

	/**
	 * Get the {@link SQLTypeConverter}.
	 * @return the {@link SQLTypeConverter}
	 */
	default SQLTypeConverter getTypeConverter() {
		return SQLTypeConverter.getDefault();
	}

	/**
	 * Trace given SQL statement.
	 * <p>
	 * If tracing is enabled, the SQL statement is logged using the <code>INFO</code> level, otherwise it is logged
	 * using the <code>DEBUG</code> level.
	 * </p>
	 * @param sql SQL to trace
	 */
	void trace(String sql);

}
