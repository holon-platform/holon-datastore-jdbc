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
package com.holonplatform.datastore.jdbc.spring.internal;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.internal.DefaultJdbcDatastore;
import com.holonplatform.spring.internal.datastore.DatastoreInitializer;

/**
 * Spring {@link JdbcDatastore} implementation.
 *
 * @since 5.0.0
 */
public class DefaultSpringJdbcDatastore extends DefaultJdbcDatastore
		implements InitializingBean, BeanNameAware, BeanFactoryAware, BeanClassLoaderAware {

	private static final long serialVersionUID = -3784286815816600405L;

	/**
	 * Bean name
	 */
	private String beanName;

	/**
	 * ClassLoader
	 */
	private transient volatile ClassLoader classLoader;

	/**
	 * BeanFactory
	 */
	private transient volatile BeanFactory beanFactory;

	/**
	 * Constructor. Auto-initialization is disabled and triggered at {@link #afterPropertiesSet()} execution.
	 */
	public DefaultSpringJdbcDatastore() {
		super(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		
		// intialization
		initialize(classLoader);

		// configuration
		DatastoreInitializer.configureDatastore(this, beanName, beanFactory);

		LOGGER.info("JDBC Datastore initialized - using dialect [" + getDialect().getClass().getName() + "]");
	}

}
