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

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.datastore.operation.UpdateOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractUpdateOperation;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;

/**
 * JDBC {@link UpdateOperation}.
 *
 * @since 5.1.0
 */
public class JdbcUpdateOperation extends AbstractUpdateOperation {

	private static final long serialVersionUID = 7143507117624707335L;

	// Commodity factory
	@SuppressWarnings("serial")
	static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, UpdateOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, UpdateOperation>() {

		@Override
		public Class<? extends UpdateOperation> getCommodityType() {
			return UpdateOperation.class;
		}

		@Override
		public UpdateOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcUpdateOperation(context);
		}
	};

	private final JdbcStatementExecutionContext executionContext;

	public JdbcUpdateOperation(JdbcStatementExecutionContext executionContext) {
		super();
		this.executionContext = executionContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// resolution context
		final JdbcResolutionContext context = JdbcResolutionContext.create(executionContext, AliasMode.AUTO);

		return executionContext.withSharedConnection(() -> {

			// resolve primary key
			final SQLPrimaryKey primaryKey = context
					.resolve(getConfiguration().getTarget(), SQLPrimaryKey.class, context)
					.orElseThrow(() -> new DataAccessException(
							"Cannot obtain the primary key for target [" + getConfiguration().getTarget() + "]"));

			// execute using a BulkUpdate
			return executionContext.create(BulkUpdate.class).target(getConfiguration().getTarget())
					.withWriteOptions(getConfiguration().getWriteOptions()).set(getConfiguration().getValue())
					.filter(JdbcDatastoreUtils.getPrimaryKeyFilter(executionContext, primaryKey,
							getConfiguration().getValue()))
					.execute();

		});
	}

}
