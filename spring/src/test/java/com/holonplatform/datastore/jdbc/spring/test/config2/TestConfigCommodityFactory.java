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
package com.holonplatform.datastore.jdbc.spring.test.config2;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.spring.DatastoreCommodityFactory;

@DatastoreCommodityFactory
public class TestConfigCommodityFactory implements JdbcDatastoreCommodityFactory<TestConfigCommodity> {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#createCommodity(com.holonplatform.core.datastore.
	 * DatastoreCommodityContext)
	 */
	@Override
	public TestConfigCommodity createCommodity(JdbcDatastoreCommodityContext ctx)
			throws CommodityConfigurationException {
		return new TestConfigCommodity(ctx.getDatabase().orElse(null));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#getCommodityType()
	 */
	@Override
	public Class<? extends TestConfigCommodity> getCommodityType() {
		return TestConfigCommodity.class;
	}

}
