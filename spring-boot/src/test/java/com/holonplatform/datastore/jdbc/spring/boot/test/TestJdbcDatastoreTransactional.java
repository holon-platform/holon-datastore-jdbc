/*
 * Copyright 2016-2019 Axioma srl.
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jdbc.spring.boot.test.services.TestService;
import com.holonplatform.datastore.jdbc.spring.boot.test.services.TestServiceImpl;

@SpringBootTest
@ActiveProfiles("p6")
public class TestJdbcDatastoreTransactional {

	// test1
	public final static DataTarget<String> TARGET1 = DataTarget.named("test1");
	public final static PathProperty<Long> KEY1 = PathProperty.create("keycode", Long.class);
	public final static PathProperty<String> STR1 = PathProperty.create("strv", String.class);
	public final static PropertySet<?> SET1 = PropertySet.builderOf(KEY1, STR1).withIdentifier(KEY1).build();

	// test2
	public final static DataTarget<String> TARGET2 = DataTarget.named("test2");
	public final static PathProperty<Long> KEY2 = PathProperty.create("keycode", Long.class);
	public final static PathProperty<String> STR2 = PathProperty.create("strv", String.class);
	public final static PropertySet<?> SET2 = PropertySet.builderOf(KEY2, STR2).withIdentifier(KEY2).build();

	@Configuration
	@EnableAutoConfiguration
	@ComponentScan(basePackageClasses = TestServiceImpl.class)
	protected static class Config {

	}

	@Autowired
	private TestService service;

	@Autowired
	private Datastore datastore;

	@Test
	public void testTransactional() {

		assertThrows(DataAccessException.class, () -> service.testSave(1L));

		String str = datastore.query(TARGET1).filter(KEY1.eq(1L)).findOne(STR1).orElse(null);
		assertNull(str);
		//assertEquals("STR1:1", str);

		str = datastore.query(TARGET2).filter(KEY2.eq(1L)).findOne(STR2).orElse(null);
		assertNull(str);
		//assertEquals("STR2:1", str);

	}

}
