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
package com.holonplatform.datastore.jdbc.internal.converters;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;
import com.holonplatform.datastore.jdbc.JdbcDialect;

/**
 * {@link QueryExpression} result set converter.
 * 
 * @param <T> Expression type
 * 
 * @since 5.0.0
 */
public class QueryExpressionResultSetConverter<T> extends AbstractResultSetConverter<T> {

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Selection query expression
	 */
	private final QueryExpression<T> expression;

	/**
	 * Selection label
	 */
	private final String selection;

	/**
	 * Constructor
	 * @param dialect Dialect (not null)
	 * @param expression Selection query expression (not null)
	 * @param selection Selection label (not null)
	 */
	public QueryExpressionResultSetConverter(JdbcDialect dialect, QueryExpression<T> expression, String selection) {
		super();

		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		ObjectUtils.argumentNotNull(expression, "Selection expression must be not null");
		ObjectUtils.argumentNotNull(selection, "Selection label must be not null");

		this.dialect = dialect;
		this.expression = expression;
		this.selection = selection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convert(ResultSet resultSet) throws QueryResultConversionException {

		try {
			final Object value = dialect.getValueDeserializer().deserializeValue(expression,
					getResult(dialect, resultSet, selection));

			// check type
			if (value != null && !TypeUtils.isAssignable(value.getClass(), expression.getType())) {
				throw new QueryResultConversionException("Expected a value of projection type ["
						+ expression.getType().getName() + "], got a value of type: " + value.getClass().getName());
			}

			return (T) value;

		} catch (SQLException e) {
			throw new QueryResultConversionException(
					"Failed to convert query result for expression [" + expression + "]", e);
		}
	}

}
