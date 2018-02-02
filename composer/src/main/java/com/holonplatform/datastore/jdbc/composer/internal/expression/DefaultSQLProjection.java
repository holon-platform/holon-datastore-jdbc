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
package com.holonplatform.datastore.jdbc.composer.internal.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jdbc.composer.SQLCompositionContext;
import com.holonplatform.datastore.jdbc.composer.SQLResultConverter;
import com.holonplatform.datastore.jdbc.composer.SQLStatementCompositionContext;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection;
import com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection;

/**
 * Default {@link SQLProjection} implementation.
 * 
 * @param <R> Projection type
 * 
 * @since 5.1.0
 */
public class DefaultSQLProjection<R> implements MutableSQLProjection<R> {

	/**
	 * Allowed SQL-safe alias characters
	 */
	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	/**
	 * Projection type
	 */
	private final Class<? extends R> projectionType;

	/**
	 * Alias main sequence
	 */
	private final int aliasMainSequence;

	/**
	 * Selection SQL expressions
	 */
	private final List<String> selections = new ArrayList<>();

	/**
	 * Selection SQL expressions aliases
	 */
	private final Map<String, String> aliases = new HashMap<>();

	/**
	 * Query results converter
	 */
	private SQLResultConverter<R> converter;

	// Alias generation

	private int aliasCounter = 0;
	private Map<String, Integer> generatedSelection = new HashMap<>();
	private Map<String, Integer> generatedAlias = new HashMap<>();

	/**
	 * Constructor
	 * @param projectionType Projection type (not null)
	 * @param context SQL context from which to obtain the generated alias main sequence, which will be appended to any
	 *        generated selection alias name
	 */
	public DefaultSQLProjection(Class<? extends R> projectionType, SQLCompositionContext context) {
		ObjectUtils.argumentNotNull(projectionType, "Projection type must be not null");
		ObjectUtils.argumentNotNull(context, "SQLCompositionContext must be not null");
		this.projectionType = projectionType;
		this.aliasMainSequence = SQLCompositionContext.getContextSequence(context,
				SQLStatementCompositionContext.class);
	}

	/**
	 * Constructor
	 * @param projectionType Projection type (not null)
	 * @param aliasMainSequence Selection expressions alias main sequence, which will be appended to the alias name when
	 *        it is auto-generated
	 */
	public DefaultSQLProjection(Class<? extends R> projectionType, int aliasMainSequence) {
		super();
		ObjectUtils.argumentNotNull(projectionType, "Projection type must be not null");
		this.projectionType = projectionType;
		this.aliasMainSequence = aliasMainSequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.TypedExpression#getType()
	 */
	@Override
	public Class<? extends R> getType() {
		return projectionType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLProjection#getSelection()
	 */
	@Override
	public List<String> getSelection() {
		return selections;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLProjection#getSelectionAlias(java.lang.String)
	 */
	@Override
	public Optional<String> getSelectionAlias(String selection) {
		return Optional.ofNullable(aliases.get(selection));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection#setConverter(com.
	 * holonplatform.datastore.jdbc.composer.SQLResultConverter)
	 */
	@Override
	public void setConverter(SQLResultConverter<R> converter) {
		this.converter = converter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.composer.expression.SQLProjection#getConverter()
	 */
	@Override
	public Optional<SQLResultConverter<R>> getConverter() {
		return Optional.ofNullable(converter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getSelection() == null || getSelection().isEmpty()) {
			throw new InvalidExpressionException("Null or empty selection");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection#addSelection(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public void addSelection(String selection, String alias) {
		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		if (selection.trim().equals("")) {
			throw new IllegalArgumentException("Selection must be not empty");
		}
		ObjectUtils.argumentNotNull(alias, "Selection alias must be not null");

		selections.add(selection);
		aliases.put(selection, alias);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jdbc.composer.expression.SQLProjection.MutableSQLProjection#addSelection(java.lang.
	 * String, boolean)
	 */
	@Override
	public String addSelection(String selection, boolean generateAlias) {

		ObjectUtils.argumentNotNull(selection, "Selection must be not null");
		if (selection.trim().equals("")) {
			throw new IllegalArgumentException("Selection must be not empty");
		}

		selections.add(selection);

		if (generateAlias) {
			String alias = generateAlias(selection);
			aliases.put(selection, alias);
			return alias;
		} else {
			return selection;
		}
	}

	/**
	 * Generate an alias name for given selection expression
	 * @param selectionExpression Selection expression for which to generate the alias
	 * @return Generated alias
	 */
	protected String generateAlias(String selectionExpression) {

		String selection = selectionExpression;
		int idx = selectionExpression.lastIndexOf('.');
		if (selectionExpression.indexOf('(') < 0 && idx > -1 && idx < (selectionExpression.length() - 1)) {
			selection = selectionExpression.substring(idx + 1, selectionExpression.length());
		}

		aliasCounter++;

		StringBuilder sb = new StringBuilder();

		String prefix = ((selection.length() <= 4) ? selection : selection.substring(0, 4)).toLowerCase();
		char[] pa = prefix.toCharArray();
		char[] sanitized = new char[pa.length];
		for (int i = 0; i < pa.length; i++) {
			sanitized[i] = (ALIAS_CHARS.indexOf(pa[i]) > -1) ? pa[i] : '_';
		}

		sb.append(sanitized);

		if (aliasMainSequence > 0) {
			sb.append(aliasMainSequence);
			sb.append("_");
		}

		sb.append(aliasCounter);
		sb.append("_");

		int subcount = 0;
		if (generatedSelection.containsKey(selection)) {
			subcount = generatedSelection.get(selection) + 1;
		}
		generatedSelection.put(selection, subcount);
		sb.append(subcount);

		String partialAlias = sb.toString();

		int duplicateCount = 0;
		if (generatedAlias.containsKey(partialAlias)) {
			duplicateCount = generatedAlias.get(partialAlias) + 1;
		}
		generatedAlias.put(partialAlias, duplicateCount);

		if (duplicateCount > 0) {
			sb.append("_");
			sb.append(duplicateCount);
		}

		return sb.toString();
	}

}
