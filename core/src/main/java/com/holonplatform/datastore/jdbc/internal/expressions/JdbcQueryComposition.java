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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;

/**
 * Represents the composition of a JDBC query, providing query clauses and a {@link #serialize()} method to obtain the
 * actual SQL query.
 * 
 * @param <T> Projection result type
 *
 * @since 5.0.0
 */
public interface JdbcQueryComposition<T> extends JdbcQueryClauses, Expression {

	/**
	 * Get result set limit.
	 * @return Results limit, an empty Optional indicates no limit.
	 */
	Optional<Integer> getLimit();

	/**
	 * Get 0-based results offset.
	 * @return Results offset 0-based index, an empty Optional indicates no offset.
	 */
	Optional<Integer> getOffset();

	/**
	 * Get the query projection.
	 * @return the query projection
	 */
	ProjectionContext<T> getProjection();

	/**
	 * Serialize the query cluases into a SQL query
	 * @return SQL query
	 * @throws QueryBuildException Error serializing query
	 */
	String serialize() throws QueryBuildException;

}
