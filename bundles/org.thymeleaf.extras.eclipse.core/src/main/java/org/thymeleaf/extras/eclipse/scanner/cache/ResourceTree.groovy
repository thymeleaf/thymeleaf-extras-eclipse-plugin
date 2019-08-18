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

package org.thymeleaf.extras.eclipse.scanner.cache

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject

/**
 * Generic representation of resources in a developer's workspace, divided-up by
 * projects.
 * 
 * @param <T> The resource being mapped against a project.
 * @author Emanuel Rabina
 */
class ResourceTree<T> {

	private HashMap<IJavaProject,ResourceProject<T>> projects = [:]

	/**
	 * Add a project and an associated resource to the tree.  If the project
	 * exists, the resource will be added to the existing project instead.
	 * 
	 * @param project
	 * @param path     The path to the resource.
	 * @param resource Resources to associate with the project.
	 */
	void addResourceToProject(IJavaProject project, IPath path, T resource) {

		if (!containsProject(project)) {
			projects[project, new ResourceProject<T>()]
		}
		projects[project].addResource(path, resource)
	}

	/**
	 * Add a project and its associated resources to the tree.  If the project
	 * exists, the resources will be added to the existing project instead.
	 * 
	 * @param project
	 * @param path      The path to the resource.
	 * @param resources List of resources to associate with the project.
	 */
	void addResourcesToProject(IJavaProject project, IPath path, List<T> resources) {

		if (!containsProject(project)) {
			projects[project, new ResourceProject<T>()]
		}
		def resourceProject = projects[project]
		resources.each { resource ->
			resourceProject.addResource(path, resource)
		}
	}

	/**
	 * Return whether or not this tree knows about the given Java project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has a matching entry in this tree.
	 */
	boolean containsProject(IJavaProject project) {

		return projects.containsKey(project)
	}

	/**
	 * Retrieves all of the resources associated with the given project.
	 * 
	 * @param project
	 * @return List of all resources attached to the project.
	 */
	Collection<T> getResourcesForProject(IJavaProject project) {

		return projects[project].resources
	}
}
