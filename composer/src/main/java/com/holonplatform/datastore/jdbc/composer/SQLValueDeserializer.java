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

import java.sql.Connection;
import java.sql.SQLException;

import com.holonplatform.core.Provider;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLValueDeserializer;

/**
 * SQL values deserializaer.
 *
 * @since 5.1.0
 */
public interface SQLValueDeserializer {

	/**
	 * Deserialize the <code>value</code> associated to given <code>expression</code>, to obtain a value type which
	 * matches the expression type.
	 * @param <T> Expression type
	 * @param connection Optional {@link Connection} provider
	 * @param expression Expression for which the deserialization is invoked
	 * @param value Value to deserialize
	 * @return Deserialized value
	 * @throws SQLException If value cannot be deserialized using given expression type
	 */
	<T> T deserialize(Provider<Connection> connection, TypedExpression<T> expression, Object value) throws SQLException;

	/**
	 * Add a deserialized value processor.
	 * @param valueProcessor the value processor to add (not null)
	 */
	void addValueProcessor(ValueProcessor valueProcessor);

	/**
	 * Create the default {@link SQLValueDeserializer}.
	 * @return the default {@link SQLValueDeserializer}
	 */
	static SQLValueDeserializer getDefault() {
		return DefaultSQLValueDeserializer.INSTANCE;
	}

	/**
	 * Processor to process a value before actual deserialization.
	 */
	@FunctionalInterface
	public interface ValueProcessor {

		/**
		 * Process a value to be deserialized.
		 * @param connection Connection provider
		 * @param expression Expression for which the deserialization is invoked
		 * @param value Value to deserialize
		 * @return Processed value
		 * @throws SQLException If an error occurred
		 */
		Object processValue(Provider<Connection> connection, TypedExpression<?> expression, Object value)
				throws SQLException;

	}

}
