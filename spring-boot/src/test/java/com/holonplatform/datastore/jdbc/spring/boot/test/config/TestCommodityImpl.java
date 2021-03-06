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
package com.holonplatform.datastore.jdbc.spring.boot.test.config;

import javax.sql.DataSource;

import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;

@SuppressWarnings("serial")
public class TestCommodityImpl implements TestCommodity {

	private final DataSource dataSource;
	private final SQLDialect dialect;

	private final IdentifierResolutionStrategy identifierResolutionStrategy;

	public TestCommodityImpl(DataSource dataSource, SQLDialect dialect,
			IdentifierResolutionStrategy identifierResolutionStrategy) {
		super();
		this.dataSource = dataSource;
		this.dialect = dialect;
		this.identifierResolutionStrategy = identifierResolutionStrategy;
	}

	@Override
	public IdentifierResolutionStrategy getIdentifierResolutionStrategy() {
		return identifierResolutionStrategy;
	}

	@Override
	public void test() {
		if (dataSource == null) {
			throw new IllegalStateException("Null DataSource");
		}
		if (dialect == null) {
			throw new IllegalStateException("Null Dialect");
		}
	}

}
