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
package com.holonplatform.datastore.jdbc.spring.boot.internal;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.datastore.jdbc.spring.internal.JdbcDatastoreRegistrar;
import com.holonplatform.jdbc.spring.internal.DataSourceFactoryBean;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.DataContextBoundBeanDefinition;
import com.holonplatform.spring.internal.PrimaryMode;

/**
 * Registrar for JDBC {@link Datastore} beans registration.
 * 
 * @since 5.0.0
 */
public class JdbcDatastoreAutoConfigurationRegistrar
		implements ImportBeanDefinitionRegistrar, BeanFactoryAware, BeanClassLoaderAware, EnvironmentAware {

	/**
	 * Bean factory
	 */
	private BeanFactory beanFactory;

	/**
	 * Beans ClassLoader
	 */
	private ClassLoader beanClassLoader;

	/**
	 * Environment
	 */
	private Environment environment;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
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
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
		if (beanFactory instanceof ListableBeanFactory) {
			// Get DataSource bean names
			List<String> dataSourceBeanNames = BeanRegistryUtils.getBeanNames((ListableBeanFactory) beanFactory,
					DataSource.class);
			List<String> dataSourceFactoryBeanNames = BeanRegistryUtils.getBeanNames((ListableBeanFactory) beanFactory,
					DataSourceFactoryBean.class);
			final List<String> dsBeanNames = new ArrayList<>(
					dataSourceBeanNames.size() + dataSourceFactoryBeanNames.size());
			dataSourceBeanNames.forEach(n -> dsBeanNames.add(n));
			dataSourceFactoryBeanNames.forEach(n -> {
				if (!dsBeanNames.contains(n)) {
					dsBeanNames.add(n);
				}
			});

			for (String dsBeanName : dsBeanNames) {
				// get data context id, if available
				String dataContextId = null;
				BeanDefinition bd = registry.getBeanDefinition(dsBeanName);
				if (bd instanceof DataContextBoundBeanDefinition) {
					dataContextId = ((DataContextBoundBeanDefinition) bd).getDataContextId().orElse(null);
				}

				// register JDBC datastore
				JdbcDatastoreRegistrar.registerDatastore(registry, environment, dataContextId, PrimaryMode.AUTO,
						dsBeanName, null, true, beanClassLoader);
			}
		}
	}

}