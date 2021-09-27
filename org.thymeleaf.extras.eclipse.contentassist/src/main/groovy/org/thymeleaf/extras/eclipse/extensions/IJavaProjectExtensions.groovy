/* 
 * Copyright 2021, The Thymeleaf project (http://www.thymeleaf.org/)
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
import org.thymeleaf.extras.eclipse.nature.ThymeleafNature

/**
 * Extensions for Eclipse's {@link IJavaProject} class.
 * 
 * @author Emanuel Rabina
 */
class IJavaProjectExtensions {

	/**
	 * Check if the Thymeleaf nature has been applied to the given project.
	 * 
	 * @param self
	 * @return
	 */
	static boolean hasThymeleafNature(IJavaProject self) {

		return self.project.hasNature(ThymeleafNature.THYMELEAF_NATURE_ID)
	}
}
