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

import java.util.Optional;

import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLTypeConverter;

/**
 * Converter to perform SQL type to Java type conversions and back.
 * <p>
 * The {@link Void} class should be used to represent a <code>null</code> type.
 * </p>
 *
 * @since 5.1.0
 */
public interface SQLTypeConverter {

	/**
	 * Get the Java type which corresponds to given SQL type.
	 * @param context SQL context
	 * @param sqlType The sql type (not null)
	 * @return Optional Java type, empty if it cannot be resolved
	 */
	Optional<Class<?>> getJavaType(SQLContext context, SQLType sqlType);

	/**
	 * Get the SQL type which corresponds to given Java type.
	 * @param context SQL context
	 * @param javaType The sql type (not null)
	 * @return Optional SQL type, empty if it cannot be resolved
	 */
	Optional<SQLType> getSqlType(SQLContext context, Class<?> javaType);

	/**
	 * Get the default {@link SQLTypeConverter}.
	 * @return the default {@link SQLTypeConverter}
	 */
	static SQLTypeConverter getDefault() {
		return DefaultSQLTypeConverter.INSTANCE;
	}

}
