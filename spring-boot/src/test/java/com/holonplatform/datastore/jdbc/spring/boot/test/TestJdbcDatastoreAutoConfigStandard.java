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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.dialect.H2Dialect;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.jdbc.DatabasePlatform;

@SpringBootTest
@ActiveProfiles("p1")
public class TestJdbcDatastoreAutoConfigStandard {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

	final static DataTarget<String> NAMED_TARGET = DataTarget.named("test1");

	final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	final static PathProperty<String> STR1 = PathProperty.create("strv", String.class);

	@Autowired
	private JdbcDatastore datastore;

	@Transactional
	@Test
	public void testDatastore() {

		assertNotNull(datastore);

		assertEquals(DatabasePlatform.H2, ((JdbcDatastoreCommodityContext) datastore).getDatabase().orElse(null));
		assertTrue(((JdbcDatastoreCommodityContext) datastore).getDialect() instanceof H2Dialect);

		datastore.save(NAMED_TARGET, PropertyBox.builder(KEY, STR1).set(KEY, 7L).set(STR1, "Test ds (7)").build());

		Optional<Long> found = datastore.query().target(NAMED_TARGET).filter(KEY.eq(7L)).findOne(KEY);
		assertTrue(found.isPresent());

	}

}
