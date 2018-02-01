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
import com.holonplatform.core.datastore.bulk.BulkInsert;
import com.holonplatform.core.datastore.operation.InsertOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.datastore.operation.AbstractInsertOperation;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcExecutionContext;

/**
 * JDBC {@link InsertOperation}.
 *
 * @since 5.1.0
 */
public class JdbcInsert extends AbstractInsertOperation {

	private static final long serialVersionUID = -3547948214277724242L;

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, InsertOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, InsertOperation>() {

		@Override
		public Class<? extends InsertOperation> getCommodityType() {
			return InsertOperation.class;
		}

		@Override
		public InsertOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcInsert(context);
		}
	};

	private final JdbcExecutionContext executionContext;

	public JdbcInsert(JdbcExecutionContext executionContext) {
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
		try {
			getConfiguration().validate();
		} catch (InvalidExpressionException e) {
			throw new DataAccessException("Cannot execute operation", e);
		}
		
		// execute using a BulkInsert
		return executionContext.create(BulkInsert.class).target(getConfiguration().getTarget())
				.operationPaths(getConfiguration().getValue()).withWriteOptions(getConfiguration().getWriteOptions())
				.withExpressionResolvers(getConfiguration().getExpressionResolvers())
				.singleValue(getConfiguration().getValue()).execute();
		
	}

}
