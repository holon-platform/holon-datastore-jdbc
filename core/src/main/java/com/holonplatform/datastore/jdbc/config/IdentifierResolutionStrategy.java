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
package com.holonplatform.datastore.jdbc.config;

import com.holonplatform.core.datastore.DatastoreOperations;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;

/**
 * Enumeration of the available {@link PropertyBox}/{@link PropertySet} identifier resolution strategies.
 * <p>
 * The identifier resolution strategy is used by the JDBC Datastore to perform operations which involve a
 * {@link PropertyBox} and for which the {@link PropertyBox} identifier properties are required to match the
 * {@link PropertyBox} data with the database table primary key.
 * </p>
 * <p>
 * The JDBC Datastore operations which require a consistent idenfier resolution are:
 * {@link DatastoreOperations#refresh(com.holonplatform.core.datastore.DataTarget, PropertyBox)},
 * {@link DatastoreOperations#save(com.holonplatform.core.datastore.DataTarget, PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)},
 * {@link DatastoreOperations#update(com.holonplatform.core.datastore.DataTarget, PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)},
 * {@link DatastoreOperations#delete(com.holonplatform.core.datastore.DataTarget, PropertyBox, com.holonplatform.core.datastore.DatastoreOperations.WriteOption...)}.
 * </p>
 *
 * @since 5.1.0
 */
public enum IdentifierResolutionStrategy {

	/**
	 * If the {@link PropertySet} bound to the {@link PropertyBox} provides provides a not empty identifier property
	 * set, use it as {@link PropertyBox} identifier. Otherwise, try to obtain the {@link PropertyBox} identifier
	 * properties from the database table primary key.
	 */
	AUTO,

	/**
	 * Use the {@link PropertySet} identifier properties as {@link PropertyBox} identifier.
	 * @see PropertySet#getIdentifiers()
	 */
	IDENTIFIER_PROPERTIES,

	/**
	 * Use the database table primary key to obtain the {@link PropertyBox} identifier.
	 */
	TABLE_PRIMARY_KEY;

}
