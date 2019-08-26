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
package com.holonplatform.datastore.jdbc.spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.test.config.TestCommodity;
import com.holonplatform.datastore.jdbc.spring.test.config.TestCommodityFactory;
import com.holonplatform.jdbc.spring.EnableDataSource;

@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestEnableJdbcDatastoreIdentifier.Config.class)
public class TestEnableJdbcDatastoreIdentifier {

	@Configuration
	@PropertySource("test.properties")
	@ComponentScan(basePackageClasses = TestCommodityFactory.class)
	@EnableTransactionManagement
	@EnableDataSource(enableTransactionManager = true)
	@EnableJdbcDatastore(identifierResolutionStrategy = IdentifierResolutionStrategy.IDENTIFIER_PROPERTIES)
	protected static class Config {

	}

	private final static DataTarget<String> NAMED_TARGET = DataTarget.named("testd");

	private final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	private final static PathProperty<String> STR = PathProperty.create("strv", String.class);

	private final static PropertySet<?> PROPS = PropertySet.builderOf(KEY, STR).withIdentifier(KEY).build();

	@Autowired
	private Datastore datastore;

	@Transactional
	@Test
	public void testIdentifierResolutionStrategy() {

		assertEquals(IdentifierResolutionStrategy.IDENTIFIER_PROPERTIES,
				datastore.create(TestCommodity.class).getIdentifierResolutionStrategy());

		OperationResult res = datastore.insert(NAMED_TARGET,
				PropertyBox.builder(KEY, STR).set(KEY, 787L).set(STR, "Test ids").build());
		assertEquals(1, res.getAffectedCount());

		PropertyBox data = datastore.query().target(NAMED_TARGET).filter(KEY.eq(787L)).findOne(PROPS).orElse(null);
		assertNotNull(data);

		data.setValue(STR, "*Test ids");

		res = datastore.update(NAMED_TARGET, data);
		assertEquals(1, res.getAffectedCount());

		String str = datastore.query().target(NAMED_TARGET).filter(KEY.eq(787L)).findOne(STR).orElse(null);
		assertEquals("*Test ids", str);

	}

	@Transactional
	@Test
	public void testIdentifierResolutionStrategyError() {
		final PropertySet<?> PROPS_NOID = PropertySet.of(KEY, STR);

		Assertions.assertThrows(DataAccessException.class, () -> {

			OperationResult res = datastore.insert(NAMED_TARGET,
					PropertyBox.builder(KEY, STR).set(KEY, 787L).set(STR, "Test ids").build());
			assertEquals(1, res.getAffectedCount());

			PropertyBox data = datastore.query().target(NAMED_TARGET).filter(KEY.eq(787L)).findOne(PROPS_NOID)
					.orElse(null);
			assertNotNull(data);

			data.setValue(STR, "*Test ids");

			datastore.update(NAMED_TARGET, data);

		});
	}

}
