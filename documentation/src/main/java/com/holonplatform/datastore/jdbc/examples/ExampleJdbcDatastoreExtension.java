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

import javax.sql.DataSource;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCommodity;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.query.Query;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.composer.SQLDialect;
import com.holonplatform.datastore.jdbc.composer.expression.SQLExpression;
import com.holonplatform.datastore.jdbc.composer.expression.SQLToken;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityContext;
import com.holonplatform.datastore.jdbc.config.JdbcDatastoreCommodityFactory;
import com.holonplatform.jdbc.DatabasePlatform;

@SuppressWarnings({ "unused", "serial" })
public class ExampleJdbcDatastoreExtension {

	// tag::commodity[]
	interface MyCommodity extends DatastoreCommodity { // <1>

		DatabasePlatform getPlatform();

	}

	class MyCommodityImpl implements MyCommodity { // <2>

		private final DatabasePlatform platform;

		public MyCommodityImpl(DatabasePlatform platform) {
			super();
			this.platform = platform;
		}

		@Override
		public DatabasePlatform getPlatform() {
			return platform;
		}

	}

	class MyCommodityFactory implements JdbcDatastoreCommodityFactory<MyCommodity> { // <3>

		@Override
		public Class<? extends MyCommodity> getCommodityType() {
			return MyCommodity.class;
		}

		@Override
		public MyCommodity createCommodity(JdbcDatastoreCommodityContext context)
				throws CommodityConfigurationException {
			// examples of configuration attributes:
			DataSource dataSource = context.getDataSource();
			SQLDialect dialect = context.getDialect();
			return new MyCommodityImpl(context.getDatabase().orElse(DatabasePlatform.NONE));
		}

	}
	// end::commodity[]

	public void commodityFactory() {
		// tag::factoryreg[]
		Datastore datastore = JdbcDatastore.builder() //
				.withCommodity(new MyCommodityFactory()) // <1>
				.build();
		// end::factoryreg[]
	}

	class MyExpressionResolver implements ExpressionResolver<SQLToken, SQLToken> {

		@Override
		public Optional<SQLToken> resolve(SQLToken expression, ResolutionContext context)
				throws InvalidExpressionException {
			return Optional.of(expression);
		}

		@Override
		public Class<? extends SQLToken> getExpressionType() {
			return SQLToken.class;
		}

		@Override
		public Class<? extends SQLToken> getResolvedType() {
			return SQLToken.class;
		}

	}

	public void expressionResolverRegistration() {
		// tag::expreg1[]
		Datastore datastore = JdbcDatastore.builder() //
				.withExpressionResolver(new MyExpressionResolver()) // <1>
				.build();
		// end::expreg1[]

		// tag::expreg2[]
		datastore.addExpressionResolver(new MyExpressionResolver()); // <2>
		// end::expreg2[]

		// tag::expreg3[]
		long result = datastore.query().target(DataTarget.named("test")) //
				.withExpressionResolver(new MyExpressionResolver()) // <1>
				.count();
		// end::expreg3[]
	}

	// tag::expres1[]
	class KeyIs implements QueryFilter {

		private final Long value;

		public KeyIs(Long value) {
			this.value = value;
		}

		public Long getValue() {
			return value;
		}

		@Override
		public void validate() throws InvalidExpressionException {
			if (value == null) {
				throw new InvalidExpressionException("Kay value must be not null");
			}
		}

	}
	// end::expres1[]

	public void expres2() {
		// tag::expres2[]
		final ExpressionResolver<KeyIs, SQLExpression> keyIsResolver = ExpressionResolver.create( //
				KeyIs.class, // <1>
				SQLExpression.class, // <2>
				(keyIs, ctx) -> Optional.of(SQLExpression.create("key = " + keyIs.getValue()))); // <3>
		// end::expres2[]

		// tag::expres3[]
		Datastore datastore = JdbcDatastore.builder().withExpressionResolver(keyIsResolver) // <1>
				.build();

		Query query = datastore.query().filter(new KeyIs(1L)); // <2>
		// end::expres3[]
	}

}
