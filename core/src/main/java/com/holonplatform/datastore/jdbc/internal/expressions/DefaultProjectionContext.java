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
package com.holonplatform.datastore.jdbc.internal.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Default {@link ProjectionContext} implementation.
 * 
 * @param <R> Projection result type
 *
 * @since 5.0.0
 */
public class DefaultProjectionContext<R> implements ProjectionContext<R> {

	/**
	 * Allowed SQL-safe alias characters
	 */
	private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyw0123456789_";

	/**
	 * Resolution context
	 */
	private final JdbcResolutionContext resolutionContext;

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
	private ResultSetConverter<R> converter;

	private int aliasCounter = 0;
	private Map<String, Integer> generatedSelection = new HashMap<>();
	private Map<String, Integer> generatedAlias = new HashMap<>();

	/**
	 * Constructor
	 * @param resolutionContext Resolution context (not null)
	 */
	public DefaultProjectionContext(JdbcResolutionContext resolutionContext) {
		super();
		ObjectUtils.argumentNotNull(resolutionContext, "Resolution context must be not null");
		this.resolutionContext = resolutionContext;
	}

	/**
	 * Add a selection expression, generating an alias label for it
	 * @param selection Selection to add (not null)
	 * @return Generated selection alias
	 */
	public String addSelection(String selection) {
		return addSelection(selection, true);
	}

	/**
	 * Add a selection expression
	 * @param selection Selection to add (not null)
	 * @param generateAlias Whether to generate an alias label for the selection expression
	 * @return If <code>generateAlias</code> was <code>true</code>, the generated selection alias. <code>null</code>
	 *         otherwise.
	 */
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

		if (resolutionContext.getSequence() > 0) {
			sb.append(resolutionContext.getSequence());
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
		sb.append("_");
		sb.append(duplicateCount);
		sb.append("_");

		return sb.toString();
	}

	/**
	 * Set the query results converter
	 * @param converter The query results converter to set
	 */
	public void setConverter(ResultSetConverter<R> converter) {
		this.converter = converter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ProjectionContext#getSelection()
	 */
	@Override
	public List<String> getSelection() {
		return selections;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.SelectionContext#getSelectionAlias(java.lang.String)
	 */
	@Override
	public Optional<String> getSelectionAlias(String selection) {
		return Optional.ofNullable(aliases.get(selection));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jdbc.internal.ProjectionContext#getConverter()
	 */
	@Override
	public ResultSetConverter<R> getConverter() {
		return converter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getConverter() == null) {
			throw new InvalidExpressionException("Null results converter");
		}
	}

}
