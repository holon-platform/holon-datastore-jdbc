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
package com.holonplatform.datastore.jdbc.internal.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLResult;

/**
 * A {@link SQLResult} backed by a jdbc {@link ResultSet}.
 *
 * @since 5.1.0
 */
public class ResultSetSQLResult implements SQLResult {

	private final ResultSet resultSet;

	/**
	 * Constructor
	 * @param resultSet ResultSet (not null)
	 */
	public ResultSetSQLResult(ResultSet resultSet) {
		super();
		ObjectUtils.argumentNotNull(resultSet, "ResultSet must be not null");
		this.resultSet = resultSet;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResult#getValue(int)
	 */
	@Override
	public Object getValue(int index) throws SQLException {
		return resultSet.getObject(index);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResult#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String name) throws SQLException {
		return resultSet.getObject(name);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResult#getValueCount()
	 */
	@Override
	public int getValueCount() throws SQLException {
		return resultSet.getMetaData().getColumnCount();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.SQLResult#getValueName(int)
	 */
	@Override
	public Optional<String> getValueName(int index) throws SQLException {
		return Optional.ofNullable(resultSet.getMetaData().getColumnLabel(index));
	}

	public static SQLResult of(ResultSet resultSet) {
		return new ResultSetSQLResult(resultSet);
	}

}
