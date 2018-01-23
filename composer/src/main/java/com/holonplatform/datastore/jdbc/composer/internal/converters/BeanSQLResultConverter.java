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
import java.util.Map;
import java.util.Map.Entry;

import com.holonplatform.core.Path;
import com.holonplatform.core.Provider;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLResult;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.SQLValueDeserializer;

/**
 * Bean SQL result converter.
 * 
 * @param <T> Bean type
 * 
 * @since 5.0.0
 */
public class BeanSQLResultConverter<T> implements SQLResultConverter<T> {

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
	public BeanSQLResultConverter(BeanPropertySet<T> beanPropertySet, Map<String, Path<?>> pathSelection) {
		super();
		ObjectUtils.argumentNotNull(beanPropertySet, "Bean property set must be not null");
		ObjectUtils.argumentNotNull(pathSelection, "Selection must be not null");
		this.beanPropertySet = beanPropertySet;
		this.pathSelection = pathSelection;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLResultConverter#convert(com.holonplatform.datastore.jdbc.composer.
	 * SQLContext, com.holonplatform.core.Provider, com.holonplatform.datastore.jdbc.composer.SQLResult)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public T convert(SQLContext context, Provider<Connection> connection, SQLResult result) throws SQLException {

		final SQLValueDeserializer deserializer = context.getValueDeserializer();

		T instance;
		try {
			instance = beanPropertySet.getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new SQLException("Failed to istantiate bean class [" + beanPropertySet.getBeanClass() + "]", e);
		}

		for (Entry<String, Path<?>> entry : pathSelection.entrySet()) {
			// result value
			Object value = result.getValue(entry.getKey());
			// deserialize value
			Object deserialized = deserializer.deserialize(connection, entry.getValue(), value)
					.orElseThrow(() -> new SQLException("Failed to deserialize value [" + value + "]"));
			// write value in bean instance
			beanPropertySet.write((Path) entry.getValue(), deserialized, instance);
		}

		return instance;
	}

}