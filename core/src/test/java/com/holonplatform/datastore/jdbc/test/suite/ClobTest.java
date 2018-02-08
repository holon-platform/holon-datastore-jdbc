/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.datastore.jdbc.test.suite;

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.CLOB_RDR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.CLOB_STR;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;

public class ClobTest extends AbstractJdbcDatastoreSuiteTest {

	private static final PropertySet<?> CLOB_SET_STR = PropertySet.of(PROPERTIES, CLOB_STR);
	private static final PropertySet<?> CLOB_SET_RDR = PropertySet.of(PROPERTIES, CLOB_RDR);

	@Test
	public void testClobString() {
		inTransaction(() -> {

			// query

			String sval = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_STR).orElse(null);
			assertNotNull(sval);
			assertEquals("clocbcontent", sval);

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_STR)
					.orElse(null);
			assertNotNull(value);
			assertEquals("clocbcontent", value.getValue(CLOB_STR));

			// update

			value.setValue(CLOB_STR, "updclob");
			getDatastore().update(NAMED_TARGET, value);

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_STR).orElse(null);
			assertNotNull(value);
			assertEquals("updclob", value.getValue(CLOB_STR));

			// insert
			value = PropertyBox.builder(CLOB_SET_STR).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
					.set(CLOB_STR, "savedclob").build();
			getDatastore().insert(NAMED_TARGET, value);

			sval = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(CLOB_STR).orElse(null);
			assertNotNull(sval);
			assertEquals("savedclob", sval);

		});
	}

	@Test
	public void testClobReader() {
		inTransaction(() -> {
			try {
				// query

				try (Reader reader = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_RDR)
						.orElse(null)) {
					assertNotNull(reader);
					String sval = ConversionUtils.readerToString(reader, false);
					assertEquals("clocbcontent", sval);
				}

				PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_RDR)
						.orElse(null);
				assertNotNull(value);
				try (Reader reader = value.getValue(CLOB_RDR)) {
					assertNotNull(reader);
					String sval = ConversionUtils.readerToString(reader, false);
					assertEquals("clocbcontent", sval);
				}

				// update

				value.setValue(CLOB_RDR, new StringReader("updclob"));
				getDatastore().update(NAMED_TARGET, value);

				value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(CLOB_SET_RDR)
						.orElse(null);
				assertNotNull(value);
				try (Reader reader = value.getValue(CLOB_RDR)) {
					assertNotNull(reader);
					String sval = ConversionUtils.readerToString(reader, false);
					assertEquals("updclob", sval);
				}

				// insert
				value = PropertyBox.builder(CLOB_SET_RDR).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
						.set(CLOB_RDR, new StringReader("savedclob")).build();
				getDatastore().insert(NAMED_TARGET, value);

				try (Reader reader = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(CLOB_RDR)
						.orElse(null)) {
					assertNotNull(reader);
					String sval = ConversionUtils.readerToString(reader, false);
					assertEquals("savedclob", sval);
				}

			} catch (IOException e) {
				throw new DataAccessException(e);
			}
		});
	}

}
