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

package org.thymeleaf.extras.eclipse.resources

import org.eclipse.core.runtime.IPath

/**
 * Representation of a project that contains one or more files which contain
 * specific resource information.
 * 
 * @param <T> The resource being mapped against a project.
 * @author Emanuel Rabina
 */
class ResourceProject<T> {

	private final HashMap<IPath,T> files = new HashMap<IPath,T>()

	/**
	 * Adds a resource to this project.  If the path already exists for the
	 * resource, then this method will ovewrite that resource.
	 * 
	 * @param path     The resource path.
	 * @param resource The resource at the given path.
	 */
	void addResource(IPath path, T resource) {

		files.put(path, resource)
	}

	/**
	 * Return all resources associated with this project.
	 * 
	 * @return All resources in this project.
	 */
	Collection<T> getResources() {

		return files.values()
	}

	/**
	 * Return whether or not this project has a resource at the given path.
	 * 
	 * @param path
	 * @return <tt>true</tt> if a resource in this project originates from the
	 * 		   given path.
	 */
	boolean hasResource(IPath path) {

		return files.containsKey(path)
	}

	/**
	 * Removes a resource from this project.
	 * 
	 * @param path The path to the resource.
	 */
	void removeResource(IPath path) {

		files.remove(path)
	}
}
