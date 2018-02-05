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

import java.sql.PreparedStatement;

import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.bulk.BulkUpdate;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkUpdateOperation;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;

/**
 * JDBC datastore {@link BulkUpdate} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkUpdate extends AbstractBulkUpdateOperation<BulkUpdate> implements BulkUpdate {

	private static final long serialVersionUID = 1L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkUpdate> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkUpdate>() {

		@Override
		public Class<? extends BulkUpdate> getCommodityType() {
			return BulkUpdate.class;
		}

		@Override
		public BulkUpdate createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcBulkUpdate(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcBulkUpdate(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.datastore.bulk.AbstractBulkUpdateOperation#getActualOperation()
	 */
	@Override
	protected BulkUpdate getActualOperation() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// resolve
		final SQLStatement statement = context.resolveOrFail(getConfiguration(), SQLStatement.class);

		// trace
		operationContext.trace(statement.getSql());

		// execute
		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = operationContext.prepareStatement(statement, c)) {
				int count = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.UPDATE).affectedCount(count).build();
			}

		});

	}

}
