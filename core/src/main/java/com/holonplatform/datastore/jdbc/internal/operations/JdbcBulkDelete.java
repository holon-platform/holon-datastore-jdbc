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
import com.holonplatform.core.datastore.bulk.BulkDelete;
import com.holonplatform.core.internal.datastore.bulk.AbstractBulkDelete;
import com.holonplatform.core.internal.datastore.operation.common.DeleteOperationConfiguration;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;

/**
 * JDBC datastore {@link BulkDelete} implementation.
 * 
 * @since 5.0.0
 */
public class JdbcBulkDelete extends AbstractBulkDelete {

	private static final long serialVersionUID = 1L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkDelete> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, BulkDelete>() {

		@Override
		public Class<? extends BulkDelete> getCommodityType() {
			return BulkDelete.class;
		}

		@Override
		public BulkDelete createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcBulkDelete(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcBulkDelete(JdbcOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.bulk.DMLClause#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// create operation configuration
		final DeleteOperationConfiguration configuration = DeleteOperationConfiguration.builder()
				.target(getConfiguration().getTarget()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers())
				.filter(getConfiguration().getFilter().orElse(null)).build();

		// resolve
		final SQLStatement statement = context.resolveOrFail(configuration, SQLStatement.class);

		// trace
		operationContext.trace(statement.getSql());

		// execute
		return operationContext.withConnection(c -> {

			try (PreparedStatement stmt = operationContext.prepareStatement(statement, c)) {
				int count = stmt.executeUpdate();
				return OperationResult.builder().type(OperationType.DELETE).affectedCount(count).build();
			}

		});

	}

}
