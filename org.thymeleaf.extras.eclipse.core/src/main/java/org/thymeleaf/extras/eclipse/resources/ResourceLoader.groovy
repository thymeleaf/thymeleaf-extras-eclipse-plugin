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

package org.thymeleaf.extras.eclipse.resources

/**
 * Loads resource information from the resources located by a
 * {@link ResourceLocator}.
 * 
 * @param <T> The type of resource being loaded.
 * @param <L> The locator being used.
 * @author Emanuel Rabina
 */
interface ResourceLoader<T, L extends ResourceLocator> {

	/**
	 * Loads all of the resources from the results of a given locator.
	 * 
	 * @param locator
	 * @return List of resource models, built from the underlying resources
	 *         picked out by the locator.
	 */
	List<T> load(L locator)
}
