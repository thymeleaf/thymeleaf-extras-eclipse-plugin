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

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod

import javax.inject.Named

/**
 * Representation of all of the projects which contain dialect files found in
 * the user's workspace.  Used to better track changes made to dialect files
 * within the workspace so that the dialect cache used in content assist is kept
 * up to date.
 * 
 * @author Emanuel Rabina
 */
@Named
class DialectTree {

	private HashMap<IJavaProject,DialectProject> dialectProjects = [:]

	// Saved project dialect item lists
	private HashMap<IJavaProject,ArrayList<AttributeProcessor>> projectAttributeProcessors = [:]
	private HashMap<IJavaProject,ArrayList<ElementProcessor>> projectElementProcessors = [:]
	private HashMap<IJavaProject,ArrayList<ExpressionObjectMethod>> projectExpressionobjectMethods = [:]

	/**
	 * Comparator for dialect items.  Dialect items are sorted in alphabetical
	 * order, prefix first, then the processor name.
	 */
	private static final Closure dialectItemSorter = { DialectItem item1, DialectItem item2 ->
		def dialect1 = item1.dialect
		def dialect2 = item2.dialect
		return dialect1 != dialect2 ? dialect1.prefix <=> dialect2.prefix : item1.name <=> item2.name
	}

	/**
	 * Add a dialect and associated project to the tree.  If the associated
	 * project exists, the dialect will be added to the existing project
	 * instead.
	 * 
	 * @param project
	 * @param dialectPath
	 *   The resource path to the dialect.
	 * @param dialectItems
	 *   A list of the items in the dialect, but already processed to include all
	 *   the information they need for content assist queries.
	 */
	void addProjectDialect(IJavaProject project, IPath dialectPath, List<DialectItem> dialectItems) {

		if (!dialectProjects[project]) {
			dialectProjects[project] = new DialectProject()
		}
		dialectProjects[project].addDialect(dialectPath, dialectItems)
	}

	/**
	 * Return whether or not this tree knows about the given Java project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has a matching entry in this tree.
	 */
	boolean containsProject(IJavaProject project) {

		return dialectProjects[project]
	}

	/**
	 * Retrieve all attribute processors for the given project.
	 * 
	 * @param project
	 * @return List of all attribute processors for the given project.
	 */
	List<AttributeProcessor> getAttributeProcessorsForProject(IJavaProject project) {

		return projectAttributeProcessors.getOrCreate(project) { ->
			return dialectProjects[project].attributeProcessors.sort(false, dialectItemSorter)
		}
	}

	/**
	 * Retrieve all element processors for the given project.
	 * 
	 * @param project
	 * @return List of all element processors for the given project.
	 */
	List<ElementProcessor> getElementProcessorsForProject(IJavaProject project) {

		return projectAttributeProcessors.getOrCreate(project) { ->
			return dialectProjects[project].elementProcessors.sort(false, dialectItemSorter)
		}
	}

	/**
	 * Retrieve all expression object methods for the given project.
	 * 
	 * @param project
	 * @return List of all expression object methods for the given project.
	 */
	List<ExpressionObjectMethod> getExpressionObjectMethodsForProject(IJavaProject project) {

		return projectExpressionobjectMethods.getOrCreate(project) { ->
			return dialectProjects[project].expressionObjectMethods.sort(false, dialectItemSorter)
		}
	}

	/**
	 * Update the dialect file that was mapped to the given path, with the new
	 * processed dialect items.
	 * 
	 * @param dialectFilepath
	 * @param dialectItems
	 */
	void updateDialect(IPath dialectFilePath, List<DialectItem> dialectItems) {

		dialectProjects.each { javaProject, dialectProject ->
			if (dialectProject.hasDialect(dialectFilePath)) {
				if (dialectItems) {
					dialectProject.addDialect(dialectFilePath, dialectItems)
				}
				else {
					dialectProject.removeDialect(dialectFilePath)
				}
				projectAttributeProcessors.remove(javaProject)
				projectElementProcessors.remove(javaProject)
				projectExpressionobjectMethods.remove(javaProject)
			}
		}
	}
}
