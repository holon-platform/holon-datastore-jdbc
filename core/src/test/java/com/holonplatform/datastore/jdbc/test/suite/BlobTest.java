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

import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.BLOB_BYS;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.BLOB_IST;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.KEY;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NAMED_TARGET;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.NBOOL;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.PROPERTIES;
import static com.holonplatform.datastore.jdbc.test.data.TestDataModel.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.streams.LimitedInputStream;
import com.holonplatform.datastore.jdbc.test.data.TestDataModel;

public class BlobTest extends AbstractJdbcDatastoreSuiteTest {

	private static final PropertySet<?> BLOB_SET_BYT = PropertySet.of(PROPERTIES, BLOB_BYS);
	private static final PropertySet<?> BLOB_SET_IST = PropertySet.of(PROPERTIES, BLOB_IST);

	@Test
	public void testBlobBytes() {
		inTransaction(() -> {

			// query

			byte[] bval = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_BYS).orElse(null);
			assertNotNull(bval);
			assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, bval));

			PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_BYT)
					.orElse(null);
			assertNotNull(value);
			assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, value.getValue(BLOB_BYS)));

			// update

			final byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

			value.setValue(BLOB_BYS, bytes);
			getDatastore().update(NAMED_TARGET, value);

			value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_BYT).orElse(null);
			assertNotNull(value);
			assertTrue(Arrays.equals(bytes, value.getValue(BLOB_BYS)));

			// insert

			value = PropertyBox.builder(BLOB_SET_BYT).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
					.set(BLOB_BYS, bytes).build();
			getDatastore().insert(NAMED_TARGET, value);

			bval = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(BLOB_BYS).orElse(null);
			assertNotNull(bval);
			assertTrue(Arrays.equals(bytes, bval));

		});
	}

	@SuppressWarnings("resource")
	@Test
	public void testBlobStream() {
		inTransaction(() -> {

			try {
				// query

				InputStream is = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_IST)
						.orElse(null);
				assertNotNull(is);
				byte[] bval = ConversionUtils.convertInputStreamToBytes(is);
				assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, bval));

				PropertyBox value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_IST)
						.orElse(null);
				assertNotNull(value);
				is = value.getValue(BLOB_IST);
				bval = ConversionUtils.convertInputStreamToBytes(is);
				assertTrue(Arrays.equals(TestDataModel.DEFAULT_BLOB_VALUE, bval));

				// update

				final byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

				value.setValue(BLOB_IST, new ByteArrayInputStream(bytes));
				getDatastore().update(NAMED_TARGET, value);

				value = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(1L)).findOne(BLOB_SET_IST)
						.orElse(null);
				assertNotNull(value);

				is = value.getValue(BLOB_IST);
				bval = ConversionUtils.convertInputStreamToBytes(is);

				assertTrue(Arrays.equals(bytes, bval));

				// insert

				value = PropertyBox.builder(BLOB_SET_IST).set(KEY, 77L).set(STR, "Test clob").set(NBOOL, false)
						.set(BLOB_IST, new ByteArrayInputStream(bytes)).build();
				getDatastore().insert(NAMED_TARGET, value);

				is = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(BLOB_IST).orElse(null);
				assertNotNull(is);
				bval = ConversionUtils.convertInputStreamToBytes(is);
				assertTrue(Arrays.equals(bytes, bval));

			} catch (IOException e) {
				throw new DataAccessException(e);
			}

		});
	}

	@Test
	public void testBlobFile() {
		inTransaction(() -> {

			final File file = new File(getClass().getClassLoader().getResource("testfile.txt").getFile());

			try (FileInputStream fis = new FileInputStream(file)) {

				PropertyBox value = PropertyBox.builder(BLOB_SET_IST).set(KEY, 77L).set(STR, "Test clob")
						.set(NBOOL, false).set(BLOB_IST, LimitedInputStream.create(fis, file.length())).build();
				getDatastore().insert(NAMED_TARGET, value);

			} catch (IOException e) {
				throw new DataAccessException(e);
			}

			try (InputStream is = getDatastore().query().target(NAMED_TARGET).filter(KEY.eq(77L)).findOne(BLOB_IST)
					.orElse(null)) {
				assertNotNull(is);
				byte[] bval = ConversionUtils.convertInputStreamToBytes(is);
				String cnt = new String(bval);
				assertEquals("testfilecontent", cnt);
			} catch (IOException e) {
				throw new DataAccessException(e);
			}

		});
	}

}
