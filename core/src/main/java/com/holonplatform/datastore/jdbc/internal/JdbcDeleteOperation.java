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
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.operation.DeleteOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractDeleteOperation;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.internal.context.JdbcStatementExecutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.AliasMode;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;

/**
 * JDBC {@link DeleteOperation}.
 *
 * @since 5.1.0
 */
public class JdbcDeleteOperation extends AbstractDeleteOperation {

	private static final long serialVersionUID = 4155821525871792639L;

	// Commodity factory
	@SuppressWarnings("serial")
	static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, DeleteOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, DeleteOperation>() {

		@Override
		public Class<? extends DeleteOperation> getCommodityType() {
			return DeleteOperation.class;
		}

		@Override
		public DeleteOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcDeleteOperation(context);
		}
	};

	private final JdbcStatementExecutionContext executionContext;

	public JdbcDeleteOperation(JdbcStatementExecutionContext executionContext) {
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
			final TablePrimaryKey primaryKey = context
					.resolve(getConfiguration().getTarget(), TablePrimaryKey.class, context)
					.orElseThrow(() -> new DataAccessException(
							"Cannot obtain the primary key for target [" + getConfiguration().getTarget() + "]"));

			// execute using a BulkDelete
			return executionContext.create(BulkDelete.class).target(getConfiguration().getTarget())
					.withWriteOptions(getConfiguration().getWriteOptions()).filter(JdbcDatastoreUtils
							.getPrimaryKeyFilter(executionContext, primaryKey, getConfiguration().getValue()))
					.execute();

		});
	}

}
