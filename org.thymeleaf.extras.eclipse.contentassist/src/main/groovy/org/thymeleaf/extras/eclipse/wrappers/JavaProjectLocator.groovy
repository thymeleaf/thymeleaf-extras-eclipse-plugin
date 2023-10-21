/* 
 * Copyright 2021, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.wrappers

import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.ui.IWorkbench

import jakarta.inject.Inject
import jakarta.inject.Named

/**
 * A basic wrapper around Eclipse's static methods to get the Java project for
 * the current file.  This is so we can then mock this in unit tests and not
 * have to rely on Eclipse internals to be up and running.
 * 
 * @author Emanuel Rabina
 */
@Named
class JavaProjectLocator {

	@Inject
	private final IWorkbench workbench

	/**
	 * Locate an Eclipse {@link IJavaProject} for the currently-open file.
	 * 
	 * @return
	 */
	IJavaProject locate() {

		def project = workbench.workbenchWindows[0].activePage.activeEditor.editorInput.file.project
		return project.isNatureEnabled(JavaCore.NATURE_ID) ? JavaCore.create(project) : null
	}
}
