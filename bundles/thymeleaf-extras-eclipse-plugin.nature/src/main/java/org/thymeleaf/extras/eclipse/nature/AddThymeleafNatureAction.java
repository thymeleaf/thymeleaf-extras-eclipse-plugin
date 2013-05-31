/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Adds a Thymeleaf nature to the project.
 * 
 * @author Emanuel Rabina
 */
public class AddThymeleafNatureAction implements IObjectActionDelegate {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IAction action) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
