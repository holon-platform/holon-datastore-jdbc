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

import com.holonplatform.core.query.lock.LockMode;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition;

/**
 * Default {@link SQLQueryDefinition}.
 *
 * @since 5.1.0
 */
public class DefaultSQLQueryDefinition implements SQLQueryDefinition {

	/**
	 * SELECT clause
	 */
	private String select;

	/**
	 * DISTINCT clause
	 */
	private boolean distinct;

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
	 * Locks
	 */
	private LockMode lockMode;

	private Long lockTimeout;

	/**
	 * Result converter
	 */
	private SQLResultConverter<?> resultConverter;

	public DefaultSQLQueryDefinition() {
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
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition#isDistinct()
	 */
	@Override
	public boolean isDistinct() {
		return distinct;
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

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition#getLockMode()
	 */
	@Override
	public Optional<LockMode> getLockMode() {
		return Optional.ofNullable(lockMode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition#getLockTimeout()
	 */
	@Override
	public Optional<Long> getLockTimeout() {
		return Optional.ofNullable(lockTimeout);
	}

	/**
	 * Set the SELECT query part
	 * @param select The part to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}

	/**
	 * Set whether to add the DISTINCT clause.
	 * @param distinct <code>true</code> to add the DISTINCT clause
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
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
	 * Set the lock mode.
	 * @param lockMode the lock mode to set
	 */
	public void setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
	}

	/**
	 * Set the lock timeout.
	 * @param lockTimeout the lock timeout to set
	 */
	public void setLockTimeout(Long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	/**
	 * Set the {@link SQLResultConverter}.
	 * @param resultConverter the result converter to set
	 */
	public void setResultConverter(SQLResultConverter<?> resultConverter) {
		this.resultConverter = resultConverter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLQueryDefinition#getResultConverter()
	 */
	@Override
	public Optional<SQLResultConverter<?>> getResultConverter() {
		return Optional.ofNullable(resultConverter);
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultSQLQueryDefinition [select=" + select + ", from=" + from + ", where=" + where + ", orderBy="
				+ orderBy + ", groupBy=" + groupBy + ", resultConverter=" + resultConverter + "]";
	}

}
