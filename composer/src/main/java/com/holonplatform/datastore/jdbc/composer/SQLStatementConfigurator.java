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
package com.holonplatform.datastore.jdbc.composer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holonplatform.datastore.jdbc.composer.expression.SQLStatement;
import com.holonplatform.datastore.jdbc.composer.internal.DefaultSQLStatementConfigurator;

/**
 * SQL statements configurator.
 *
 * @since 5.1.0
 */
public interface SQLStatementConfigurator {

	/**
	 * Configure given {@link PreparedStatement}, setting the statement parameter values, if necessary.
	 * @param context SQL context
	 * @param jdbcStatement The JDBC statement to configure (not null)
	 * @param sqlStatement The SQL statement definition (not null)
	 * @return The configured JDBC statement
	 * @throws SQLException If an error occurred
	 */
	PreparedStatement configureStatement(SQLContext context, PreparedStatement jdbcStatement, SQLStatement sqlStatement)
			throws SQLException;

	/**
	 * Get the default {@link SQLStatementConfigurator}.
	 * @return the default {@link SQLStatementConfigurator}
	 */
	static SQLStatementConfigurator getDefault() {
		return DefaultSQLStatementConfigurator.INSTANCE;
	}

}
