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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.test.config.KeyOne;
import com.holonplatform.jdbc.DataSourceBuilder;

public class ExpressionResolverRegistrationUT {

	private Datastore datastore;

	@BeforeEach
	public void initDatastore() {
		DataSource dataSource = DataSourceBuilder.builder().url("jdbc:h2:mem:rslv").username("sa")
				.withInitScriptResource("h2/schema.sql").withInitScriptResource("h2/data.sql").build();
		datastore = JdbcDatastore.builder().dataSource(dataSource).build();
	}

	@Test
	public void testResolver() {

		String str = datastore.query(NAMED_TARGET).filter(new KeyOne()).findOne(STR1).orElse(null);

		assertEquals("One", str);
	}

}
