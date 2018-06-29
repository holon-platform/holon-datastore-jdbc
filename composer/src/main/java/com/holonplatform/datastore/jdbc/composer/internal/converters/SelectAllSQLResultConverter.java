/*
 * Copyright 2016-2018 Axioma srl.
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
import java.util.HashMap;
import java.util.Map;

import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResult;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;

/**
 * Select all {@link SQLResultConverter}.
 *
 * @since 5.2.0
 */
public class SelectAllSQLResultConverter implements SQLResultConverter<Map<String, Object>> {

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResultConverter#getConversionType()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Map<String, Object>> getConversionType() {
		return (Class<? extends Map<String, Object>>) (Class<?>) Map.class;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLResultConverter#convert(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext, com.holonplatform.datastore.jdbc.composer.SQLResult)
	 */
	@Override
	public Map<String, Object> convert(SQLExecutionContext context, SQLResult result) throws SQLException {

		final int count = result.getValueCount();

		final Map<String, Object> map = new HashMap<>(count);
		for (int i = 1; i <= count; i++) {
			final String name = result.getValueName(i).orElse(null);
			if (name != null) {
				map.put(name, result.getValue(i));
			}
		}
		return map;
	}

}
