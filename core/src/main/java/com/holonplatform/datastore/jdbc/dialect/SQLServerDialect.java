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
package com.holonplatform.datastore.jdbc.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jdbc.JdbcDatastore;
import com.holonplatform.datastore.jdbc.JdbcDialect;
import com.holonplatform.datastore.jdbc.JdbcDialect.SQLFunction.DefaultFunction;
import com.holonplatform.datastore.jdbc.internal.JdbcQueryClauses;
import com.holonplatform.datastore.jdbc.internal.dialect.SQLValueSerializer;
import com.holonplatform.datastore.jdbc.internal.expressions.JdbcResolutionContext.ResolutionQueryClause;
import com.holonplatform.datastore.jdbc.internal.support.ParameterValue;

/**
 * MSSQL {@link JdbcDialect}.
 *
 * @since 5.0.0
 */
public class SQLServerDialect implements JdbcDialect {

	private static final long serialVersionUID = -3585193712573424374L;

	private static final SQLFunction AVG_FUNCTION = new AvgFunction();

	private static final SQLServerParameterProcessor PARAMETER_PROCESSOR = new SQLServerParameterProcessor();

	private static final SQLServer2012LimitHandler LIMIT_HANDLER_2012 = new SQLServer2012LimitHandler();

	private boolean supportsGeneratedKeys;
	private boolean generatedKeyAlwaysReturned;
	private boolean supportsLikeEscapeClause;

	private boolean version2012orHigher;

	private final StatementConfigurator statementConfigurator;

	public SQLServerDialect() {
		super();
		this.statementConfigurator = StatementConfigurator.create(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#init(com.holonplatform.datastore.jdbc.JdbcDatastore)
	 */
	@Override
	public void init(JdbcDatastore datastore) throws SQLException {
		datastore.withConnection(c -> {
			DatabaseMetaData databaseMetaData = c.getMetaData();
			supportsGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
			generatedKeyAlwaysReturned = databaseMetaData.generatedKeyAlwaysReturned();
			supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
			int version = databaseMetaData.getDatabaseMajorVersion();
			version2012orHigher = version >= 0 && version >= 11;
			return null;
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getStatementConfigurator()
	 */
	@Override
	public StatementConfigurator getStatementConfigurator() {
		return statementConfigurator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getParameterProcessor()
	 */
	@Override
	public Optional<SQLParameterProcessor> getParameterProcessor() {
		return Optional.of(PARAMETER_PROCESSOR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getFunction(java.lang.String)
	 */
	@Override
	public SQLFunction getFunction(String name) {
		if (DefaultFunction.AVG.getName().equals(name)) {
			return AVG_FUNCTION;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#supportsLikeEscapeClause()
	 */
	@Override
	public boolean supportsLikeEscapeClause() {
		return supportsLikeEscapeClause;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#supportsGetGeneratedKeys()
	 */
	@Override
	public boolean supportsGetGeneratedKeys() {
		return supportsGeneratedKeys;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() {
		return generatedKeyAlwaysReturned;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#deleteStatementAliasSupported()
	 */
	@Override
	public boolean deleteStatementAliasSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.JdbcDialect#getLimitHandler()
	 */
	@Override
	public Optional<LimitHandler> getLimitHandler() {
		return version2012orHigher ? Optional.of(LIMIT_HANDLER_2012) : Optional.empty();
	}

	@SuppressWarnings("serial")
	private static final class AvgFunction implements SQLFunction {

		@Override
		public String getName() {
			return "avg";
		}

		@Override
		public boolean hasParenthesesIfNoArguments() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.datastore.jdbc.internal.SQLFunction#serialize(java.util.List)
		 */
		@Override
		public String serialize(List<String> arguments) {
			final StringBuilder sb = new StringBuilder();
			sb.append(getName());
			sb.append("(");
			sb.append("cast(");
			sb.append(arguments.get(0));
			sb.append(" as decimal)");
			sb.append(")");
			return sb.toString();
		}

	}

	private static final class SQLServerParameterProcessor implements SQLParameterProcessor {

		@Override
		public String processParameter(String serialized, ParameterValue parameter, DialectResolutionContext context) {
			if (context.getResolutionQueryClause().isPresent()
					&& (context.getResolutionQueryClause().get() == ResolutionQueryClause.WHERE)) {
				TemporalType temporalType = parameter.getTemporalType().orElse(null);
				if (temporalType != null) {
					if (TemporalType.TIME == temporalType) {
						Optional<String> serializedTime = SQLValueSerializer.serializeDate(parameter.getValue(),
								temporalType);
						if (serializedTime.isPresent()) {
							context.replaceParameter(serialized,
									ParameterValue.create(String.class, serializedTime.get(), temporalType));
							return serialized;
						}
					}
				}
			}
			return serialized;
		}

	}

	@SuppressWarnings("serial")
	private static final class SQLServer2012LimitHandler implements LimitHandler {

		@Override
		public String limitResults(JdbcQueryClauses query, String serializedSql, int limit, int offset) {
			return serializedSql + ((offset > -1) ? (" OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY")
					: (" FETCH FIRST " + limit + " ROWS ONLY"));
		}

	}

}
