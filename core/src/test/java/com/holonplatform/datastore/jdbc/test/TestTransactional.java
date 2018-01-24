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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.jdbc.DatabasePlatform;

public class TestTransactional {

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);
	
	private final static DataTarget<String> TARGET = DataTarget.named("testtx");
	
	private JdbcDatastore datastore;

	@Before
	public void initDatastore() {
		DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("tx")
				.addScript("h2/schema-tx.sql").addScript("h2/data-tx.sql").build();
		datastore = JdbcDatastore.builder().dataSource(dataSource).database(DatabasePlatform.H2).traceEnabled(true)
				.build();
	}
	
	@Test
	public void testTransactional() {
		
		long count = datastore.query().target(TARGET).count();
		Assert.assertEquals(1L, count);
		
		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);
			
			long cnt = datastore.query().target(TARGET).count();
			Assert.assertEquals(2L, cnt);
			
			tx.rollback();
		});
		
		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(1L, count);
		
		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);
			
			long cnt = datastore.query().target(TARGET).count();
			Assert.assertEquals(2L, cnt);
			
			tx.commit();
		});
		
		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(2L, count);
		
	}

}
