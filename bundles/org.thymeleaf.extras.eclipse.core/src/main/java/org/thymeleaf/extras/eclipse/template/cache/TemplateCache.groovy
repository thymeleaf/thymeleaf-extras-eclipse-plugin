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

package org.thymeleaf.extras.eclipse.template.cache

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject
import org.thymeleaf.extras.eclipse.scanner.cache.ResourceTree
import org.thymeleaf.extras.eclipse.template.ProjectTemplateLocator
import org.thymeleaf.extras.eclipse.template.TemplateLoader
import org.thymeleaf.extras.eclipse.template.model.Fragment
import org.thymeleaf.extras.eclipse.template.model.Template

/**
 * A basic in-memory store of all known template fragments per project.
 * 
 * @author Emanuel Rabina
 */
class TemplateCache {

	private static TemplateLoader templateLoader = new TemplateLoader()

	// Tree structure of all fragments in the user's workspace
	private static ResourceTree<Template> fragmentTree = new ResourceTree<Template>()

	/**
	 * Return all of the fragments in the given project.
	 * 
	 * @param project The current project.
	 * @return List of fragments in the project.
	 */
	static List<Fragment> getFragments(IJavaProject project) {

		// Build and cache a fragment library for the given project
		if (!fragmentTree.containsProject(project)) {
			def projectTemplateLocator = new ProjectTemplateLocator(project)
			def templates = templateLoader.loadResources(projectTemplateLocator)
			if (templates.size() > 0) {
				templates.each { template ->
					fragmentTree.addResourceToProject(project, template.filePath, template)
				}
			}
			else {
				fragmentTree.addResourcesToProject(project, null, [])
			}
		}

		return fragmentTree.getResourcesForProject(project).inject([]) { fragments, template ->
			return fragments.addAll(template.fragments)
		}
	}

	/**
	 * Clear the cache and perform any other cleanup.
	 */
	static void shutdown() {
	}

	/**
	 * Initialize the cache.
	 */
	static void startup() {
	}
}
