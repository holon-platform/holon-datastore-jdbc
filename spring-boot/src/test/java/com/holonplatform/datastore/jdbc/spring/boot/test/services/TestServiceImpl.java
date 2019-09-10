/*
 * Copyright 2016-2019 Axioma srl.
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
package com.holonplatform.datastore.jdbc.spring.boot.test.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.spring.boot.test.TestJdbcDatastoreTransactional;

@Service
public class TestServiceImpl implements TestService {

	@Autowired
	private Datastore datastore;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void testSave(long id) {

		// save test1
		save1(id);

		// save test2
		save2(id);
		
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void save1(long id) {
		PropertyBox v1 = PropertyBox.builder(TestJdbcDatastoreTransactional.SET1)
				.set(TestJdbcDatastoreTransactional.KEY1, id).set(TestJdbcDatastoreTransactional.STR1, "STR1:" + id)
				.build();

		datastore.save(TestJdbcDatastoreTransactional.TARGET1, v1);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void save2(long id) {
		PropertyBox v2 = PropertyBox.builder(TestJdbcDatastoreTransactional.SET2)
				.set(TestJdbcDatastoreTransactional.KEY2, id).set(TestJdbcDatastoreTransactional.STR2, "STR2:" + id)
				.build();

		datastore.save(DataTarget.named("wrong") /*TestJdbcDatastoreTransactional.TARGET2*/, v2);
	}

}
