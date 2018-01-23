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
package com.holonplatform.datastore.jdbc.internal;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;

public class JdbcBulkUpdateFactory implements DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkUpdate> {

	private static final long serialVersionUID = 3132861778694395214L;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#getCommodityType()
	 */
	@Override
	public Class<? extends BulkUpdate> getCommodityType() {
		return BulkUpdate.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.DatastoreCommodityFactory#createCommodity(com.holonplatform.core.datastore.
	 * DatastoreCommodityContext)
	 */
	@Override
	public BulkUpdate createCommodity(JdbcDatastoreCommodityContext context) throws CommodityConfigurationException {
		return new JdbcBulkUpdate(context);
	}

}
