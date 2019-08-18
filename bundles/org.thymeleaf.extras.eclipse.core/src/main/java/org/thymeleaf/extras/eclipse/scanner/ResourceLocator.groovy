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

package org.thymeleaf.extras.eclipse.scanner

/**
 * Locates certain kinds of resources to be loaded at a later time.
 * 
 * @param <F> The type/format being returned by the locator.
 * @author Emanuel Rabina
 */
interface ResourceLocator<F> {

	/**
	 * Looks for resources and returns a list over those resources that can be
	 * accepted by a {@link ResourceLoader}.
	 * 
	 * @return List of formats over a resource type.
	 */
	List<F> locateResources()
}
