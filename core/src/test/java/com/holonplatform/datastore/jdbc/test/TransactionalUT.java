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
package com.holonplatform.datastore.jdbc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DatabasePlatform;

public class TransactionalUT {

	private final static String INIT_SQL = "create table testtx (code bigint primary key, text varchar(100) not null); INSERT INTO testtx VALUES (1, 'TheOne');";

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);

	private final static DataTarget<String> TARGET = DataTarget.named("testtx");

	private static JdbcDatastore datastore;

	@BeforeAll
	public static void initDatastore() {

		DataSource dataSource = DataSourceBuilder.builder().url("jdbc:h2:mem:txdb").username("sa")
				.withInitScript(INIT_SQL).build();

		datastore = JdbcDatastore.builder().dataSource(dataSource).database(DatabasePlatform.H2).traceEnabled(true)
				.build();
	}

	@Test
	public void testTransactional() {

		long count = datastore.query().target(TARGET).count();
		assertEquals(1L, count);

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);

			long cnt = datastore.query().target(TARGET).count();
			assertEquals(2L, cnt);

			tx.rollback();
		});

		count = datastore.query().target(TARGET).count();
		assertEquals(1L, count);

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);

			long cnt = datastore.query().target(TARGET).count();
			assertEquals(2L, cnt);

			tx.commit();
		});

		count = datastore.query().target(TARGET).count();
		assertEquals(2L, count);

		// test auto commit

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 3L).set(TEXT, "Three").build();
			datastore.insert(TARGET, box);
		}, TransactionConfiguration.withAutoCommit());

		count = datastore.query().target(TARGET).count();
		assertEquals(3L, count);

		// rollback

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 4L).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			tx.rollback();
		});

		count = datastore.query().target(TARGET).count();
		assertEquals(3L, count);

		// rollback on error

		Assertions.assertThrows(DataAccessException.class, () -> datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			tx.commit();
		}));

		count = datastore.query().target(TARGET).count();
		assertEquals(3L, count);

		Assertions.assertThrows(RuntimeException.class, () -> datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 4L).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			throw new RuntimeException("Should rollback");

		}, TransactionConfiguration.withAutoCommit()));

		count = datastore.query().target(TARGET).count();
		assertEquals(3L, count);

		// test nested

		datastore.withTransaction(tx -> {
			String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
			assertEquals("Two", txt);

			OperationResult res = datastore.bulkUpdate(TARGET).set(TEXT, "Two*").filter(CODE.eq(2L)).execute();
			assertEquals(1, res.getAffectedCount());

			String val = datastore.withConnection(c -> {
				try (ResultSet rs = c.createStatement().executeQuery("SELECT text FROM testtx WHERE code=2")) {
					rs.next();
					return rs.getString(1);
				}
			});
			assertEquals("Two*", val);

			tx.rollback();
		});

		String txtv = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
		assertEquals("Two", txtv);

		datastore.withTransaction(tx -> {

			assertTrue(tx.isNew());

			OperationResult res = datastore.bulkUpdate(TARGET).set(TEXT, "Two_tx1").filter(CODE.eq(2L)).execute();
			assertEquals(1, res.getAffectedCount());

			datastore.withTransaction(tx2 -> {

				String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
				assertEquals("Two_tx1", txt);

				assertFalse(tx2.isNew());

			});

			String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
			assertEquals("Two_tx1", txt);

			tx.rollback();
		});

		txtv = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
		assertEquals("Two", txtv);

	}

}
