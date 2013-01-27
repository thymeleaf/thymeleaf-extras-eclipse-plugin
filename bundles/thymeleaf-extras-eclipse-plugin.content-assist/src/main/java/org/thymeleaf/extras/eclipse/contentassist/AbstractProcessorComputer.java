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

package org.thymeleaf.extras.eclipse.contentassist;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * Common code between the various processors in this plugin that pick out
 * Thymeleaf processors from the document.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractProcessorComputer {

	private static final Pattern PROCESSOR_NAME_PATTERN = Pattern.compile("[\\w:-]*");
	private static final Pattern UTILITY_METHOD_PATTERN = Pattern.compile("#\\w*(\\.\\w*)?");

	/**
	 * Return a list of the namespaces valid at the given node.
	 * 
	 * @param node
	 * @return List of namespaces known to this node.
	 */
	protected static ArrayList<QName> findNodeNamespaces(Node node) {

		ArrayList<QName> namespaces = new ArrayList<QName>();

		if (node instanceof Element) {
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = ((Attr)attributes.item(i)).getName();
				if (name.startsWith("xmlns:")) {
					namespaces.add(new QName(((Element)node).getAttribute(name), "", name.substring(6)));
				}
			}
		}
		Node parent = node.getParentNode();
		if (parent != null) {
			namespaces.addAll(findNodeNamespaces(parent));
		}

		return namespaces;
	}

	/**
	 * Returns whether or not the given pattern is a utility method string.
	 * 
	 * @param pattern The autocomplete pattern to check against.
	 * @return <tt>true</tt> if the pattern matches a utility method.
	 */
	protected static boolean isUtilityMethodPattern(String pattern) {

		return UTILITY_METHOD_PATTERN.matcher(pattern).matches();
	}

	/**
	 * Returns whether or not the given pattern is a processor name string.
	 * 
	 * @param pattern The autocomplete pattern to check against.
	 * @return <tt>true</tt> if the pattern matches a processor name.
	 */
	protected static boolean isProcessorNamePattern(String pattern) {

		return PROCESSOR_NAME_PATTERN.matcher(pattern).matches();
	}

	/**
	 * Returns whether or not the given character is a valid processor name or
	 * utility method character.
	 * 
	 * @param c
	 * @return <tt>true</tt> if <tt>char</tt> is an alphanumeric character, or
	 * 		   one of the following symbols: <tt>: - # .</tt>
	 */
	protected static boolean isProcessorOrUtilityMethodChar(char c) {

		return Character.isLetterOrDigit(c) || c == ':' || c == '-' || c == '#' || c =='.';
	}
}
