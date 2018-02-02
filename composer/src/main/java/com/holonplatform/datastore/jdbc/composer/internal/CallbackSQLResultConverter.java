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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.sql.SQLException;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResult;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;

/**
 * A {@link SQLResultConverter} implementation which uses a callback {@link Function} to perform actual result
 * conversion.
 *
 * @param <R> Conversion result type
 *
 * @since 5.1.0
 */
public class CallbackSQLResultConverter<R> implements SQLResultConverter<R> {

	private final Class<? extends R> conversionType;
	private final BiFunction<SQLExecutionContext, SQLResult, R> conversionFunction;

	public CallbackSQLResultConverter(Class<? extends R> conversionType,
			BiFunction<SQLExecutionContext, SQLResult, R> conversionFunction) {
		super();
		ObjectUtils.argumentNotNull(conversionType, "Conversion type must be not null");
		ObjectUtils.argumentNotNull(conversionFunction, "Conversion function must be not null");
		this.conversionType = conversionType;
		this.conversionFunction = conversionFunction;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends R> getConversionType() {
		return conversionType;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLResultConverter#convert(com.holonplatform.datastore.jdbc.composer.
	 * SQLExecutionContext, com.holonplatform.datastore.jdbc.composer.SQLResult)
	 */
	@Override
	public R convert(SQLExecutionContext context, SQLResult result) throws SQLException {
		return conversionFunction.apply(context, result);
	}

}
