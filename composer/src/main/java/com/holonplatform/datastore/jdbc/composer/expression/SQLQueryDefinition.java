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
package com.holonplatform.datastore.jdbc.composer.expression;

import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;

/**
 * SQL query definition expression.
 * 
 * @since 5.1.0
 */
public interface SQLQueryDefinition extends Expression {

	/**
	 * Get the <code>SELECT</code> clause.
	 * @return Select SQL clause
	 */
	String getSelect();

	/**
	 * Get whether to add the <code>DISTINCT</code> clause.
	 * @return Whether to add the <code>DISTINCT</code> clause
	 */
	boolean isDistinct();

	/**
	 * Get the <code>FROM</code> clause.
	 * @return From SQL clause
	 */
	String getFrom();

	/**
	 * Get the <code>WHERE</code> clause.
	 * @return Optional where SQL clause
	 */
	Optional<String> getWhere();

	/**
	 * Get the <code>ORDER BY</code> clause.
	 * @return Optional order by SQL clause
	 */
	Optional<String> getOrderBy();

	/**
	 * Get the <code>GROUP BY</code> clause.
	 * @return Optional group by SQL clause
	 */
	Optional<String> getGroupBy();

	/**
	 * Get the lock mode.
	 * @return Optional lock mode
	 */
	Optional<LockMode> getLockMode();

	/**
	 * Get the lock timeout.
	 * @return Optional lock timeout
	 */
	Optional<Long> getLockTimeout();

	/**
	 * Get the optional {@link SQLResultConverter}.
	 * @return Optional query result converter
	 */
	Optional<SQLResultConverter<?>> getResultConverter();

}
