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

import java.util.Optional;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.InsertOperation;
import com.holonplatform.core.datastore.operation.PropertyBoxOperationConfiguration;
import com.holonplatform.core.datastore.operation.SaveOperation;
import com.holonplatform.core.datastore.operation.UpdateOperation;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.operation.AbstractSaveOperation;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;

/**
 * JDBC {@link SaveOperation}.
 *
 * @since 5.1.0
 */
public class JdbcSave extends AbstractSaveOperation {

	private static final long serialVersionUID = -8341947663708669197L;

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, SaveOperation> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, SaveOperation>() {

		@Override
		public Class<? extends SaveOperation> getCommodityType() {
			return SaveOperation.class;
		}

		@Override
		public SaveOperation createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			return new JdbcSave(context);
		}
	};

	private final JdbcOperationContext operationContext;

	public JdbcSave(JdbcOperationContext operationContext) {
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

		/// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		return operationContext.withSharedConnection(() -> {

			// resolve primary key
			final Optional<SQLPrimaryKey> primaryKey = context.resolve(getConfiguration(), SQLPrimaryKey.class,
					context);

			if (!primaryKey.isPresent()) {
				LOGGER.warn("(Save operation) Cannot obtain the primary key for operation [" + getConfiguration()
						+ "]: an INSERT operation will be performed by default");
				return insert(getConfiguration());
			} else {
				// check existence using primary key
				try {
					QueryFilter pkFilter = JdbcOperationUtils.getPrimaryKeyFilter(operationContext.getDialect(),
							primaryKey.get(), getConfiguration().getValue());

					final Path<?> singleKey = (primaryKey.get().getPaths().length == 1) ? primaryKey.get().getPaths()[0]
							: null;
					Query q = operationContext.create(Query.class).target(getConfiguration().getTarget())
							.filter(pkFilter);
					boolean exists = ((singleKey != null) ? q.findOne(Count.create(singleKey)).orElse(0L)
							: q.count()) > 0;

					return exists ? update(getConfiguration()) : insert(getConfiguration());

				} catch (DataAccessException dae) {
					LOGGER.warn(
							"(Save operation) Cannot build a primary key filter: an INSERT operation will be performed by default ["
									+ dae.getMessage() + "]");
					return insert(getConfiguration());
				}
			}

		});
	}

	private OperationResult insert(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(InsertOperation.class).target(configuration.getTarget())
				.value(configuration.getValue()).withWriteOptions(configuration.getWriteOptions()).execute();
	}

	private OperationResult update(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(UpdateOperation.class).target(configuration.getTarget())
				.value(configuration.getValue()).withWriteOptions(configuration.getWriteOptions()).execute();
	}

}
