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
package com.holonplatform.datastore.jdbc.internal.converters;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;

import com.holonplatform.core.Path;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.BeanProjection;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLValueDeserializer;

/**
 * {@link BeanProjection} result set converter.
 * 
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public class BeanResultSetConverter<T> extends AbstractResultSetConverter<T> {

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Bean property set
	 */
	private final BeanPropertySet<T> beanPropertySet;

	/**
	 * Query selection
	 */
	private final Map<String, Path<?>> pathSelection;

	/**
	 * Constructor
	 * @param dialect Dialect (not null)
	 * @param beanPropertySet Bean property set (not null)
	 * @param pathSelection Query selection (not null)
	 */
	public BeanResultSetConverter(JdbcDialect dialect, BeanPropertySet<T> beanPropertySet,
			Map<String, Path<?>> pathSelection) {
		super();

		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		ObjectUtils.argumentNotNull(beanPropertySet, "Bean property set must be not null");
		ObjectUtils.argumentNotNull(pathSelection, "Selection must be not null");

		this.dialect = dialect;
		this.beanPropertySet = beanPropertySet;
		this.pathSelection = pathSelection;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public T convert(Connection connection, ResultSet resultSet) throws QueryResultConversionException {

		final SQLValueDeserializer deserializer = dialect.getValueDeserializer();

		try {

			T instance = beanPropertySet.getBeanClass().newInstance();

			for (Entry<String, Path<?>> entry : pathSelection.entrySet()) {
				beanPropertySet.write(
						(Path) entry.getValue(), deserializer.deserializeValue(connection,
								(QueryExpression<?>) entry.getValue(), getResult(dialect, resultSet, entry.getKey())),
						instance);
			}

			return instance;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new QueryResultConversionException("Unsupporterted bean projection type - bean class ["
					+ beanPropertySet.getBeanClass().getName() + "] must provide a public empty constructor", e);
		} catch (Exception e) {
			throw new QueryResultConversionException(
					"Failed to convert results using bean class [" + beanPropertySet.getBeanClass().getName() + "]", e);
		}
	}

}
