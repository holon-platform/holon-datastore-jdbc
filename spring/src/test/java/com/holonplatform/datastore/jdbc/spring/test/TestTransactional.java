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
package com.holonplatform.datastore.jdbc.spring.test;

import java.sql.ResultSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.jdbc.spring.EnableDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTransactional.Config.class)
@DirtiesContext
public class TestTransactional {

	private final static PathProperty<Long> CODE = PathProperty.create("code", long.class);
	private final static PathProperty<String> TEXT = PathProperty.create("text", String.class);

	private final static DataTarget<String> TARGET = DataTarget.named("testtx");

	@Configuration
	@PropertySource("testtx.properties")
	@EnableDataSource(enableTransactionManager = true, dataContextId = "tx")
	@EnableJdbcDatastore(dataContextId = "tx")
	protected static class Config {

	}

	@Autowired
	private JdbcDatastore datastore;

	@Test
	public void testTransactional() {

		long count = datastore.query().target(TARGET).count();
		Assert.assertEquals(1L, count);

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);

			long cnt = datastore.query().target(TARGET).count();
			Assert.assertEquals(2L, cnt);

			tx.rollback();
		});

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(1L, count);

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 2L).set(TEXT, "Two").build();
			datastore.insert(TARGET, box);

			long cnt = datastore.query().target(TARGET).count();
			Assert.assertEquals(2L, cnt);

			tx.commit();
		});

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(2L, count);

		// test auto commit

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 3L).set(TEXT, "Three").build();
			datastore.insert(TARGET, box);
		}, TransactionConfiguration.withAutoCommit());

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(3L, count);

		// rollback

		datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 4L).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			tx.rollback();
		});

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(3L, count);

		// rollback on error

		TestUtils.expectedException(DataAccessException.class, () -> datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			tx.commit();
		}));

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(3L, count);

		TestUtils.expectedException(RuntimeException.class, () -> datastore.withTransaction(tx -> {
			PropertyBox box = PropertyBox.builder(CODE, TEXT).set(CODE, 4L).set(TEXT, "ToRollback").build();
			datastore.insert(TARGET, box);

			throw new RuntimeException("Should rollback");

		}, TransactionConfiguration.withAutoCommit()));

		count = datastore.query().target(TARGET).count();
		Assert.assertEquals(3L, count);

		// test nested

		datastore.withTransaction(tx -> {
			String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
			Assert.assertEquals("Two", txt);

			OperationResult res = datastore.bulkUpdate(TARGET).set(TEXT, "Two*").filter(CODE.eq(2L)).execute();
			Assert.assertEquals(1, res.getAffectedCount());

			String val = datastore.withConnection(c -> {
				try (ResultSet rs = c.createStatement().executeQuery("SELECT text FROM testtx WHERE code=2")) {
					rs.next();
					return rs.getString(1);
				}
			});
			Assert.assertEquals("Two*", val);

			tx.rollback();
		});

		String txtv = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
		Assert.assertEquals("Two", txtv);

		datastore.withTransaction(tx -> {

			OperationResult res = datastore.bulkUpdate(TARGET).set(TEXT, "Two_tx1").filter(CODE.eq(2L)).execute();
			Assert.assertEquals(1, res.getAffectedCount());

			datastore.withTransaction(tx2 -> {

				String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
				Assert.assertEquals("Two_tx1", txt);

				Assert.assertFalse(tx2.isNew());

			});

			String txt = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
			Assert.assertEquals("Two_tx1", txt);

			tx.rollback();
		});

		txtv = datastore.query().target(TARGET).filter(CODE.eq(2L)).findOne(TEXT).orElse(null);
		Assert.assertEquals("Two", txtv);

	}

}
