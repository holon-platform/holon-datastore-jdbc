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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.holonplatform.datastore.jdbc.JdbcDialect;

/**
 * Represents the result of a sql statement preparation whith named parameters replacement.
 * 
 * @since 5.0.0
 */
public interface PreparedSql extends Serializable {

	/**
	 * Get the SQL string
	 * @return SQL
	 */
	String getSql();

	/**
	 * Get the parameter values
	 * @return the parameter values in the right sequence
	 */
	List<ParameterValue> getParameterValues();

	/**
	 * Create a {@link PreparedStatement} from given connection using {@link #getSql()} statement and configure the
	 * statement parameter values according to {@link #getParameterValues()}.
	 * @param connection Connection (not null)
	 * @param dialect Dialect (not null)
	 * @return The configured {@link PreparedStatement}
	 * @throws SQLException If an error occurred creating or configuring the statement
	 */
	PreparedStatement createStatement(Connection connection, JdbcDialect dialect) throws SQLException;

}
