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

package org.thymeleaf.extras.eclipse.contentassist

import org.w3c.dom.Attr
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.namespace.QName

/**
 * Common code between the various content assist computers that pick out
 * Thymeleaf processors or expression object methods from the document.
 * 
 * @author Emanuel Rabina
 */
abstract class AbstractComputer {

	/**
	 * Return a list of the namespaces valid at the given node.
	 * 
	 * @param node
	 * @return List of namespaces known to this node.
	 */
	protected static ArrayList<QName> findNodeNamespaces(Node node) {

		def namespaces = []

		if (node instanceof Element) {
			def attributes = node.attributes
			for (def i = 0; i < attributes.length; i++) {
				def name = ((Attr)attributes.item(i)).name
				if (name.startsWith("xmlns:")) {
					namespaces.add(new QName(((Element)node).getAttribute(name), '', name.substring(6)))
				}
			}
		}
		def parent = node.parentNode
		if (parent != null) {
			namespaces.addAll(findNodeNamespaces(parent))
		}

		return namespaces
	}

	/**
	 * Returns whether or not the given character is a valid expression object
	 * method name character.
	 * 
	 * @param c
	 * @return <tt>true</tt> if <tt>char</tt> is an alphanumeric character, or
	 * 		   one of the following symbols: <tt># .</tt>
	 */
	protected static boolean isExpressionObjectMethodChar(char c) {

		return Character.isLetterOrDigit(c) || c == '#' || c =='.'
	}
}
