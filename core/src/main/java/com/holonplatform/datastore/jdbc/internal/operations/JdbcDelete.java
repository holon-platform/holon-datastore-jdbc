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

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.datastore.operation.DeleteOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractDeleteOperation;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;

/**
 * JDBC {@link DeleteOperation}.
 *
 * @since 5.1.0
 */
public class JdbcDelete extends AbstractDeleteOperation {

	private static final long serialVersionUID = 4155821525871792639L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, DeleteOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, DeleteOperation>() {

		@Override
		public Class<? extends DeleteOperation> getCommodityType() {
			return DeleteOperation.class;
		}

		@Override
		public DeleteOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcDelete(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcDelete(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		try {
			getConfiguration().validate();
		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Cannot execute operation", e);
		}

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		return operationContext.withSharedConnection(() -> {

			// resolve primary key
			final SQLPrimaryKey primaryKey = context.resolve(getConfiguration(), SQLPrimaryKey.class, context)
					.orElseThrow(() -> new DataAccessException(
							"Cannot obtain the primary key to use for operation [" + getConfiguration() + "]"));

			// execute using a BulkDelete
			return operationContext.create(BulkDelete.class).target(getConfiguration().getTarget())
					.withWriteOptions(getConfiguration().getWriteOptions())
					.withExpressionResolvers(getConfiguration().getExpressionResolvers())
					.filter(JdbcOperationUtils.getPrimaryKeyFilter(operationContext.getDialect(), primaryKey,
							getConfiguration().getValue()))
					.execute();

		});

	}

}
