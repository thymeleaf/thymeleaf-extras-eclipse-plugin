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

package org.thymeleaf.extras.eclipse.dialect

/**
 * Locates Thymeleaf dialect information to be loaded at some later time.
 * 
 * @param <T> Type of the dialect information.
 * @author Emanuel Rabina
 */
interface DialectLocator<T> {

	/**
	 * Looks for dialects and returns them as some type of list to be consumed
	 * by a {@link DialectLoader} that can accept that type.
	 * 
	 * @return List of input streams over the dialect help XML files.
	 */
	List<T> locateDialects()
}
