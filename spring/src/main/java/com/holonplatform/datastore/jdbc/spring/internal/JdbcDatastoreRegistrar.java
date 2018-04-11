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

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.config.IdentifierResolutionStrategy;
import com.holonplatform.datastore.jdbc.internal.JdbcDatastoreLogger;
import com.holonplatform.datastore.jdbc.spring.EnableJdbcDatastore;
import com.holonplatform.datastore.jdbc.spring.JdbcDatastoreConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jdbc.spring.SpringJdbcConnectionHandler;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;
import com.holonplatform.spring.PrimaryMode;
import com.holonplatform.spring.internal.AbstractConfigPropertyRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.GenericDataContextBoundBeanDefinition;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.TypeCache.Sort;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Registrar for JDBC {@link Datastore} bean registration using {@link EnableJdbcDatastore} annotation.
 * 
 * @since 5.0.0
 */
public class JdbcDatastoreRegistrar extends AbstractConfigPropertyRegistrar implements BeanClassLoaderAware {

	/*
	 * Logger
	 */
	private static final Logger LOGGER = JdbcDatastoreLogger.create();

	/**
	 * Datastore enhanced classes cache
	 */
	private static final TypeCache<String> DATASTORE_PROXY_CACHE = new TypeCache<>(Sort.WEAK);

	/**
	 * Beans class loader
	 */
	private ClassLoader beanClassLoader;

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
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
		if (!annotationMetadata.isAnnotated(EnableJdbcDatastore.class.getName())) {
			// ignore call from sub classes
			return;
		}

		Map<String, Object> attributes = annotationMetadata
				.getAnnotationAttributes(EnableJdbcDatastore.class.getName());

		// attributes
		String dataContextId = BeanRegistryUtils.getAnnotationValue(attributes, "dataContextId", null);
		String dataSourceReference = BeanRegistryUtils.getAnnotationValue(attributes, "dataSourceReference", null);

