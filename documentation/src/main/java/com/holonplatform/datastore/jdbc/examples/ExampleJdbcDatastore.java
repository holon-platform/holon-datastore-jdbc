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
package com.holonplatform.datastore.jdbc.examples;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcOrderBySort;
import com.holonplatform.datastore.jdbc.JdbcWhereFilter;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;

@SuppressWarnings("unused")
public class ExampleJdbcDatastore {

	@SuppressWarnings("serial")
	private class MyDialect implements JdbcDialect {

		@Override
		public StatementConfigurator getStatementConfigurator() {
			return null;
		}

		@Override
		public void init(JdbcDatastore datastore) throws SQLException {
		}

		@Override
		public boolean supportsLikeEscapeClause() {
			return false;
		}

		@Override
		public boolean supportsGetGeneratedKeys() {
			return false;
		}

		@Override
		public boolean generatedKeyAlwaysReturned() {
			return false;
		}

	}

	public void setup() {
		// tag::setup[]
		JdbcDatastore datastore = JdbcDatastore.builder() // obtain the builder
				.dataSource(getDataSource()) // <1>
				.database(DatabasePlatform.ORACLE) // <2>
				.build();

		datastore = JdbcDatastore.builder() // obtain the builder
				.dataSource(DataSourceConfigProperties.builder().withPropertySource("jdbc.properties").build()) // <3>
				.dialect(new MyDialect()) // <4>
				.build();

		datastore = JdbcDatastore.builder() // obtain the builder
				.dataContextId("db1") // <5>
				.dataSource(DataSourceConfigProperties.builder("db1").withPropertySource("jdbc.properties").build()) // <6>
				.configuration(
						DatastoreConfigProperties.builder("db1").withPropertySource("datastore.properties").build()) // <7>
				.build();
		// end::setup[]
	}

	public void connection() {
		// tag::connection[]
		JdbcDatastore datastore = getJdbcDatastore(); // build or obtain a JdbcDatastore

		String name = datastore.withConnection(connection -> {
			try (ResultSet rs = connection.createStatement().executeQuery("select name from test where id=1")) {
				rs.next();
				return rs.getString(1);
			}
		});
		// end::connection[]
	}

	public void ids() {
		// tag::ids[]
		final PathProperty<Long> KEY = PathProperty.create("key", Long.class); // <1>
		final PathProperty<String> TEXT = PathProperty.create("text", String.class);

		Datastore datastore = getDatastore(); // build or obtain a Datastore

		PropertyBox value = PropertyBox.builder(KEY, TEXT).set(TEXT, "test").build(); // <2>

		datastore.insert(DataTarget.named("tableName"), value, DefaultWriteOption.BRING_BACK_GENERATED_IDS); // <3>

		Long keyValue = value.getValue(KEY); // <4>
		// end::ids[]
	}

	public void where() {
		// tag::where[]
		QueryFilter filter = JdbcWhereFilter.create("name=? and id=?", "TestName", 1); // <1>
		// end::where[]
	}

	public void orderby() {
		// tag::orderby[]
		QuerySort sort = JdbcOrderBySort.create("id asc, name desc"); // <1>
		// end::orderby[]
	}

	@SuppressWarnings("static-method")
	private DataSource getDataSource() {
		return null;
	}

	@SuppressWarnings("static-method")
	private Datastore getDatastore() {
		return null;
	}

	@SuppressWarnings("static-method")
	private JdbcDatastore getJdbcDatastore() {
		return null;
	}

}
