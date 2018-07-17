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

import java.util.Optional;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.query.lock.LockQuery;

@SuppressWarnings("unused")
public class ExampleJdbcDatastoreLock {

	public void commodity() {
		// tag::lockcommodity[]
		Datastore datastore = getJdbcDatastore();

		LockQuery lockQuery = datastore.create(LockQuery.class); // <1>
		// end::lockcommodity[]
	}

	public void execution() {
		// tag::execution[]
		final NumericProperty<Long> ID = NumericProperty.longType("id");
		final StringProperty VALUE = StringProperty.create("value");

		Datastore datastore = getJdbcDatastore();

		Optional<PropertyBox> result = datastore.create(LockQuery.class) // <1>
				.target(DataTarget.named("test")).filter(ID.eq(1L)) //
				.lock() // <2>
				.findOne(ID, VALUE); // <3>

		result = datastore.create(LockQuery.class).target(DataTarget.named("test")) //
				.filter(ID.eq(1L)).lock(3000) // <4>
				.findOne(ID, VALUE);

		boolean lockAcquired = datastore.create(LockQuery.class).target(DataTarget.named("test")).filter(ID.eq(1L))
				.tryLock(0); // <5>
		// end::execution[]
	}

	private static Datastore getJdbcDatastore() {
		return null;
	}

}
