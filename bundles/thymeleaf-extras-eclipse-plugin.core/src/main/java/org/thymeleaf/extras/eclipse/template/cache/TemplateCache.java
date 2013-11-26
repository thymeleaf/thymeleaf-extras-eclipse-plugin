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

package org.thymeleaf.extras.eclipse.template.cache;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.thymeleaf.extras.eclipse.scanner.cache.ResourceTree;
import org.thymeleaf.extras.eclipse.template.ProjectTemplateLocator;
import org.thymeleaf.extras.eclipse.template.TemplateLoader;
import org.thymeleaf.extras.eclipse.template.model.Fragment;
import org.thymeleaf.extras.eclipse.template.model.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic in-memory store of all known template fragments per project.
 * 
 * @author Emanuel Rabina
 */
public class TemplateCache {

	private static TemplateLoader templateloader = new TemplateLoader();

	// Tree structure of all fragments in the user's workspace
	private static ResourceTree<Template> fragmenttree;

	/**
	 * Return all of the fragments in the given project.
	 * 
	 * @param project The current project.
	 * @return List of fragments in the project.
	 */
	public static List<Fragment> getFragments(IJavaProject project) {

		loadTemplatesFromProject(project);

		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		for (Template template: fragmenttree.getResourcesForProject(project)) {
			fragments.addAll(template.getFragments());
		}
		return fragments;
	}

	/**
	 * Gather all the template information from the given project, if we haven't
	 * got information on that project in the first place.
	 * 
	 * @param project
	 */
	private static void loadTemplatesFromProject(IJavaProject project) {

		if (!fragmenttree.containsProject(project)) {
			ProjectTemplateLocator projecttemplatelocator = new ProjectTemplateLocator(project);
			List<Template> templates = templateloader.loadResources(projecttemplatelocator);
			List<IPath> templatefilepaths = projecttemplatelocator.getTemplateFilePaths();

			if (templates.size() > 0) {
				for (int i = 0; i < templates.size(); i++) {
					Template template = templates.get(i);
					IPath templatefilepath = templatefilepaths.get(i);

					fragmenttree.addResourceToProject(project, templatefilepath, template);
				}
			}
			else {
				fragmenttree.addResourcesToProject(project, null, new ArrayList<Template>());
			}
		}
	}

	/**
	 * Clear the cache and perform any other cleanup.
	 */
	public static void shutdown() {
	}

	/**
	 * Initialize the cache.
	 */
	public static void startup() {

		fragmenttree = new ResourceTree<Template>();
	}
}
