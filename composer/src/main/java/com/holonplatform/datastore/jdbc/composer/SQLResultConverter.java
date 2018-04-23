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
import java.util.function.BiFunction;

import com.holonplatform.datastore.jdbc.composer.internal.CallbackSQLResultConverter;

/**
 * Converter to convert a {@link SQLResult} into another result type.
 * 
 * @param <R> Conversion result type
 *
 * @since 5.1.0
 */
public interface SQLResultConverter<R> {

	/**
	 * Get the type into which this converter is able to convert a {@link SQLResult}.
	 * @return The conversion type
	 */
	Class<? extends R> getConversionType();

	/**
	 * Convert a {@link SQLResult} into expected result type.
	 * @param context SQL execution context
	 * @param result The result to convert
	 * @return Converted result
	 * @throws SQLException If an error occurred
	 */
	R convert(SQLExecutionContext context, SQLResult result) throws SQLException;

	/**
	 * Create a new {@link SQLResultConverter} for given <code>conversionType</code>, using provided {@link BiFunction}
	 * as result conversion strategy.
	 * @param <R> Conversion result type
	 * @param conversionType Conversion type (not null)
	 * @param conversionFunction Conversion function (not null)
	 * @return A new {@link SQLResultConverter}
	 */
	static <R> SQLResultConverter<R> create(Class<? extends R> conversionType,
			BiFunction<SQLExecutionContext, SQLResult, R> conversionFunction) {
		return new CallbackSQLResultConverter<>(conversionType, conversionFunction);
	}

}
