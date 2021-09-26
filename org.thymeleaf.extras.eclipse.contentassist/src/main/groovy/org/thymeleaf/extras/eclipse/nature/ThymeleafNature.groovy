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


import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectNature 
/**
 * Configures a project that has been given the Thymeleaf nature.
 * 
 * @author Emanuel Rabina
 */
class ThymeleafNature implements IProjectNature {

	static final String THYMELEAF_NATURE_ID  = 'org.thymeleaf.extras.eclipse.core.ThymeleafNature'
	static final String THYMELEAF_BUILDER_ID = 'org.thymeleaf.extras.eclipse.core.ThymeleafBuilder'

	IProject project

	@Override
	void configure() {

		// Add the Thymeleaf builder
		def description = project.description
		def newCommand = description.newCommand()
		newCommand.setBuilderName(THYMELEAF_BUILDER_ID)
		description.buildSpec = description.buildSpec + newCommand
		project.setDescription(description, null)
	}

	@Override
	void deconfigure() {

		// Remove the Thymeleaf builder
		def description = project.description
		description.buildSpec = description.buildSpec - THYMELEAF_BUILDER_ID
		project.setDescription(description, null)
	}
}
