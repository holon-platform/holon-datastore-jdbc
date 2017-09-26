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
package com.holonplatform.datastore.jdbc.internal.resolvers;

import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.internal.expressions.TablePrimaryKey;
import com.holonplatform.datastore.jdbc.internal.pk.PrimaryKeyInspector;
import com.holonplatform.datastore.jdbc.internal.pk.PrimaryKeysCache;

/**
 * {@link TablePrimaryKey} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public class PrimaryKeyResolver implements ExpressionResolver<DataTarget, TablePrimaryKey> {

	private static final long serialVersionUID = -1693018694034935286L;

	/**
	 * Cache
	 */
	private final PrimaryKeysCache primaryKeysCache;

	/**
	 * Inspector
	 */
	private final PrimaryKeyInspector primaryKeyInspector;

	/**
	 * Constructor
	 * @param primaryKeysCache Cache (not null)
	 * @param primaryKeyInspector Inspector (not null)
	 */
	public PrimaryKeyResolver(PrimaryKeysCache primaryKeysCache, PrimaryKeyInspector primaryKeyInspector) {
		super();

		ObjectUtils.argumentNotNull(primaryKeysCache, "PrimaryKeysCache must be not null");
		ObjectUtils.argumentNotNull(primaryKeyInspector, "Primary key inspector must be not null");

		this.primaryKeysCache = primaryKeysCache;
		this.primaryKeyInspector = primaryKeyInspector;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends DataTarget> getExpressionType() {
		return DataTarget.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends TablePrimaryKey> getResolvedType() {
		return TablePrimaryKey.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<TablePrimaryKey> resolve(DataTarget expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		String tableName = null;
		Optional<TablePrimaryKey> targetPrimaryKey = Optional.empty();

		// resolve target
		final DataTarget<?> target = context.resolve(expression, DataTarget.class, context).orElse(expression);
		target.validate();
		tableName = target.getName();

		if (targetPrimaryKey.isPresent()) {
			return targetPrimaryKey;
		}

		return retrieve(tableName);
	}

	/**
	 * Retrieve the primary key for given table name.
	 * <p>
	 * The primary key is obtained from cache if available.
	 * </p>
	 * @param tableName Table name
	 * @return Table primary key
	 * @throws InvalidExpressionException If an error occurred
	 */
	private Optional<TablePrimaryKey> retrieve(String tableName) throws InvalidExpressionException {
		Optional<TablePrimaryKey> cached = primaryKeysCache.get(tableName);
		if (cached.isPresent()) {
			return cached;
		}

		try {
			Optional<Path<?>[]> pk = primaryKeyInspector.getPrimaryKey(tableName);
			if (pk.isPresent()) {
				TablePrimaryKey tablePrimaryKey = TablePrimaryKey.create(pk.get());
				primaryKeysCache.put(tableName, tablePrimaryKey);
				return Optional.of(tablePrimaryKey);
			}
		} catch (SQLException e) {
			throw new InvalidExpressionException("Failed to retrieve primary key for table [" + tableName + "]", e);
		}

		return Optional.empty();
	}

}
