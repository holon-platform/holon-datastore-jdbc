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

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.operation.Insert;
import com.holonplatform.core.datastore.operation.Save;
import com.holonplatform.core.datastore.operation.Update;
import com.holonplatform.core.datastore.operation.commons.PropertyBoxOperationConfiguration;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.operation.AbstractSave;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLPrimaryKey;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.context.JdbcOperationContext;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;
import com.holonplatform.datastore.jdbc.internal.support.JdbcOperationUtils;

/**
 * JDBC {@link Save}.
 *
 * @since 5.1.0
 */
public class JdbcSave extends AbstractSave {

	private static final long serialVersionUID = -8341947663708669197L;

	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Save> FACTORY = new DatastoreCommodityFactory<JdbcDatastoreCommodityContext, Save>() {

		@Override
		public Class<? extends Save> getCommodityType() {
			return Save.class;
		}

		@Override
		public Save createCommodity(JdbcDatastoreCommodityContext context) throws CommodityConfigurationException {
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
		getConfiguration().validate();

		/// composition context
		final SQLCompositionContext context = SQLCompositionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		return operationContext.withSharedConnection(() -> {

			final boolean fallbackToInsert = !getConfiguration()
					.hasWriteOption(DefaultWriteOption.SAVE_DISABLE_INSERT_FALLBACK);

			// resolve primary key
			final Optional<SQLPrimaryKey> primaryKey = context.resolve(getConfiguration(), SQLPrimaryKey.class,
					context);

			if (!primaryKey.isPresent()) {
				if (fallbackToInsert) {
					LOGGER.warn("(Save operation) Cannot obtain the primary key for operation [" + getConfiguration()
							+ "]: an INSERT operation will be performed by default");
					return insert(getConfiguration());
				} else {
					throw new DataAccessException(
							"Failed to perform a consistent SAVE operation: cannot obtain the primary key to use for operation ["
									+ getConfiguration() + "]");
				}
			} else {
				// check existence using primary key
				final boolean exists;
				try {
					QueryFilter pkFilter = JdbcOperationUtils.getPrimaryKeyFilter(operationContext.getDialect(),
							primaryKey.get(), getConfiguration().getValue());

					final Path<?> singleKey = (primaryKey.get().getPaths().length == 1) ? primaryKey.get().getPaths()[0]
							: null;
					Query q = operationContext.create(Query.class).target(getConfiguration().getTarget())
							.filter(pkFilter);
					exists = ((singleKey != null) ? q.findOne(Count.create(singleKey)).orElse(0L) : q.count()) > 0;
				} catch (DataAccessException dae) {
					if (fallbackToInsert) {
						LOGGER.warn(
								"(Save operation) Failed to check value existence using primary key: an INSERT operation will be performed by default ["
										+ dae.getMessage() + "]");
						LOGGER.debug(() -> "(Save operation) Failed to check value existence using primary key", dae);
						return insert(getConfiguration());
					} else {
						throw new DataAccessException(
								"Failed to perform a consistent SAVE operation: cannot check value existence using primary",
								dae);
					}
				}

				// perform an update or insert operation according to key existence
				return exists ? update(getConfiguration()) : insert(getConfiguration());
			}

		});
	}

	/**
	 * Perform an insert operation using given <code>configuration</code>.
	 * @param configuration Operation configuration
	 * @return Operation result
	 */
	private OperationResult insert(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(Insert.class).target(configuration.getTarget()).value(configuration.getValue())
				.withWriteOptions(configuration.getWriteOptions()).execute();
	}

	/**
	 * Perform an update operation using given <code>configuration</code>.
	 * @param configuration Operation configuration
	 * @return Operation result
	 */
	private OperationResult update(PropertyBoxOperationConfiguration configuration) {
		return operationContext.create(Update.class).target(configuration.getTarget()).value(configuration.getValue())
				.withWriteOptions(configuration.getWriteOptions()).execute();
	}

}
