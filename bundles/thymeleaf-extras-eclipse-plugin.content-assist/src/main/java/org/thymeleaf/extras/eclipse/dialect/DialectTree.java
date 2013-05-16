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

package org.thymeleaf.extras.eclipse.dialect;

import org.eclipse.jdt.core.IJavaProject;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;

import java.util.HashMap;
import java.util.List;

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

	// Resource listener for changes to dialect projects and files
	private DialectChangeListener dialectchangelistener = new DialectChangeListener();

	/**
	 * Add a bundled dialect to the tree, which will be available across all
	 * dialect item requests.
	 * 
	 * @param dialect
	 * @param dialectitems A list of the items in the dialect, but already
	 * 					   processed to include all the information they need
	 * 					   for content assist queries.
	 */
	public void addBundledDialect(Dialect dialect, List<DialectItem> dialectitems) {

		bundleddialects.put(dialect, new DialectFile(dialectitems));
	}

	/**
	 * Add a dialect and associated project to the tree.  If the associated
	 * project exists, the dialect will be added to the existing project
	 * instead.
	 * 
	 * @param project
	 * @param dialect
	 * @param dialectitems A list of the items in the dialect, but already
	 * 					   processed to include all the information they need
	 * 					   for content assist queries.
	 */
	public void addProjectDialect(IJavaProject project, Dialect dialect, List<DialectItem> dialectitems) {

		if (!containsProject(project)) {
			dialectprojects.put(project, new DialectProject());
		}
		dialectprojects.get(project).dialectfiles.put(dialect, new DialectFile(dialectitems));
	}

	/**
	 * Clean up the workspace change listener.
	 */
	public void close() {

		dialectchangelistener.close();
	}

	/**
	 * Return whether or not this tree knows about the given Java project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has a matching entry in this tree.
	 */
	public boolean containsProject(IJavaProject project) {

		return dialectprojects.containsKey(project);
	}

	/**
	 * Retrieve all attribute processors for the given project.
	 * 
	 * @param project
	 * @return List of all attribute processors for the given project.
	 */
	public List<AttributeProcessor> getAttributeProcessorsForProject(IJavaProject project) {

		return dialectprojects.get(project).getAttributeProcessors();
	}

	/**
	 * Retrieve all element processors for the given project.
	 * 
	 * @param project
	 * @return List of all element processors for the given project.
	 */
	public List<ElementProcessor> getElementProcessorsForProject(IJavaProject project) {

		return dialectprojects.get(project).getElementProcessors();
	}
}
