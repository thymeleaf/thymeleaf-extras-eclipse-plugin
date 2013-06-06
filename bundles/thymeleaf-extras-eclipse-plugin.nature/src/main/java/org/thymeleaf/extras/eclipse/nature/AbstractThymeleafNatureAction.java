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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Common code for right-click menu actions on projects.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractThymeleafNatureAction implements IObjectActionDelegate {

	protected ArrayList<IProject> selectedprojects = new ArrayList<IProject>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void selectionChanged(IAction action, ISelection selection) {
	
		selectedprojects.clear();
	
		// Gather selected projects
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredselection = (IStructuredSelection)selection;
			for (Iterator structuredselectioniter = structuredselection.iterator();
				 structuredselectioniter.hasNext(); ) {
				Object object = structuredselectioniter.next();
				if (object instanceof IAdaptable) {
					IProject project = (IProject)((IAdaptable)object).getAdapter(IProject.class);
					if (project != null) {
						selectedprojects.add(project);
					}
					else {
						action.setEnabled(false);
						return;
					}
				}
				else {
					action.setEnabled(false);
					return;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
