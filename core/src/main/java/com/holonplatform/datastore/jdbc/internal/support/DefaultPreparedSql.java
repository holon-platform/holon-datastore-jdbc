/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.datastore.jdbc.internal.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.StatementConfigurationException;

/**
 * Default {@link PreparedSql} implementation.
 *
 * @since 5.0.0
 */
public class DefaultPreparedSql implements PreparedSql {

	private static final long serialVersionUID = 2293959517771266701L;

	/**
	 * SQL statement
	 */
	private final String sql;

	/**
	 * Parameters
	 */
	private final List<ParameterValue> parameterValues;

	/**
	 * Constructor
	 * @param sql SQL statement
	 * @param parameterValues Parameters
	 */
	public DefaultPreparedSql(String sql, List<ParameterValue> parameterValues) {
		super();
		this.sql = sql;
		this.parameterValues = parameterValues;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.PreparedSql#getSql()
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.PreparedSql#getParameterValues()
	 */
	@Override
	public List<ParameterValue> getParameterValues() {
		return (parameterValues != null) ? Collections.unmodifiableList(parameterValues) : Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.support.PreparedSql#createStatement(java.sql.Connection,
	 * com.holonplatform.datastore.jdbc.JdbcDialect)
	 */
	@Override
	public PreparedStatement createStatement(Connection connection, JdbcDialect dialect) throws SQLException {
		ObjectUtils.argumentNotNull(connection, "Connection must be not null");
		ObjectUtils.argumentNotNull(dialect, "Dialect must be not null");

		final String sql = getSql();
		if (sql == null) {
			throw new SQLException("Cannot configure a PreparedStatement: null SQL");
		}

		PreparedStatement stmt = connection.prepareStatement(sql);

		try {
			dialect.getStatementConfigurator().configureStatement(connection, stmt, sql, getParameterValues());
		} catch (StatementConfigurationException e) {
			throw new SQLException("Failed to configure statement using SQL [" + sql + "] and parameters ["
					+ getParameterValues() + "]", e);
		}

		return stmt;
	}

}
