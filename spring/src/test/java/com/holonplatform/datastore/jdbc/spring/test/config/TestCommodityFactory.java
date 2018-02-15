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
package com.holonplatform.datastore.jdbc.spring.test.config;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.spring.DatastoreCommodityFactory;

@DatastoreCommodityFactory
public class TestCommodityFactory implements JdbcDatastoreCommodityFactory<TestCommodity> {

	private static final long serialVersionUID = 6536242494559220301L;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#getCommodityType()
	 */
	@Override
	public Class<? extends TestCommodity> getCommodityType() {
		return TestCommodity.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#createCommodity(com.holonplatform.core.datastore.
	 * DatastoreCommodityContext)
	 */
	@Override
	public TestCommodity createCommodity(JdbcDatastoreCommodityContext context) throws CommodityConfigurationException {
		return new TestCommodityImpl(context.getDataSource(), context.getDialect(), context.getIdentifierResolutionStrategy());
	}

}