		String dataSourceBeanName = dataSourceReference;
		if (dataSourceBeanName == null) {
			dataSourceBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
					EnableDataSource.DEFAULT_DATASOURCE_BEAN_NAME);
		}

		// defaults
		JdbcDatastoreConfigProperties defaultConfig = JdbcDatastoreConfigProperties.builder(dataContextId)
				.withProperty(JdbcDatastoreConfigProperties.PRIMARY_MODE,
						BeanRegistryUtils.getAnnotationValue(attributes, "primary", PrimaryMode.AUTO))
				.withProperty(JdbcDatastoreConfigProperties.PLATFORM,
						BeanRegistryUtils.getAnnotationValue(attributes, "platform", DatabasePlatform.NONE))
				.withProperty(JdbcDatastoreConfigProperties.TRANSACTIONAL,
						BeanRegistryUtils.getAnnotationValue(attributes, "transactional", true))
				.withProperty(JdbcDatastoreConfigProperties.IDENTIFIER_RESOLUTION_STRATEGY,
						BeanRegistryUtils.getAnnotationValue(attributes, "identifierResolutionStrategy",
								IdentifierResolutionStrategy.AUTO))
				.build();

		// register Datastore
		registerDatastore(registry, getEnvironment(), dataContextId, dataSourceBeanName, defaultConfig,
				beanClassLoader);
	}

	/**
	 * Register a {@link JdbcDatastore} bean
	 * @param registry BeanDefinitionRegistry
	 * @param environment Spring environment
	 * @param dataContextId Data context id
	 * @param datasourceBeanName DataSource bean name reference
	 * @param defaultConfig Default configuration properties
	 * @param beanClassLoader Bean class loader
	 * @return Registered Datastore bean name
	 */
	public static String registerDatastore(BeanDefinitionRegistry registry, Environment environment,
			String dataContextId, String datasourceBeanName, JdbcDatastoreConfigProperties defaultConfig,
			ClassLoader beanClassLoader) {

		// Datastore configuration
		DatastoreConfigProperties datastoreConfig = DatastoreConfigProperties.builder(dataContextId)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();

		// JDBC Datastore configuration
		JdbcDatastoreConfigProperties jdbcDatastoreConfig = JdbcDatastoreConfigProperties.builder(dataContextId)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();

		// Configuration
		PrimaryMode primaryMode = defaultConfig
				.getConfigPropertyValueOrElse(JdbcDatastoreConfigProperties.PRIMARY_MODE,
						() -> jdbcDatastoreConfig.getConfigPropertyValue(JdbcDatastoreConfigProperties.PRIMARY_MODE))
				.orElse(PrimaryMode.AUTO);

		DatabasePlatform platform = defaultConfig
				.getConfigPropertyValueOrElse(JdbcDatastoreConfigProperties.PLATFORM,
						() -> jdbcDatastoreConfig.getConfigPropertyValue(JdbcDatastoreConfigProperties.PLATFORM))
				.orElse(DatabasePlatform.NONE);

		boolean transactional = defaultConfig
				.getConfigPropertyValueOrElse(JdbcDatastoreConfigProperties.TRANSACTIONAL,
						() -> jdbcDatastoreConfig.getConfigPropertyValue(JdbcDatastoreConfigProperties.TRANSACTIONAL))
				.orElse(true);

		IdentifierResolutionStrategy identifierResolutionStrategy = defaultConfig
				.getConfigPropertyValueOrElse(JdbcDatastoreConfigProperties.IDENTIFIER_RESOLUTION_STRATEGY,
						() -> jdbcDatastoreConfig
								.getConfigPropertyValue(JdbcDatastoreConfigProperties.IDENTIFIER_RESOLUTION_STRATEGY))
				.orElse(IdentifierResolutionStrategy.AUTO);

		// check primary
		boolean primary = PrimaryMode.TRUE == primaryMode;
		if (!primary && PrimaryMode.AUTO == primaryMode) {
			if (registry.containsBeanDefinition(datasourceBeanName)) {
				BeanDefinition bd = registry.getBeanDefinition(datasourceBeanName);
				primary = bd.isPrimary();
			}
		}

		// create bean definition
		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);

		final Class<?> datastoreClass = transactional
				? addTransactionalAnnotations(DefaultSpringJdbcDatastore.class, dataContextId, beanClassLoader)
				: DefaultSpringJdbcDatastore.class;

		definition.setBeanClass(datastoreClass);

		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setDependsOn(datasourceBeanName);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		String beanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableJdbcDatastore.DEFAULT_DATASTORE_BEAN_NAME);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("dataSource", new RuntimeBeanReference(datasourceBeanName));
		pvs.add("identifierResolutionStrategy", identifierResolutionStrategy);
		pvs.add("connectionHandler", SpringJdbcConnectionHandler.create());

		if (platform != null && platform != DatabasePlatform.NONE) {
			pvs.add("database", platform);
		}

		if (datastoreConfig != null) {
			if (datastoreConfig.isTrace()) {
				pvs.add("traceEnabled", Boolean.TRUE);
			}
			String dialectClassName = datastoreConfig.getDialect();
			if (dialectClassName != null) {
				try {
					SQLDialect dialect = (SQLDialect) Class.forName(dialectClassName).newInstance();
					if (dialect != null) {
						pvs.add("dialect", dialect);
					}
				} catch (Exception e) {
					throw new BeanCreationException(beanName,
							"Failed to load JdbcDialect class using name [" + dialectClassName + "]", e);
				}
			}
		}

		definition.setPropertyValues(pvs);

		registry.registerBeanDefinition(beanName, definition);

		// log
		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered JDBC Datastore bean with name \"");
		log.append(beanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		log.append(" bound to DataSource bean: ");
		log.append(datasourceBeanName);
		LOGGER.info(log.toString());

		return beanName;

	}

	private static final ElementMatcher<MethodDescription> TRANSACTIONAL_METHOD_NAMES = ElementMatchers.named("save")
			.or(ElementMatchers.named("delete")).or(ElementMatchers.named("insert"))
			.or(ElementMatchers.named("update"));

	private static final ElementMatcher<MethodDescription> TRANSACTIONAL_METHODS = ElementMatchers.isPublic()
			.and(TRANSACTIONAL_METHOD_NAMES);

	/**
	 * Add Spring {@link Transactional} annotation to the Datastore class suitable methods.
	 * @param datastoreClass Datastore class
	 * @param dataContextId Data context id
	 * @param classLoader Datastore class ClassLoader
	 * @return Modified Datastore class
	 */
	private synchronized static <T extends Datastore> Class<?> addTransactionalAnnotations(
			Class<? extends T> datastoreClass, String dataContextId, ClassLoader classLoader) {

		// Proxy class name
		final StringBuilder nameBuilder = new StringBuilder();
		nameBuilder.append(datastoreClass.getName());
		nameBuilder.append("$Proxy$");
		if (dataContextId != null) {
			nameBuilder.append(dataContextId);
		} else {
			nameBuilder.append("default");
		}
		nameBuilder.append("$");
		nameBuilder.append(classLoader.hashCode());

		final String proxyName = nameBuilder.toString();

		// check cache
		Class<?> cached = DATASTORE_PROXY_CACHE.find(classLoader, proxyName);
		if (cached != null) {
			return cached;
		}

		try {

			// Transactional annotation
			AnnotationDescription.Builder annotationBuilder = AnnotationDescription.Builder.ofType(Transactional.class);
			if (dataContextId != null) {
				annotationBuilder = annotationBuilder.define("value", dataContextId);
			}
			final AnnotationDescription transactionalAnnotation = annotationBuilder.build();

			// Build proxy class
			Class<?> proxy = new ByteBuddy().subclass(datastoreClass).name(proxyName).method(TRANSACTIONAL_METHODS)
					.intercept(SuperMethodCall.INSTANCE).annotateMethod(transactionalAnnotation).make()
					.load(classLoader, ClassLoadingStrategy.Default.INJECTION).getLoaded();

			DATASTORE_PROXY_CACHE.insert(classLoader, proxyName, proxy);

			return proxy;

		} catch (Exception e) {
			LOGGER.warn("Failed to enhance datastore class [" + datastoreClass.getName()
					+ "] with transactional annotations", e);
		}

		return datastoreClass;
	}

}
