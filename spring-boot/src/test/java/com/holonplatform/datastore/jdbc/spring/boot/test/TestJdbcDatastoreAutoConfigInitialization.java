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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.spring.ScopeTenant;

@SpringBootTest
@ActiveProfiles("p1")
public class TestJdbcDatastoreAutoConfigInitialization {

	private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();
	
	final static DataTarget<String> NAMED_TARGET = DataTarget.named("test1");

	final static PathProperty<Long> KEY = PathProperty.create("keycode", long.class);
	final static PathProperty<String> STR = PathProperty.create("strv", String.class);

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

		@Bean
		public TenantResolver tenantResolver() {
			return () -> Optional.ofNullable(CURRENT_TENANT_ID.get());
		}

		@ScopeTenant
		@Bean
		public JdbcDatastore datastore(TenantResolver tenantResolver, DataSource dataSource) {
			tenantResolver.getTenantId().orElseThrow(() -> new IllegalStateException("Missing tenant id"));
			return JdbcDatastore.builder().dataSource(dataSource).build();
		}

	}

	@Autowired
	private ApplicationContext appCtx;

	@Test
	public void testConfig() {
		try {
			CURRENT_TENANT_ID.set("X");
			Datastore ds = appCtx.getBean(Datastore.class);
			assertNotNull(ds);
			
			ds.query().target(NAMED_TARGET).list(KEY);
		} finally {
			CURRENT_TENANT_ID.set(null);
		}
	}

}
