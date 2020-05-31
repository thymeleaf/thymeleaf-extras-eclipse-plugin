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

package org.thymeleaf.extras.eclipse.nature

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject 
/**
 * Removes the Thymeleaf nature from selected projects.
 * 
 * @author Emanuel Rabina
 */
class RemoveThymeleafNatureAction extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	Object execute(ExecutionEvent event) {

		def selectedProjects = event.applicationContext.defaultVariable
		selectedProjects
			// Projects in the current selection that can receive the Thymeleaf nature
			.inject([]) { acc, selectedProject ->
				return selectedProject instanceof IProject ? acc << selectedProject :
					selectedProject instanceof IJavaProject ? acc << selectedProject.project :
					acc
			}
			// Remove the Thymeleaf nature from the selected projects
			.each { project ->
				def description = project.description
				description.natureIds = description.natureIds - ThymeleafNature.THYMELEAF_NATURE_ID
				project.setDescription(description, null)
			}

		return null
	}
}
