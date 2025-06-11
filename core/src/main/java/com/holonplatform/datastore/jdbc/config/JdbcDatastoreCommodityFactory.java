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

import java.util.ServiceLoader;

import jakarta.annotation.Priority;

import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;

/**
 * JDBC {@link DatastoreCommodityFactory} extension type to allow automatic registration using Java
 * {@link ServiceLoader} extension, through a
 * <code>com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory</code> file under the
 * <code>META-INF/services</code> folder.
 * <p>
 * A {@link Priority} annotation can be used on concrete {@link JdbcDatastoreCommodityFactory} implementation class to
 * define a registration order, where a lower value means higher priority value.
 * </p>
 * 
 * @param <C> Actual commodity type
 *
 * @since 5.0.0
 */
public interface JdbcDatastoreCommodityFactory<C extends DatastoreCommodity>
		extends DatastoreCommodityFactory<JdbcDatastoreCommodityContext, C> {

}
