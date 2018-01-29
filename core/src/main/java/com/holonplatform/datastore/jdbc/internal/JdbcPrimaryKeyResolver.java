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
package com.holonplatform.datastore.jdbc.internal;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jdbc.composer.SQLExecutionContext;
import com.holonplatform.datastore.jdbc.composer.SQLPrimaryKeyResolver;

/**
 * JDBC {@link SQLPrimaryKeyResolver}.
 * <p>
 * Like the default resolver, tries to obtain the primary key from {@link PropertyBox} identifier properties. If the
 * identifier properties are not available, try to obtain the primary key of the table associated to the provided
 * {@link DataTarget} from the JDBC connection. Only the primary key column names which match a {@link Path} property of
 * the {@link PropertyBox} property set will be taken into account.
 * </p>
 *
 * @since 5.1.0
 */
public class JdbcPrimaryKeyResolver implements SQLPrimaryKeyResolver {

	private final SQLExecutionContext context;

	public JdbcPrimaryKeyResolver(SQLExecutionContext context) {
		super();
		ObjectUtils.argumentNotNull(context, "SQLExecutionContext must be not null");
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.SQLPrimaryKeyResolver#getPrimaryKey(com.holonplatform.core.datastore.
	 * DataTarget, com.holonplatform.core.property.PropertyBox)
	 */
	@Override
	public Optional<Path<?>[]> getPrimaryKey(DataTarget<?> target, PropertyBox propertyBox) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the <em>primary key</em> paths for given <code>tableName</code>, if available.
	 * @param tableName Table name for which to retrieve the primary key (not null)
	 * @return Table primary key paths, or empty if the primary key is not available
	 * @throws SQLException If a database error occurred
	 */
	Optional<Path<?>[]> getPrimaryKey(String table) throws SQLException {
		
		final String tableName = context.getDialect().getTableName(table);

		return context.withConnection(connection -> {

			List<OrderedPath> paths = new ArrayList<>();

			DatabaseMetaData databaseMetaData = connection.getMetaData();

			try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
				while (resultSet.next()) {
					final String columnName = resultSet.getString("COLUMN_NAME");
					OrderedPath op = new OrderedPath();
					op.path = Path.of(columnName,
							JdbcDatastoreUtils.getColumnType(databaseMetaData, tableName, columnName));
					op.sequence = resultSet.getShort("KEY_SEQ");
					paths.add(op);
				}
			}

			if (!paths.isEmpty()) {
				Collections.sort(paths);
				return Optional.of(
						paths.stream().map(p -> p.path).collect(Collectors.toList()).toArray(new Path[paths.size()]));
			}

			return Optional.empty();

		});
	}

	/**
	 * Support class to order {@link Path}s using a sequence.
	 */
	private static class OrderedPath implements Comparable<OrderedPath> {

		@SuppressWarnings("rawtypes")
		Path path;
		short sequence;

		@Override
		public int compareTo(OrderedPath o) {
			return ((Short) sequence).compareTo(o.sequence);
		}

	}

}
