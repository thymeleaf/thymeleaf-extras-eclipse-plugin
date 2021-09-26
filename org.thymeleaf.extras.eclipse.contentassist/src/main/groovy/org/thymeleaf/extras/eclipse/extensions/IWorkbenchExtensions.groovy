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

package org.thymeleaf.extras.eclipse.extensions

import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.ui.IWorkbench

/**
 * Extensions to Eclipse's {@link IWorkbench} interface.
 * 
 * @author Emanuel Rabina
 */
class IWorkbenchExtensions {

	/**
	 * Find the Java project for the file the user is working on.
	 * 
	 * @param self
	 * @return
	 */
	static IJavaProject getCurrentJavaProject(IWorkbench self) {

		// TODO: Holy cow this is one helluva chain.  Is there an easier way to get
		//       the project of the current file in view?
		def project = self.workbenchWindows[0].activePage.activeEditor.editorInput.file.project
		return project.isNatureEnabled(JavaCore.NATURE_ID) ? JavaCore.create(project) : null
	}
}
