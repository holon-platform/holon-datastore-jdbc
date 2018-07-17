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

import java.sql.SQLException;
import java.util.Optional;

import com.holonplatform.core.ConstantConverterExpression;
import com.holonplatform.core.TypedExpression;

/**
 * Represents the result of a SQL operation, with methods to provide the result values.
 *
 * @since 5.1.0
 */
public interface SQLResult {

	/**
	 * Get the value at given index.
	 * @param index Result value index, starting from <code>1</code>.
	 * @return The result value
	 * @throws SQLException Error retrieving the result value
	 */
	Object getValue(int index) throws SQLException;

	/**
	 * Get the value with given name.
	 * @param name Resut value name (not null)
	 * @return The result value
	 * @throws SQLException Error retrieving the result value
	 */
	Object getValue(String name) throws SQLException;

	/**
	 * Get the number of available result values.
	 * @return the number of available result values
	 * @throws SQLException If an error occurred
	 */
	int getValueCount() throws SQLException;

	/**
	 * Get the result value name at given index, if available.
	 * @param index Result value index, starting from <code>1</code>
	 * @return Optional result value name
	 * @throws SQLException If an error occurred
	 */
	Optional<String> getValueName(int index) throws SQLException;

	/**
	 * Get the result value with given <code>name</code>, using given <code>expression</code> to provide the expected
	 * value type and any other value deserialization strategy element, such as a converter, which can be used by
	 * current {@link SQLValueDeserializer}.
	 * @param <T> Expected value type
	 * @param context SQL execution context (not null)
	 * @param expression Expression (not null)
	 * @param name Result value name (not null)
	 * @return The result value
	 * @throws SQLException If an error occurred
	 */
	default <T> T getValue(SQLExecutionContext context, TypedExpression<T> expression, String name)
			throws SQLException {
		return context.getValueDeserializer().deserialize(context, expression, getValue(name));
	}

	/**
	 * Get the result value at given <code>index</code>, using given <code>expression</code> to provide the expected
	 * value type and any other value deserialization strategy element, such as a converter, which can be used by
	 * current {@link SQLValueDeserializer}.
	 * @param <T> Expected value type
	 * @param context SQL execution context (not null)
	 * @param expression Expression (not null)
	 * @param index Result value index, starting from <code>1</code>.
	 * @return The result value
	 * @throws SQLException If an error occurred
	 */
	default <T> T getValue(SQLExecutionContext context, TypedExpression<T> expression, int index) throws SQLException {
		return context.getValueDeserializer().deserialize(context, expression, getValue(index));
	}

	/**
	 * Get the result value with given <code>name</code> using current {@link SQLValueDeserializer} to obtain the value
	 * in the expected type.
	 * @param <T> Expected value type
	 * @param context SQL execution context (not null)
	 * @param name Result value name (not null)
	 * @return The result value
	 * @throws SQLException If an error occurred
	 */
	@SuppressWarnings("unchecked")
	default <T> T getValue(SQLExecutionContext context, String name) throws SQLException {
		final Object value = getValue(name);
		return (T) context.getValueDeserializer().deserialize(context, ConstantConverterExpression.create(value), value);
	}

	/**
	 * Get the result value at given <code>index</code> using current {@link SQLValueDeserializer} to obtain the value
	 * in the expected type.
	 * @param <T> Expected value type
	 * @param context SQL execution context (not null)
	 * @param index Result value index, starting from <code>1</code>
	 * @return The result value
	 * @throws SQLException If an error occurred
	 */
	@SuppressWarnings("unchecked")
	default <T> T getValue(SQLExecutionContext context, int index) throws SQLException {
		final Object value = getValue(index);
		return (T) context.getValueDeserializer().deserialize(context, ConstantConverterExpression.create(value), value);
	}

}
