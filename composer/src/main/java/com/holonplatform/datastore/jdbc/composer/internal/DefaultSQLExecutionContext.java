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
package com.holonplatform.datastore.jdbc.composer.internal;

import java.sql.Connection;
import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jdbc.composer.SQLContext;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;

/**
 * Default {@link SQLExecutionContext}.
 *
 * @since 5.1.0
 */
public class DefaultSQLExecutionContext implements SQLExecutionContext {

	private final SQLContext sqlContext;
	private final Connection connection;

	/**
	 * Constructor.
	 * @param sqlContext SQL context (not null)
	 * @param connection Optional connection
	 */
	public DefaultSQLExecutionContext(SQLContext sqlContext, Connection connection) {
		super();
		this.sqlContext = sqlContext;
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#getDialect()
	 */
	@Override
	public SQLDialect getDialect() {
		return sqlContext.getDialect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLContext#trace(java.lang.String)
	 */
	@Override
	public void trace(String sql) {
		sqlContext.trace(sql);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver.ExpressionResolverProvider#getExpressionResolvers()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<ExpressionResolver> getExpressionResolvers() {
		return sqlContext.getExpressionResolvers();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLExecutionContext#getConnection()
	 */
	@Override
	public Optional<Connection> getConnection() {
		return Optional.ofNullable(connection);
	}

}
