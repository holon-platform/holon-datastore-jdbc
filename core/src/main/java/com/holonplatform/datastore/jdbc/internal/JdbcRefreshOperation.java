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
import com.holonplatform.core.datastore.operation.RefreshOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractRefreshOperation;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;

/**
 * JDBC {@link RefreshOperation}.
 *
 * @since 5.1.0
 */
public class JdbcRefreshOperation extends AbstractRefreshOperation {

	private static final long serialVersionUID = 1202760170834449222L;

	// Commodity factory
	@SuppressWarnings("serial")
	static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, RefreshOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, RefreshOperation>() {

		@Override
		public Class<? extends RefreshOperation> getCommodityType() {
			return RefreshOperation.class;
		}

		@Override
		public RefreshOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcRefreshOperation(context);
		}
	};

	private final JdbcStatementExecutionContext executionContext;

	public JdbcRefreshOperation(JdbcStatementExecutionContext executionContext) {
		super();
		this.executionContext = executionContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public PropertyBox execute() {

		// validate
		getConfiguration().validate();

		// resolution context
		final JdbcResolutionContext context = JdbcResolutionContext.create(executionContext, AliasMode.AUTO);

		return executionContext.withSharedConnection(() -> {

			// resolve primary key
			final TablePrimaryKey primaryKey = context
					.resolve(getConfiguration().getTarget(), TablePrimaryKey.class, context)
					.orElseThrow(() -> new DataAccessException(
							"Cannot obtain the primary key for target [" + getConfiguration().getTarget() + "]"));

			// execute using Query
			return executionContext.create(Query.class).target(getConfiguration().getTarget())
					.filter(JdbcDatastoreUtils.getPrimaryKeyFilter(executionContext, primaryKey,
							getConfiguration().getValue()))
					.findOne(getConfiguration().getValue())
					.orElseThrow(() -> new DataAccessException("No data found for primary key ["
							+ printPrimaryKey(primaryKey, getConfiguration().getValue()) + "]"));

		});
	}

	private String printPrimaryKey(TablePrimaryKey primaryKey, PropertyBox value) {

		final PathPropertyBoxAdapter adapter = PathPropertyBoxAdapter.builder(value)
				.pathMatcher(new DialectPathMatcher(executionContext.getDialect())).build();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < primaryKey.getKeys().length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(primaryKey.getKeys()[i].getName());
			sb.append("=");
			Object v = adapter.getValue(primaryKey.getKeys()[i]).orElse(null);
			if (v != null) {
				sb.append(v);
			} else {
				sb.append("[NULL]");
			}
		}
		return sb.toString();
	}

}
