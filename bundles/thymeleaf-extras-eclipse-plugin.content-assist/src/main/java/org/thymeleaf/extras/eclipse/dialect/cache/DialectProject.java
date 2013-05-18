/*
 * Copyright 2013, The Thymeleaf Project (http://www.thymeleaf.org/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thymeleaf.extras.eclipse.dialect.cache;

import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Representation of a project that contains one or more files which in turn
 * contain dialect information.
 * 
 * @author Emanuel Rabina
 */
public class DialectProject {

	private final HashMap<Dialect,DialectFile> dialectfiles = new HashMap<Dialect,DialectFile>();
	private ArrayList<AttributeProcessor> attributeprocessors;
	private ArrayList<ElementProcessor> elementprocessors;
	private ArrayList<ExpressionObjectMethod> expressionobjectmethods;

	/**
	 * Adds a dialect to this project.
	 * 
	 * @param dialect
	 * @param dialectitems A list of the items in the dialect, but already
	 * 					   processed to include all the information they need
	 * 					   for content assist queries.
	 */
	void addDialect(Dialect dialect, List<DialectItem> dialectitems) {

		dialectfiles.put(dialect, new DialectFile(dialectitems));
		attributeprocessors     = null;
		elementprocessors       = null;
		expressionobjectmethods = null;
	}

	/**
	 * Return all of the attribute processors in this project.
	 * 
	 * @return List of this project's attribute processors.
	 */
	List<AttributeProcessor> getAttributeProcessors() {

		if (attributeprocessors == null) {
			attributeprocessors = new ArrayList<AttributeProcessor>();
			for (DialectFile dialectfile: dialectfiles.values()) {
				attributeprocessors.addAll(dialectfile.getAttributeProcessors());
			}
			attributeprocessors.trimToSize();
		}
		return attributeprocessors;
	}

	/**
	 * Return all of the element processors in this project.
	 * 
	 * @return List of this project's element processors.
	 */
	List<ElementProcessor> getElementProcessors() {

		if (elementprocessors == null) {
			elementprocessors = new ArrayList<ElementProcessor>();
			for (DialectFile dialectfile: dialectfiles.values()) {
				elementprocessors.addAll(dialectfile.getElementProcessors());
			}
			elementprocessors.trimToSize();
		}
		return elementprocessors;
	}

	/**
	 * Return all of the expression object methods in this project.
	 * 
	 * @return List of this project's expression object methods.
	 */
	List<ExpressionObjectMethod> getExpressionObjectMethods() {

		if (expressionobjectmethods == null) {
			expressionobjectmethods = new ArrayList<ExpressionObjectMethod>();
			for (DialectFile dialectfile: dialectfiles.values()) {
				expressionobjectmethods.addAll(dialectfile.getExpressionObjectMethods());
			}
			expressionobjectmethods.trimToSize();
		}
		return expressionobjectmethods;
	}
}
