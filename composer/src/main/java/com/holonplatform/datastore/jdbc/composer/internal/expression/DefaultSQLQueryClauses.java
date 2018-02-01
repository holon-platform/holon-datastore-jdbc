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
package com.holonplatform.datastore.jdbc.composer.internal.expression;

import java.util.Optional;

import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryClauses;

/**
 * Default {@link SQLQueryClauses}.
 *
 * @since 5.1.0
 */
public class DefaultSQLQueryClauses implements SQLQueryClauses {

	/**
	 * SELECT clause
	 */
	private String select;

	/**
	 * FROM clause
	 */
	private String from;

	/**
	 * WHERE clause
	 */
	private String where;

	/**
	 * ORDER BY clause
	 */
	private String orderBy;

	/**
	 * GROUP BY clause
	 */
	private String groupBy;

	/**
	 * Projection
	 */
	private SQLProjection<?> projection;

	public DefaultSQLQueryClauses() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see om.holonplatform.datastore.jdbc.composer.expression#getSelect()
	 */
	@Override
	public String getSelect() {
		return select;
	}

	/*
	 * (non-Javadoc)
	 * @see om.holonplatform.datastore.jdbc.composer.expression#getFrom()
	 */
	@Override
	public String getFrom() {
		return from;
	}

	/*
	 * (non-Javadoc)
	 * @see om.holonplatform.datastore.jdbc.composer.expression#getWhere()
	 */
	@Override
	public Optional<String> getWhere() {
		return Optional.ofNullable(where);
	}

	/*
	 * (non-Javadoc)
	 * @see om.holonplatform.datastore.jdbc.composer.expression#getOrderBy()
	 */
	@Override
	public Optional<String> getOrderBy() {
		return Optional.ofNullable(orderBy);
	}

	/*
	 * (non-Javadoc)
	 * @see om.holonplatform.datastore.jdbc.composer.expression#getGroupBy()
	 */
	@Override
	public Optional<String> getGroupBy() {
		return Optional.ofNullable(groupBy);
	}

	/**
	 * Set the SELECT query part
	 * @param select The part to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}

	/**
	 * Set the FROM query part
	 * @param from The part to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * Set the WHERE query part
	 * @param where The part to set
	 */
	public void setWhere(String where) {
		this.where = where;
	}

	/**
	 * Set the ORDER BY query part
	 * @param orderBy The part to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Set the GROUP BY query part
	 * @param groupBy The part to set
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	/**
	 * Set the {@link SQLProjection}.
	 * @param projection the projection to set
	 */
	public void setProjection(SQLProjection<?> projection) {
		this.projection = projection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQueryClauses#getProjection()
	 */
	@Override
	public Optional<SQLProjection<?>> getProjection() {
		return Optional.ofNullable(projection);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSelect() == null || getSelect().trim().equals("")) {
			throw new InvalidExpressionException("Missing query SELECT clause");
		}
		if (getFrom() == null || getFrom().trim().equals("")) {
			throw new InvalidExpressionException("Missing query FROM clause");
		}
	}

}
