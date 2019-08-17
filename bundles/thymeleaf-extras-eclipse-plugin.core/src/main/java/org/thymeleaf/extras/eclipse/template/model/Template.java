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

package org.thymeleaf.extras.eclipse.template.model;

import java.util.ArrayList;
import java.util.List;

import org.attoparser.dom.Document;
import org.attoparser.dom.Element;

/**
 * Model of a Thymeleaf template.
 * 
 * @author Emanuel Rabina
 */
public class Template {

	private static final String FRAGMENT_ATTRIBUTE      = "th:fragment";
	private static final String DATA_FRAGMENT_ATTRIBUTE = "data-th-fragment";

	private final ArrayList<Fragment> fragments = new ArrayList<Fragment>();

	/**
	 * Create a new template from an HTML document.
	 * 
	 * @param document
	 */
	public Template(Document document) {

		// Look for fragment signatures inside the HTML document
		findFragments(document.getFirstChildOfType(Element.class));
	}

	/**
	 * Recursive search for any fragment signatures inside an HTML document.
	 * 
	 * @param element
	 */
	private void findFragments(Element element) {

		if (element.hasAttribute(FRAGMENT_ATTRIBUTE)) {
			fragments.add(new Fragment(element.getAttributeValue(FRAGMENT_ATTRIBUTE)));
		}
		else if (element.hasAttribute(DATA_FRAGMENT_ATTRIBUTE)) {
			fragments.add(new Fragment(element.getAttributeValue(DATA_FRAGMENT_ATTRIBUTE)));
		}

		for (Element childelement: element.getChildrenOfType(Element.class)) {
			findFragments(childelement);
		}
	}

	/**
	 * Return a list of fragments in this template.
	 * 
	 * @return Fragment list.
	 */
	public List<Fragment> getFragments() {

		return fragments;
	}
}
