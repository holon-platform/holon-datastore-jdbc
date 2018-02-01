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
package com.holonplatform.datastore.jdbc.composer.internal.converters;

import java.sql.Connection;
import java.sql.SQLException;

import com.holonplatform.core.Provider;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLResult;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;

/**
 * {@link TypedExpression} SQL result converter.
 * 
 * @param <T> Expression type
 * 
 * @since 5.0.0
 */
public class TypedExpressionSQLResultConverter<T> implements SQLResultConverter<T> {

	/**
	 * Selection query expression
	 */
	private final TypedExpression<T> expression;

	/**
	 * Selection label
	 */
	private final String selection;

	/**
	 * Constructor
	 * @param expression Selection expression (not null)
	 * @param selection Selection label (optional)
	 */
	public TypedExpressionSQLResultConverter(TypedExpression<T> expression, String selection) {
		super();
		ObjectUtils.argumentNotNull(expression, "Selection expression must be not null");
		this.expression = expression;
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLResultConverter#convert(com.holonplatform.datastore.jdbc.composer.
	 * SQLContext, com.holonplatform.core.Provider, com.holonplatform.datastore.jdbc.composer.SQLResult)
	 */
	@Override
	public T convert(SQLContext context, Provider<Connection> connection, SQLResult result) throws SQLException {

		final Object value = (selection != null) ? result.getValue(selection) : result.getValue(1);

		return context.getValueDeserializer().deserialize(connection, expression, value);
	}

}
