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

/**
 * SQL query clauses expression.
 * 
 * @since 5.0.0
 */
public interface SQLQueryClauses extends Expression {

	/**
	 * Get the <code>SELECT</code> clause.
	 * @return Select SQL clause
	 */
	String getSelect();

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
	 * Get the optional {@link SQLProjection} bound to this query clauses expression.
	 * @return Optional projection
	 */
	Optional<SQLProjection<?>> getProjection();

}
