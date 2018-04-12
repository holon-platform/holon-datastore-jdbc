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
package com.holonplatform.datastore.jdbc.examples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;

import com.holonplatform.core.Path;
import com.holonplatform.core.Validator;
import com.holonplatform.core.beans.BeanDataTarget;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.OrderBySort;
import com.holonplatform.datastore.jdbc.WhereFilter;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.SQLDialectContext;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.JdbcConnectionHandler;

@SuppressWarnings("unused")
public class ExampleJdbcDatastore {

	@SuppressWarnings("serial")
	private class MyDialect implements SQLDialect {

		@Override
		public void init(SQLDialectContext context) throws SQLException {
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

	public void builder1() {
		// tag::builder1[]
		JdbcDatastore datastore = JdbcDatastore.builder() // <1>
				// Datastore configuration omitted
				.build();
		// end::builder1[]
	}

	public void builder2() {
		// tag::builder2[]
		Datastore datastore = JdbcDatastore.builder() // <1>
				// Datastore configuration omitted
				.build();
		// end::builder2[]
	}

	public void setup1() {
		// tag::setup1[]
		Datastore datastore = JdbcDatastore.builder()
				// DataSource configuration omitted
				.dataContextId("mydataContextId") // <1>
				.traceEnabled(true) // <2>
				.build();
		// end::setup1[]
	}

	public void setup2() {
		// tag::setup2[]
		Datastore datastore = JdbcDatastore.builder()
				// DataSource configuration omitted
				.configuration(DatastoreConfigProperties.builder().withPropertySource("datastore.properties").build()) // <1>
				.build();
		// end::setup2[]
	}

	public void setup3() {
		// tag::setup3[]
		DataSource datasource = createOrObtainDatasource();

		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(datasource) // <1>
				.build();
		// end::setup3[]
	}

	public void setup4() {
		// tag::setup4[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(DataSourceConfigProperties.builder().withPropertySource("datasource.properties").build()) // <1>
				.build();
		// end::setup4[]
	}

	public void setup5() {
		// tag::setup5[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(createOrObtainDatasource()) //
				.database(DatabasePlatform.H2) // <1>
				.build();
		// end::setup5[]
	}

	public void setup6() {
		// tag::setup6[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(createOrObtainDatasource()) //
				.dialect(SQLDialect.h2()) // <1>
				.dialect("com.holonplatform.datastore.jdbc.composer.dialect.H2Dialect") // <2>
				.build();
		// end::setup6[]
	}

	public void setup7() {
		// tag::setup7[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(createOrObtainDatasource()) //
				.configuration(DatastoreConfigProperties.builder().withPropertySource("datastore.properties").build()) // <1>
				.build();
		// end::setup7[]
	}

	public void setup8() {
		// tag::setup8[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(createOrObtainDatasource()) //
				.identifierResolutionStrategy(IdentifierResolutionStrategy.TABLE_PRIMARY_KEY) // <1>
				.build();
		// end::setup8[]
	}

	public void setup9() {
		// tag::setup9[]
		Datastore datastore = JdbcDatastore.builder() //
				.dataSource(createOrObtainDatasource()) //
				.connectionHandler(new JdbcConnectionHandler() { // <1>

					@Override
					public Connection getConnection(DataSource dataSource, ConnectionType connectionType)
							throws SQLException {
						// provide the JDBC connection
						return dataSource.getConnection();
					}

					@Override
					public void releaseConnection(Connection connection, DataSource dataSource,
							ConnectionType connectionType) throws SQLException {
						// release the JDBC connection
						connection.close();
					}

				}).build();
		// end::setup9[]
	}

	// tag::naming1[]
	static final NumericProperty<Long> ID = NumericProperty.longType("code") // <1>
			.validator(Validator.notNull());
	static final StringProperty VALUE = StringProperty.create("text") // <2>
			.validator(Validator.max(100));

	static final PropertySet<?> TEST = PropertySet.builderOf(ID, VALUE).identifier(ID).build(); // <3>

	static final DataTarget<?> TARGET = DataTarget.named("test"); // <4>
	// end::naming1[]

	public void naming2() {
		// tag::naming2[]
		Datastore datastore = JdbcDatastore.builder().dataSource(createOrObtainDatasource()).build(); // <1>

		PropertyBox value = PropertyBox.builder(TEST).set(ID, 1L).set(VALUE, "One").build();
		datastore.save(TARGET, value); // <2>

		Stream<PropertyBox> results = datastore.query().target(TARGET).filter(ID.goe(1L)).stream(TEST); // <3>

		List<String> values = datastore.query().target(TARGET).sort(ID.asc()).list(VALUE); // <4>

		datastore.bulkDelete(TARGET).filter(ID.gt(0L)).execute(); // <5>
		// end::naming2[]
	}

	public void mapping() {
		// tag::mapping[]
		StringProperty PROPERTY = StringProperty.create("propertyName").dataPath("str"); // <1>
		// end::mapping[]
	}

	@SuppressWarnings("hiding")
	static
	// tag::mapping2[]
	@Entity @Table(name = "test") class MyEntity {

		public static final BeanPropertySet<MyEntity> PROPERTIES = BeanPropertySet.create(MyEntity.class); // <1>

		public static final DataTarget<MyEntity> TARGET = BeanDataTarget.of(MyEntity.class); // <2>

		@Id
		@Column(name = "code")
		private Long id;

		@Column(name = "text")
		private String value;

		// getters and setters omitted

	}
	// end::mapping2[]

	public void mapping3() {
		// tag::mapping3[]
		Datastore datastore = JdbcDatastore.builder().dataSource(createOrObtainDatasource()).build(); // <1>

		PropertyBox value = PropertyBox.builder(MyEntity.PROPERTIES) //
				.set(MyEntity.PROPERTIES.property("id"), 1L) //
				.set(MyEntity.PROPERTIES.property("value"), "One").build();
		datastore.save(MyEntity.TARGET, value); // <2>

		Stream<PropertyBox> results = datastore.query().target(MyEntity.TARGET)
				.filter(MyEntity.PROPERTIES.property("id").goe(1L)).stream(MyEntity.PROPERTIES); // <3>
		// end::mapping3[]
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

	public void ids1() {
		// tag::ids1[]
		Datastore datastore = getDatastore(); // build or obtain a JDBC Datastore

		PropertyBox value = buildPropertyBoxValue();

		OperationResult result = datastore.insert(DataTarget.named("test"), value); // <1>

		Map<Path<?>, Object> keys = result.getInsertedKeys(); // <2>
		Optional<Long> keyValue = result.getInsertedKey(ID); // <3>
		keyValue = result.getFirstInsertedKey(Long.class); // <4>
		// end::ids1[]
	}

	public static PropertyBox buildPropertyBoxValue() {
		return null;
	}

	public void ids2() {
		// tag::ids2[]
		final PathProperty<Long> KEY = PathProperty.create("key", Long.class); // <1>
		final PathProperty<String> TEXT = PathProperty.create("text", String.class);

		Datastore datastore = getDatastore(); // build or obtain a JDBC Datastore

		PropertyBox value = PropertyBox.builder(KEY, TEXT).set(TEXT, "test").build(); // <2>

		datastore.insert(DataTarget.named("tableName"), value, DefaultWriteOption.BRING_BACK_GENERATED_IDS); // <3>

		Long keyValue = value.getValue(KEY); // <4>
		// end::ids2[]
	}

	public void where1() {
		// tag::where1[]
		QueryFilter whereFilter = WhereFilter.create("name='John'"); // <1>

		Stream<Long> results = getDatastore().query().target(TARGET).filter(whereFilter).stream(ID); // <2>
		// end::where1[]
	}

	public void where2() {
		// tag::where2[]
		QueryFilter whereFilter = WhereFilter.create("name=?", "John"); // <1>
		// end::where2[]
	}

	public void orderby() {
		// tag::orderby[]
		QuerySort orderBySort = OrderBySort.create("id asc, name desc"); // <1>

		Stream<Long> results = getDatastore().query().target(TARGET).sort(orderBySort).stream(ID); // <2>
		// end::orderby[]
	}

	public void transactional() {
		// tag::transactional[]
		final Datastore datastore = getDatastore(); // build or obtain a JDBC Datastore

		datastore.requireTransactional().withTransaction(tx -> { // <1>
			PropertyBox value = buildPropertyBoxValue();
			datastore.save(TARGET, value);

			tx.commit(); // <2>
		});

		OperationResult result = datastore.requireTransactional().withTransaction(tx -> { // <3>

			PropertyBox value = buildPropertyBoxValue();
			return datastore.save(TARGET, value);

		}, TransactionConfiguration.withAutoCommit()); // <4>
		// end::transactional[]
	}

	private static DataSource createOrObtainDatasource() {
		return null;
	}

	@SuppressWarnings("static-method")
	private DataSource getDataSource() {
		return null;
	}

	private static Datastore getDatastore() {
		return null;
	}

	private static JdbcDatastore getJdbcDatastore() {
		return null;
	}

}
