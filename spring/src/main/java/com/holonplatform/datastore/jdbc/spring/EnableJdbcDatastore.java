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
package com.holonplatform.datastore.jdbc.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.internal.JdbcDatastoreRegistrar;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.spring.internal.PrimaryMode;

/**
 * Annotation to be used on Spring Configuration classes to setup a JDBC {@link Datastore}.
 *
 * @since 5.0.0
 * 
 * @see JdbcDatastore
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JdbcDatastoreRegistrar.class)
public @interface EnableJdbcDatastore {

	/**
	 * Default {@link Datastore} registration bean name.
	 */
	public static final String DEFAULT_DATASTORE_BEAN_NAME = "jdbcDatastore";

	/**
	 * Optional data context id to use to discriminate Datastores when more than one persistence source is configured,
	 * i.e. when multiple DataSources and JPA persistence units are configured in context.
	 * <p>
	 * The configured data context id will be returned by the {@link DataContextBound#getDataContextId()} method of the
	 * registered {@link Datastore}.
	 * </p>
	 * <p>
	 * When a data context id is specified, the registered Datastore is bound to the {@link DataSource} with a matching
	 * data context id, if available. During registration phase, if the data context id is not null/empty and a
	 * {@link #dataSourceReference()} is not specified, an DataSource bean is searched in context using the bean name
	 * pattern: <code>dataSource_[datacontextid]</code> where <code>[datacontextid]</code> is equal to
	 * {@link #dataContextId()} attribute.
	 * </p>
	 * @return Data context id
	 */
	String dataContextId() default "";

	/**
	 * Configures the name of the {@link DataSource} bean definition to be used to create the {@link Datastore}
	 * registered using this annotation. See {@link #dataContextId()} for informations about DataSource bean lookup when
	 * a specific name is not configured.
	 * @return The name of the {@link DataSource} bean definition to be used to create the {@link Datastore}
	 */
	String dataSourceReference() default "";

	/**
	 * Set the database platform using the {@link DatabasePlatform} enumeration.
	 * <p>
	 * If specified, can be used by the JDBC datastore to auto-dectect a suitable dialect.
	 * </p>
	 * @return The database platform, or {@link DatabasePlatform#NONE} if not specified
	 */
	DatabasePlatform platform() default DatabasePlatform.NONE;

	/**
	 * Whether to qualify {@link Datastore} bean as <code>primary</code>, i.e. the preferential bean to be injected in a
	 * single-valued dependency when multiple candidates are present.
	 * <p>
	 * When mode is {@link PrimaryMode#AUTO}, the registred Datastore bean is marked as primary only when the
	 * {@link Datastore} bean to which is bound is registered as primary bean.
	 * </p>
	 * @return Primary mode, defaults to {@link PrimaryMode#AUTO}
	 */
	PrimaryMode primary() default PrimaryMode.AUTO;

	/**
	 * Get whether to add {@link Transactional} behaviour to transactional {@link Datastore} methods, to automatically
	 * create or partecipate in a transaction when methods are invoked. Affected methods are:
	 * <ul>
	 * <li>{@link Datastore#refresh(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox)}</li>
	 * <li>{@link Datastore#insert(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption...)}
	 * <li>{@link Datastore#update(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption...)}
	 * <li>{@link Datastore#save(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption...)}</li>
	 * <li>{@link Datastore#delete(com.holonplatform.core.datastore.DataTarget, com.holonplatform.core.property.PropertyBox, com.holonplatform.core.datastore.Datastore.WriteOption...)}</li>
	 * </ul>
	 * @return Whether to add {@link Transactional} behaviour to transactional datastore methods. Defaults to
	 *         <code>true</code>.
	 */
	boolean transactional() default true;

}
