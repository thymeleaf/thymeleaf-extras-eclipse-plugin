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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.Documentation;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObject;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Creates a content-assist ready dialect item from a dialect file definition.
 * 
 * @author Emanuel Rabina
 */
public class DialectItemProcessor {

	/**
	 * Package-only constructor.
	 */
	DialectItemProcessor() {
	}

	/**
	 * Creates a documentation element from the Javadocs of a processor class.
	 * 
	 * @param processor
	 * @param project
	 * @return Documentation element with the processor's Javadoc content, or
	 * 		   <tt>null</tt> if the processor had no Javadocs on it.
	 */
	private static Documentation generateDocumentation(Processor processor, IJavaProject project) {

		String processorclassname = processor.getClazz();

		try {
			IType type = project.findType(processorclassname, new NullProgressMonitor());
			if (type != null) {
				Reader reader = JavadocContentAccess.getHTMLContentReader(type, false, false);
				if (reader != null) {
					try {
						StringBuilder javadoc = new StringBuilder();
						int nextchar = reader.read();
						while (nextchar != -1) {
							javadoc.append((char)nextchar);
							nextchar = reader.read();
						}
						Documentation documentation = new Documentation();
						documentation.setValue(javadoc.toString());
						return documentation;
					}
					finally {
						reader.close();
					}
				}
			}
		}
		catch (JavaModelException ex) {
			logError("Unable to access " + processorclassname + " in the project", ex);
		}
		catch (IOException ex) {
			logError("Unable to read javadocs from " + processorclassname, ex);
		}

		return null;
	}

	/**
	 * Creates expression object method suggestions from an expression object
	 * reference.
	 * 
	 * @param dialect		   Parent dialect.
	 * @param expressionobject The exression object reference.
	 * @param project
	 * @return Set of expression object method suggestions based on the visible
	 * 		   methods of the expression object.
	 */
	private static HashSet<ExpressionObjectMethod> generateExpressionObjectMethods(Dialect dialect,
		ExpressionObject expressionobject, IJavaProject project) {

		HashSet<ExpressionObjectMethod> generatedmethods = new HashSet<ExpressionObjectMethod>();

		String classname = expressionobject.getClazz();
		try {
			IType type = project.findType(classname);
			if (type != null) {
				for (IMethod method: type.getMethods()) {
					if (!method.isConstructor()) {

						ExpressionObjectMethod expressionobjectmethod = new ExpressionObjectMethod();
						expressionobjectmethod.setDialect(dialect);

						// For Java bean methods, convert the suggestion to a property
						String methodname = method.getElementName();
						int propertypoint =
								methodname.startsWith("get") || methodname.startsWith("set") ? 3 :
								methodname.startsWith("is") ? 2 :
								-1;

						if (propertypoint != -1 && methodname.length() > propertypoint &&
							Character.isUpperCase(methodname.charAt(propertypoint))) {

							StringBuilder propertyname = new StringBuilder(methodname.substring(propertypoint));
							propertyname.insert(0, Character.toLowerCase(propertyname.charAt(0)));
							propertyname.deleteCharAt(1);
							expressionobjectmethod.setName(expressionobject.getName() + "." + propertyname);
							expressionobjectmethod.setJavaBeanProperty(true);
						}
						else {
							expressionobjectmethod.setName(expressionobject.getName() + "." + methodname);
						}

						generatedmethods.add(expressionobjectmethod);
					}
				}
			}
		}
		catch (JavaModelException ex) {
			logError("Unable to locate expression object reference: " + classname, ex);
		}

		return generatedmethods;
	}

	/**
	 * Generate the content assist documentation to accompany each dialect item.
	 * 
	 * @param dialect
	 * @param project
	 * @return List of dialect items, already processed to include all the
	 * 		   necessary documentation to be a part of the content assist
	 * 		   system.
	 */
	static List<DialectItem> processDialectItems(Dialect dialect, IJavaProject project) {

		ArrayList<DialectItem> dialectitems = new ArrayList<DialectItem>();

		for (DialectItem dialectitem: dialect.getDialectItems()) {
			if (dialectitem instanceof Processor) {
				Processor processor = (Processor)dialectitem;

				// Generate and save javadocs if no documentation present
				if (!dialectitem.isSetDocumentation() && dialectitem.isSetClazz()) {
					dialectitem.setDocumentation(generateDocumentation(processor, project));
				}
				dialectitems.add(processor);
			}
			else if (dialectitem instanceof ExpressionObject) {
				dialectitems.addAll(generateExpressionObjectMethods(dialect,
						(ExpressionObject)dialectitem, project));
			}
			else {
				dialectitems.add(dialectitem);
			}
		}

		return dialectitems;
	}
}
