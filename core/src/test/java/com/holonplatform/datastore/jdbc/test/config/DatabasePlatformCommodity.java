/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.test.config;

import java.util.Optional;

import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.jdbc.DatabasePlatform;

public class DatabasePlatformCommodity implements DatastoreCommodity {

	private static final long serialVersionUID = 8478847929033283098L;

	public static final JdbcDatastoreCommodityFactory<DatabasePlatformCommodity> FACTORY = new DatabasePlatformCommodityFactory();

	private final DatabasePlatform database;

	public DatabasePlatformCommodity(DatabasePlatform database) {
		super();
		this.database = database;
	}

	public Optional<DatabasePlatform> getDatabase() {
		return Optional.ofNullable(database);
	}

	@SuppressWarnings("serial")
	public static final class DatabasePlatformCommodityFactory
			implements JdbcDatastoreCommodityFactory<DatabasePlatformCommodity> {

		@Override
		public Class<? extends DatabasePlatformCommodity> getCommodityType() {
			return DatabasePlatformCommodity.class;
		}

		@Override
		public DatabasePlatformCommodity createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new DatabasePlatformCommodity(context.getDatabase().orElse(null));
		}

	}

}
