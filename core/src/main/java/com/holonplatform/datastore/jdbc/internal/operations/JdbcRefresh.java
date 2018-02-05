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
package com.holonplatform.datastore.jdbc.internal.operations;

import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.RefreshOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractRefreshOperation;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.DialectPathMatcher;

/**
 * JDBC {@link RefreshOperation}.
 *
 * @since 5.1.0
 */
public class JdbcRefresh extends AbstractRefreshOperation {

	private static final long serialVersionUID = 1202760170834449222L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, RefreshOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, RefreshOperation>() {

		@Override
		public Class<? extends RefreshOperation> getCommodityType() {
			return RefreshOperation.class;
		}

		@Override
		public RefreshOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcRefresh(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcRefresh(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public PropertyBox execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		return operationContext.withSharedConnection(() -> {

			// resolve primary key
			final SQLPrimaryKey primaryKey = context.resolve(getConfiguration(), SQLPrimaryKey.class, context)
					.orElseThrow(() -> new DataAccessException(
							"Cannot obtain the primary key to use for operation [" + getConfiguration() + "]"));

			// execute using Query
			return operationContext.create(Query.class).target(getConfiguration().getTarget())
					.filter(JdbcOperationUtils.getPrimaryKeyFilter(operationContext.getDialect(), primaryKey,
							getConfiguration().getValue()))
					.findOne(getConfiguration().getValue())
					.orElseThrow(() -> new DataAccessException("No data found for primary key ["
							+ printPrimaryKey(primaryKey, getConfiguration().getValue()) + "]"));

		});
	}

	private String printPrimaryKey(SQLPrimaryKey primaryKey, PropertyBox value) {
		final PathPropertyBoxAdapter adapter = PathPropertyBoxAdapter.builder(value)
				.pathMatcher(new DialectPathMatcher(operationContext.getDialect())).build();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < primaryKey.getPaths().length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(primaryKey.getPaths()[i].getName());
			sb.append("=");
			Object v = adapter.getValue(primaryKey.getPaths()[i]).orElse(null);
			if (v != null) {
				sb.append(v);
			} else {
				sb.append("[NULL]");
			}
		}
		return sb.toString();
	}

}
