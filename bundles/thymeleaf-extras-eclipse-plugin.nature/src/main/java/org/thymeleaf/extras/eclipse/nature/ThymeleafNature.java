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

	private static final String THYMELEAF_NATURE_ID = "org.thymeleaf.extras.eclipse.nature.ThymeleafNature";

	private IProject project;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure() throws CoreException {

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newnatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newnatures, 0, natures.length);
		newnatures[natures.length] = THYMELEAF_NATURE_ID;
		description.setNatureIds(newnatures);
		project.setDescription(description, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deconfigure() throws CoreException {

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newnatures = new String[natures.length - 1];
		int thymeleafnatureindex;
		for (thymeleafnatureindex = 0; thymeleafnatureindex < natures.length; thymeleafnatureindex++) {
			if (natures[thymeleafnatureindex].equals(THYMELEAF_NATURE_ID)) {
				break;
			}
		}
		System.arraycopy(natures, 0, newnatures, 0, thymeleafnatureindex);
		System.arraycopy(natures, thymeleafnatureindex + 1, newnatures, thymeleafnatureindex, newnatures.length - thymeleafnatureindex);
		description.setNatureIds(newnatures);
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
}
