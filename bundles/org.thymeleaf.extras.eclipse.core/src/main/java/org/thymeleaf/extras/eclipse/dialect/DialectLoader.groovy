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

import org.thymeleaf.extras.eclipse.dialect.xml.Dialect

/**
 * Loads dialect information of the given type, as retrieved from a
 * {@link DialectLocator}.
 * 
 * @param <T> Type of the dialect information to accept.
 * @author Emanuel Rabina
 */
interface DialectLoader<T> {

	/**
	 * Load all the dialects from the given locator, converting from the XML
	 * files to the Java representation which other Thymeleaf plugins can work
	 * with.
	 * 
	 * @param locator
	 * @return List of dialects, one for every dialect file returned by the
	 * 		   dialect locator.
	 */
	List<Dialect> loadDialects(DialectLocator<T> locator)
}
