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
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLValueDeserializer;

/**
 * {@link PropertyBox} result set converter.
 * 
 * @since 5.0.0
 */
public class PropertyBoxResultSetConverter extends AbstractResultSetConverter<PropertyBox> {

	/**
	 * Dialect
	 */
	private final JdbcDialect dialect;

	/**
	 * Property set to use
	 */
	private final PropertySet<?> propertySet;

	/**
	 * Query selection
	 */
	private final Map<String, Property<?>> propertySelection;

	/**
	 * Constructor
	 * @param dialect Dialect (not null)
	 * @param propertySet Property set to use (not null)
	 * @param propertySelection Query selection (not null)
	 */
	public PropertyBoxResultSetConverter(JdbcDialect dialect, PropertySet<?> propertySet,
			Map<String, Property<?>> propertySelection) {
		super();

		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");
		ObjectUtils.argumentNotNull(propertySet, "Property set must be not null");
		ObjectUtils.argumentNotNull(propertySelection, "Selection must be not null");

		this.dialect = dialect;
		this.propertySet = propertySet;
		this.propertySelection = propertySelection;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public PropertyBox convert(Connection connection, ResultSet resultSet) throws QueryResultConversionException {

		final SQLValueDeserializer deserializer = dialect.getValueDeserializer();

		try {
			PropertyBox.Builder builder = PropertyBox.builder(propertySet).invalidAllowed(true);
			for (Entry<String, Property<?>> entry : propertySelection.entrySet()) {
				builder.setIgnoreReadOnly((Property) entry.getValue(), deserializer.deserializeValue(connection,
						(QueryExpression<?>) entry.getValue(), getResult(dialect, resultSet, entry.getKey())));
			}
			return builder.build();
		} catch (SQLException e) {
			throw new QueryResultConversionException(e);
		}
	}

}
