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
package com.holonplatform.datastore.jdbc.spring.boot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jdbc.JdbcDatastore;

@SpringBootTest
@ActiveProfiles("p3")
public class TestJdbcDatastoreAutoConfigMulti {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

	@Autowired
	@Qualifier("one")
	private JdbcDatastore datastore1;

	@Autowired
	@Qualifier("two")
	private JdbcDatastore datastore2;

	@Test
	public void testDataContext1() {
		assertNotNull(datastore1);

		long count = datastore1.query().target(DataTarget.named("testm1")).count();
		assertEquals(0, count);
	}

	@Test
	public void testDataContext2() {
		assertNotNull(datastore2);

		long count = datastore2.query().target(DataTarget.named("testm2")).count();
		assertEquals(0, count);
	}

}
