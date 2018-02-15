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
package com.holonplatform.datastore.jdbc.spring;

import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.spring.internal.PrimaryMode;

/**
 * A {@link ConfigPropertySet} for JDBC Datastore configuration, using {@link #DEFAULT_NAME} as property prefix.
 *
 * @since 5.1.0
 */
public interface JdbcDatastoreConfigProperties extends ConfigPropertySet, DataContextBound {

	/**
	 * Configuration property set default name
	 */
	static final String DEFAULT_NAME = "holon.datastore.jdbc";

	/**
	 * The database platform to use. Must be one of the names enumerated in {@link DatabasePlatform}.
	 * <p>
	 * Auto-detected by default.
	 * </p>
	 */
	static final ConfigProperty<DatabasePlatform> PLATFORM = ConfigProperty.create("platform", DatabasePlatform.class);

	/**
	 * Whether to qualify the Datastore bean as <code>primary</code>, i.e. the preferential bean to be injected in a
	 * single-valued dependency when multiple candidates are present.
	 * <p>
	 * When mode is {@link PrimaryMode#AUTO} (default mode), the registred Datastore bean is marked as primary only when
	 * the {@link DataSource} bean to which is bound is registered as primary bean.
	 * </p>
	 */
	static final ConfigProperty<PrimaryMode> PRIMARY_MODE = ConfigProperty.create("primary-mode", PrimaryMode.class);

	/**
	 * Whether to add {@link Transactional} behaviour to transactional Datastore methods, to automatically create or
	 * partecipate in a transaction when methods are invoked. Affected methods are: <code>refresh</code>,
	 * <code>insert</code>, <code>update</code>, <code>save</code>, <code>delete</code>.
	 */
	static final ConfigProperty<Boolean> TRANSACTIONAL = ConfigProperty.create("transactional", Boolean.class);

	/**
	 * The {@link IdentifierResolutionStrategy} to use to resolve {@link PropertyBox} identifiers.
	 */
	static final ConfigProperty<IdentifierResolutionStrategy> IDENTIFIER_RESOLUTION_STRATEGY = ConfigProperty
			.create("identifier-resolution-strategy", IdentifierResolutionStrategy.class);

	/**
	 * Builder to create property set instances bound to a property data source.
	 * @param dataContextId Optional data context id to which {@link Datastore} is bound
	 * @return ConfigPropertySet builder
	 */
	static Builder<JdbcDatastoreConfigProperties> builder(String dataContextId) {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JdbcDatastoreConfigPropertiesImpl(dataContextId));
	}

	/**
	 * Builder to create property set instances bound to a property data source, without data context id specification.
	 * @return ConfigPropertySet builder
	 */
	static Builder<JdbcDatastoreConfigProperties> builder() {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new JdbcDatastoreConfigPropertiesImpl(null));
	}

	/**
	 * Default implementation
	 */
	static class JdbcDatastoreConfigPropertiesImpl extends DefaultConfigPropertySet
			implements JdbcDatastoreConfigProperties {

		private final String dataContextId;

		public JdbcDatastoreConfigPropertiesImpl(String dataContextId) {
			super((dataContextId != null && !dataContextId.trim().equals("")) ? (DEFAULT_NAME + "." + dataContextId)
					: DEFAULT_NAME);
			this.dataContextId = (dataContextId != null && !dataContextId.trim().equals("")) ? dataContextId : null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.DataContextBound#getDataContextId()
		 */
		@Override
		public Optional<String> getDataContextId() {
			return Optional.ofNullable(dataContextId);
		}

	}

}
