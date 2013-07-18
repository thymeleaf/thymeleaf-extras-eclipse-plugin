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

package org.thymeleaf.extras.eclipse.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Configures a project that has been given the Thymeleaf nature.
 * 
 * @author Emanuel Rabina
 */
public class ThymeleafNature implements IProjectNature {

	static final String THYMELEAF_NATURE_ID  = "org.thymeleaf.extras.eclipse.core.ThymeleafNature";
	static final String THYMELEAF_BUILDER_ID = "org.thymeleaf.extras.eclipse.core.ThymeleafBuilder";

	private IProject project;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure() throws CoreException {

		// Add the Thymeleaf builder
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand[] newcommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newcommands, 0, commands.length);
		ICommand command = description.newCommand();
		command.setBuilderName(THYMELEAF_BUILDER_ID);
		newcommands[commands.length] = command;
		description.setBuildSpec(newcommands);
		project.setDescription(description, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deconfigure() throws CoreException {

		// Remove the Thymeleaf builder
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand[] newcommands = new ICommand[commands.length - 1];
		int thymeleafbuilderindex;
		for (thymeleafbuilderindex = 0; thymeleafbuilderindex < commands.length; thymeleafbuilderindex++) {
			if (commands[thymeleafbuilderindex].getBuilderName().equals(THYMELEAF_BUILDER_ID)) {
				break;
			}
		}
		System.arraycopy(commands, 0, newcommands, 0, thymeleafbuilderindex);
		System.arraycopy(commands, thymeleafbuilderindex + 1, newcommands, thymeleafbuilderindex, newcommands.length - thymeleafbuilderindex);
		description.setBuildSpec(newcommands);
		project.setDescription(description, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProject getProject() {

		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProject(IProject project) {

		this.project = project;
	}

	/**
	 * Check if the Thymeleaf nature has been applied to the given project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has the Thymeleaf nature.
	 */
	public static boolean thymeleafNatureEnabled(IProject project) {

		try {
			return project.hasNature(THYMELEAF_NATURE_ID);
		}
		catch (CoreException ex) {
			// Do nothing
		}
		return false;
	}
}
