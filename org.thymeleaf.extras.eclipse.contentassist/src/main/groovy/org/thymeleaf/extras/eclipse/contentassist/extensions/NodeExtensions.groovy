/* 
 * Copyright 2021, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.contentassist.extensions

import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.namespace.QName

/**
 * Extensions to the Node interface.
 * 
 * @author Emanuel Rabina
 */
class NodeExtensions {

	/**
	 * Return a list of the namespaces which are known at the given node.
	 * 
	 * @param self
	 * @return List of namespaces known to this node.
	 */
	static List<QName> getKnownNamespaces(Node self) {

		def namespaces = []

		if (self instanceof Element) {
			def attributes = self.attributes
			for (def i = 0; i < attributes.length; i++) {
				def attributeName = attributes.item(i).name
				if (attributeName.startsWith("xmlns:")) {
					namespaces << new QName(self.getAttribute(attributeName), '', attributeName.substring(6))
				}
			}
		}
		def parent = self.parentNode
		if (parent) {
			namespaces.addAll(parent.knownNamespaces)
		}

		return namespaces
	}
}
