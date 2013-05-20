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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of all of the projects which contain dialect files found in
 * the user's workspace.  Used to better track changes made to dialect files
 * within the workspace so that the dialect cache used in content assist is kept
 * up to date.
 * 
 * @author Emanuel Rabina
 */
public class DialectTree {

	private HashMap<Dialect,DialectFile> bundleddialects = new HashMap<Dialect,DialectFile>();

	private HashMap<IJavaProject,DialectProject> dialectprojects = new HashMap<IJavaProject,DialectProject>();
	// Saved project dialect item lists
	private HashMap<IJavaProject,ArrayList<AttributeProcessor>> projectattributeprocessors =
			new HashMap<IJavaProject,ArrayList<AttributeProcessor>>();
	private HashMap<IJavaProject,ArrayList<ElementProcessor>> projectelementprocessors =
			new HashMap<IJavaProject,ArrayList<ElementProcessor>>();

	private HashMap<IJavaProject,ArrayList<ExpressionObjectMethod>> projectexpressionobjectmethods =
			new HashMap<IJavaProject,ArrayList<ExpressionObjectMethod>>();

	/**
	 * Package-only constructor.
	 */
	DialectTree() {
	}

	/**
	 * Add a bundled dialect to the tree, which will be available across all
	 * dialect item requests.
	 * 
	 * @param dialect
	 * @param dialectitems A list of the items in the dialect, but already
	 * 					   processed to include all the information they need
	 * 					   for content assist queries.
	 */
	void addBundledDialect(Dialect dialect, List<DialectItem> dialectitems) {

		bundleddialects.put(dialect, new DialectFile(dialectitems));
	}

	/**
	 * Add a dialect and associated project to the tree.  If the associated
	 * project exists, the dialect will be added to the existing project
	 * instead.
	 * 
	 * @param project
	 * @param dialectpath  The resource path to the dialect.
	 * @param dialectitems A list of the items in the dialect, but already
	 * 					   processed to include all the information they need
	 * 					   for content assist queries.
	 */
	void addProjectDialect(IJavaProject project, IPath dialectpath, List<DialectItem> dialectitems) {

		if (!containsProject(project)) {
			dialectprojects.put(project, new DialectProject());
		}
		dialectprojects.get(project).addDialect(dialectpath, dialectitems);
	}

	/**
	 * Return whether or not this tree knows about the given Java project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has a matching entry in this tree.
	 */
	boolean containsProject(IJavaProject project) {

		return dialectprojects.containsKey(project);
	}

	/**
	 * Retrieve all attribute processors for the given project.
	 * 
	 * @param project
	 * @return List of all attribute processors for the given project.
	 */
	List<AttributeProcessor> getAttributeProcessorsForProject(IJavaProject project) {

		if (!projectattributeprocessors.containsKey(project)) {
			ArrayList<AttributeProcessor> attributeprocessors = new ArrayList<AttributeProcessor>(
					dialectprojects.get(project).getAttributeProcessors());
			for (DialectFile bundleddialect: bundleddialects.values()) {
				attributeprocessors.addAll(bundleddialect.getAttributeProcessors());
			}
			Collections.sort(attributeprocessors, new DialectItemComparator());
			attributeprocessors.trimToSize();
			projectattributeprocessors.put(project, attributeprocessors);
		}
		return projectattributeprocessors.get(project);
	}

	/**
	 * Retrieve all element processors for the given project.
	 * 
	 * @param project
	 * @return List of all element processors for the given project.
	 */
	List<ElementProcessor> getElementProcessorsForProject(IJavaProject project) {

		if (!projectelementprocessors.containsKey(project)) {
			ArrayList<ElementProcessor> elementprocessors = new ArrayList<ElementProcessor>(
					dialectprojects.get(project).getElementProcessors());
			for (DialectFile bundleddialect: bundleddialects.values()) {
				elementprocessors.addAll(bundleddialect.getElementProcessors());
			}
			Collections.sort(elementprocessors, new DialectItemComparator());
			elementprocessors.trimToSize();
			projectelementprocessors.put(project, elementprocessors);
		}
		return projectelementprocessors.get(project);
	}

	/**
	 * Retrieve all expression object methods for the given project.
	 * 
	 * @param project
	 * @return List of all expression object methods for the given project.
	 */
	List<ExpressionObjectMethod> getExpressionObjectMethodsForProject(IJavaProject project) {

		if (!projectexpressionobjectmethods.containsKey(project)) {
			ArrayList<ExpressionObjectMethod> expressionobjectmethods = new ArrayList<ExpressionObjectMethod>(
					dialectprojects.get(project).getExpressionObjectMethods());
			for (DialectFile bundleddialect: bundleddialects.values()) {
				expressionobjectmethods.addAll(bundleddialect.getExpressionObjectMethods());
			}
			Collections.sort(expressionobjectmethods, new DialectItemComparator());
			expressionobjectmethods.trimToSize();
			projectexpressionobjectmethods.put(project, expressionobjectmethods);
		}
		return projectexpressionobjectmethods.get(project);
	}

	/**
	 * Update the dialect file that was mapped to the given path, with the new
	 * processed dialect items.
	 * 
	 * @param dialectfilepath
	 * @param dialectitems
	 */
	void updateDialect(IPath dialectfilepath, List<DialectItem> dialectitems) {

		for (Map.Entry<IJavaProject,DialectProject> entryset: dialectprojects.entrySet()) {
			IJavaProject javaproject = entryset.getKey();
			DialectProject dialectproject = entryset.getValue();

			if (dialectproject.hasDialect(dialectfilepath)) {
				if (dialectitems != null) {
					dialectproject.addDialect(dialectfilepath, dialectitems);
				}
				else {
					dialectproject.removeDialect(dialectfilepath);
				}
				projectattributeprocessors.remove(javaproject);
				projectelementprocessors.remove(javaproject);
				projectexpressionobjectmethods.remove(javaproject);
			}
		}
	}


	/**
	 * Comparator for dialect items.  Dialect items are sorted in alphabetical
	 * order, prefix first, then the processor name.
	 */
	private class DialectItemComparator implements Comparator<DialectItem> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(DialectItem item1, DialectItem item2) {

			Dialect dialect1 = item1.getDialect();
			Dialect dialect2 = item2.getDialect();

			return !dialect1.equals(dialect2) ?
					dialect1.getPrefix().compareTo(dialect2.getPrefix()) :
					item1.getName().compareTo(item2.getName());
		}
	}
}
