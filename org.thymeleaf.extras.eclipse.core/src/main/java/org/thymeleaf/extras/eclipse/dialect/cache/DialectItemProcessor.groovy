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

package org.thymeleaf.extras.eclipse.dialect.cache

import javax.inject.Named
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.ui.JavadocContentAccess
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import org.thymeleaf.extras.eclipse.dialect.xml.Documentation
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObject
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod
import org.thymeleaf.extras.eclipse.dialect.xml.Processor

/**
 * Creates a content-assist ready dialect item from a dialect file definition.
 * 
 * @author Emanuel Rabina
 */
@Named
class DialectItemProcessor {

	/**
	 * Creates a documentation element from the Javadocs of a processor class.
	 * 
	 * @param processor
	 * @param project
	 * @return Documentation element with the processor's Javadoc content, or
	 *   <tt>null</tt> if the processor had no Javadocs on it.
	 */
	private static Documentation generateDocumentation(Processor processor, IJavaProject project) {

		def type = project.findType(processor.clazz, new NullProgressMonitor())
		if (type) {
			def htmlContentReader = JavadocContentAccess.getHTMLContentReader(type, false, false)
			if (htmlContentReader) {
				def htmlContent = htmlContentReader.withReader { reader ->
					def javaDoc = ""
					int nextChar = reader.read()
					while (nextChar != -1) {
						javaDoc += nextChar
						nextChar = reader.read()
					}
					return javaDoc
				}
				return new Documentation(
					value: htmlContent
				)
			}
		}
		return null
	}

	/**
	 * Creates expression object method suggestions from an expression object
	 * reference.
	 * 
	 * @param dialect
	 *   Parent dialect.
	 * @param expressionObject
	 *   The exression object reference.
	 * @param project
	 * @return Set of expression object method suggestions based on the visible
	 *   methods of the expression object.
	 */
	private static HashSet<ExpressionObjectMethod> generateExpressionObjectMethods(Dialect dialect,
		ExpressionObject expressionObject, IJavaProject project) {

		def generatedMethods = new HashSet<ExpressionObjectMethod>()

		def type = project.findType(expressionObject.clazz)
		if (type) {
			for (def method: type.methods) {
				if (!method.constructor) {
					def expressionObjectMethod = new ExpressionObjectMethod(
						dialect: dialect
					)

					// For Java bean methods, convert the suggestion to a property
					def methodName = method.elementName
					def propertyPoint =
							methodName.startsWith('get') || methodName.startsWith('set') ? 3 :
							methodName.startsWith('is') ? 2 :
							-1

					if (propertyPoint != -1 && methodName.length() > propertyPoint &&
						Character.isUpperCase(methodName.charAt(propertyPoint))) {

						StringBuilder propertyName = new StringBuilder(methodName.substring(propertyPoint))
						propertyName.insert(0, Character.toLowerCase(propertyName.charAt(0)))
						propertyName.deleteCharAt(1)
						expressionObjectMethod.name = "${expressionObject.name}.${propertyName}"
						expressionObjectMethod.javaBeanProperty = true
					}
					else {
						expressionObjectMethod.name = "${expressionObject.name}.${methodName}"
					}

					generatedMethods << expressionObjectMethod
				}
			}
		}
		return generatedMethods
	}

	/**
	 * Generate the content assist documentation to accompany each dialect item.
	 * 
	 * @param dialect
	 * @param project
	 * @return List of dialect items, already processed to include all the
	 *   necessary documentation to be a part of the content assist system.
	 */
	List<DialectItem> processDialectItems(Dialect dialect, IJavaProject project) {

		return dialect.dialectItems.inject([]) { acc, dialectItem ->
			if (dialectItem instanceof Processor) {
				// Generate and save javadocs if no documentation present
				if (!dialectItem.setDocumentation && dialectItem.setClazz) {
					dialectItem.documentation = generateDocumentation(dialectItem, project)
				}
				acc << dialectItem
			}
			else if (dialectItem instanceof ExpressionObject) {
				acc += generateExpressionObjectMethods(dialect, dialectItem, project)
			}
			else {
				acc << dialectItem
			}
			return acc
		}
	}
}
