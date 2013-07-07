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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * Adds a Thymeleaf nature to selected projects.
 * 
 * @author Emanuel Rabina
 */
public class AddThymeleafNatureAction extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();
		List<IJavaProject> selectedprojects = (List<IJavaProject>)context.getDefaultVariable();

		// Add the Thymeleaf nature to all selected projects
		for (IJavaProject javaproject: selectedprojects) {
			IProject project = javaproject.getProject();
			try {
				IProjectDescription description = project.getDescription();
				String[] natures = description.getNatureIds();
				String[] newnatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newnatures, 0, natures.length);
				newnatures[natures.length] = ThymeleafNature.THYMELEAF_NATURE_ID;
				description.setNatureIds(newnatures);
				project.setDescription(description, null);
			}
			catch (CoreException ex) {
				// Do nothing
			}
		}

		return null;
	}
}
