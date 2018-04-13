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
package com.holonplatform.datastore.jdbc.examples;

import javax.sql.DataSource;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.jdbc.DatabasePlatform;

@SuppressWarnings({ "unused", "serial" })
public class ExampleJdbcDatastoreExtension {

	// tag::commodity[]
	interface MyCommodity extends DatastoreCommodity { // <1>

		DatabasePlatform getPlatform();

	}

	class MyCommodityImpl implements MyCommodity { // <2>

		private final DatabasePlatform platform;

		public MyCommodityImpl(DatabasePlatform platform) {
			super();
			this.platform = platform;
		}

		@Override
		public DatabasePlatform getPlatform() {
			return platform;
		}

	}

	class MyCommodityFactory implements JdbcDatastoreCommodityFactory<MyCommodity> { // <3>

		@Override
		public Class<? extends MyCommodity> getCommodityType() {
			return MyCommodity.class;
		}

		@Override
		public MyCommodity createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			// examples of configuration attributes:
			DataSource dataSource = context.getDataSource();
			SQLDialect dialect = context.getDialect();
			return new MyCommodityImpl(context.getDatabase().orElse(DatabasePlatform.NONE));
		}

	}
	// end::commodity[]

	public void commodityFactory() {
		// tag::factoryreg[]
		Datastore datastore = JdbcDatastore.builder() //
				.withCommodity(new MyCommodityFactory()) // <1>
				.build();
		// end::factoryreg[]
	}

}
