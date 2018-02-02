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
package com.holonplatform.datastore.jdbc.composer.expression;

import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.internal.expression.DefaultSQLProjection;

/**
 * {@link Expression} which represents a SQL query projection, providing the selection labels and an optional
 * {@link SQLResultConverter}.
 * 
 * @param <R> Projection result type
 *
 * @since 5.1.0
 */
public interface SQLProjection<R> extends TypedExpression<R> {

	/**
	 * Get the projection selection names.
	 * @return Projection selection names
	 */
	List<String> getSelection();

	/**
	 * Get the alias for given projection selection name, if defined.
	 * @param selection Selection name
	 * @return Selection alias, an empty Optional if not available
	 */
	Optional<String> getSelectionAlias(String selection);

	/**
	 * Get the SQL result converter to be used with this projection, if available.
	 * @return Optional projection {@link SQLResultConverter}
	 */
	Optional<SQLResultConverter<R>> getConverter();

	// builders

	/**
	 * Create a new {@link SQLProjection}.
	 * @param <R> Projection type
	 * @param projectionType Projection type (not null)
	 * @return A new {@link MutableSQLProjection} builder to configure selection and converter
	 */
	static <R> MutableSQLProjection<R> create(Class<? extends R> projectionType) {
		return create(projectionType, 0);
	}

	/**
	 * Create a new {@link SQLProjection}.
	 * @param <R> Projection type
	 * @param projectionType Projection type (not null)
	 * @param aliasMainSequence Generated alias main sequence, which will be appended to any generated selection alias
	 *        name
	 * @return A new {@link MutableSQLProjection} builder to configure selection and converter
	 */
	static <R> MutableSQLProjection<R> create(Class<? extends R> projectionType, int aliasMainSequence) {
		return new DefaultSQLProjection<>(projectionType, aliasMainSequence);
	}

	/**
	 * Create a new {@link SQLProjection}.
	 * @param <R> Projection type
	 * @param projectionType Projection type (not null)
	 * @param context SQL context from which to obtain the generated alias main sequence, which will be appended to any
	 *        generated selection alias name
	 * @return A new {@link MutableSQLProjection} builder to configure selection and converter
	 */
	static <R> MutableSQLProjection<R> create(Class<? extends R> projectionType, SQLCompositionContext context) {
		return new DefaultSQLProjection<>(projectionType, context);
	}

	/**
	 * A mutable {@link SQLProjection} to configure selection expressions and converter.
	 *
	 * @param <R> Projection type
	 */
	public interface MutableSQLProjection<R> extends SQLProjection<R> {

		/**
		 * Add a selection expression.
		 * @param selection Selection to add (not null)
		 * @param generateAlias Whether to generate an alias label for the selection expression
		 * @return If <code>generateAlias</code> was <code>true</code>, the generated selection alias. Given
		 *         <code>selection</code> otherwise.
		 */
		String addSelection(String selection, boolean generateAlias);

		/**
		 * Add a selection expression, with selection alias auto-generation.
		 * @param selection Selection to add (not null)
		 * @param generateAlias Whether to generate an alias label for the selection expression
		 * @return The generated selection alias
		 */
		default String addSelection(String selection) {
			return addSelection(selection, true);
		}

		/**
		 * Add a selection expression with selection alias.
		 * @param selection Selection to add (not null)
		 * @param alias Selection alias (not null)
		 */
		void addSelection(String selection, String alias);

		/**
		 * Set the result converter.
		 * @param converter the {@link SQLResultConverter} to set
		 */
		void setConverter(SQLResultConverter<R> converter);

	}

}
