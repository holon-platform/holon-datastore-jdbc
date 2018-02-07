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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.LTMS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.expression.KeyIs;
import com.holonplatform.datastore.jdbc.test.function.CastFunction;
import com.holonplatform.jdbc.DataSourceBuilder;

public class JdbcDatastoreOracleIT extends AbstractJdbcDatastoreIT {

	private static Datastore datastore;

	@BeforeClass
	public static void initDatastore() {

		final DataSource dataSource = DataSourceBuilder.build("oracle/datasource.properties");
		initSQL(dataSource, "oracle/schema.sql", "oracle/data.sql");

		datastore = JdbcDatastore.builder().dataSource(dataSource).withExpressionResolver(KeyIs.RESOLVER)
				.withExpressionResolver(new CastFunction.Resolver()).traceEnabled(true).build();

	}

	@Override
	protected Datastore getDatastore() {
		return datastore;
	}

	// in Oracle, use the trunc() function timestamp
	@Override
	public void testLocalDateTimeWithTimestampFilter() {
		List<LocalDateTime> ltvalues = getDatastore().query().target(NAMED_TARGET)
				.filter(new CastFunction<>(LTMS, "date").eq(LocalDateTime.of(2017, Month.MARCH, 23, 15, 30, 25)))
				.list(LTMS);
		assertNotNull(ltvalues);
		assertEquals(1, ltvalues.size());
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
