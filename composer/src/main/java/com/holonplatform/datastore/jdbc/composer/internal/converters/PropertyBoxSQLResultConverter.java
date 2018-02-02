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

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResult;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;

/**
 * {@link PropertyBox} SQL result converter.
 * 
 * @since 5.0.0
 */
public class PropertyBoxSQLResultConverter implements SQLResultConverter<PropertyBox> {

	/**
	 * Property set to use
	 */
	private final PropertySet<?> propertySet;

	/**
	 * Selection properties
	 */
	private final Map<String, Property<?>> selectionProperties;

	/**
	 * Selection properties
	 */
	private final Map<String, TypedExpression<?>> selectionExpressions;

	/**
	 * Constructor
	 * @param propertySet Property set to use (not null)
	 * @param propertySelection Query selection (not null)
	 */
	public PropertyBoxSQLResultConverter(PropertySet<?> propertySet, Map<String, Property<?>> selectionProperties,
			Map<String, TypedExpression<?>> selectionExpressions) {
		super();
		ObjectUtils.argumentNotNull(propertySet, "Property set must be not null");
		ObjectUtils.argumentNotNull(selectionProperties, "Selection properties must be not null");
		ObjectUtils.argumentNotNull(selectionExpressions, "Selection expressions must be not null");
		this.propertySet = propertySet;
		this.selectionProperties = selectionProperties;
		this.selectionExpressions = selectionExpressions;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends PropertyBox> getConversionType() {
		return PropertyBox.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLResultConverter#convert(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext, com.holonplatform.datastore.jdbc.composer.SQLResult)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public PropertyBox convert(SQLExecutionContext context, SQLResult result) throws SQLException {

		final SQLValueDeserializer deserializer = context.getValueDeserializer();

		// build the PropertyBox
		PropertyBox.Builder builder = PropertyBox.builder(propertySet).invalidAllowed(true);

		// set values form selections
		for (Entry<String, Property<?>> entry : selectionProperties.entrySet()) {
			TypedExpression<?> expression = selectionExpressions.get(entry.getKey());
			if (expression == null) {
				throw new SQLException("No selection expression available for selection [" + entry.getKey() + "]");
			}
			// result value
			Object value = result.getValue(entry.getKey());
			// deserialize value
			Object deserialized = deserializer.deserialize(context, expression, value);
			// set property value
			builder.setIgnoreReadOnly((Property<Object>) entry.getValue(), deserialized);
		}

		return builder.build();
	}

}
