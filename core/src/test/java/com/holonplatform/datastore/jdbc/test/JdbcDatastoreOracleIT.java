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
package com.holonplatform.datastore.jdbc.test;

import javax.sql.DataSource;

import org.junit.BeforeClass;

import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.data.KeyIs;
import com.holonplatform.jdbc.DataSourceBuilder;

public class JdbcDatastoreOracleIT extends AbstractJdbcDatastoreIT {

	private static JdbcDatastore datastore;

	@BeforeClass
	public static void initDatastore() {

		final DataSource dataSource = DataSourceBuilder.build("oracle/datasource.properties");
		initSQL(dataSource, "oracle/schema.sql", "oracle/data.sql");

		datastore = JdbcDatastore.builder().dataSource(dataSource).withExpressionResolver(KeyIs.RESOLVER)
				.traceEnabled(true).build();

	}

	@Override
	protected JdbcDatastore getDatastore() {
		return datastore;
	}

	@Override
	public void testLocalDateTimeWithTimestampFilter() {
		// in Oracle the trunc() function is required for timestamp
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.test.AbstractJdbcDatastoreTest#testTimeFilter()
	 */
	@Override
	public void testTimeFilter() {
		// Oracle does not support TIME data type
	}

}
