/* 
 * Copyright 2019, The Thymeleaf Project (http://www.thymeleaf.org/)
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

/**
 * Extension methods for the Map class.
 * 
 * @author Scanner
 */
class MapExtensions {

	/**
	 * Retrieves a value from a map by it's key.  If there is no value for the
	 * given key, then the {@code create} closure is executed whose return value
	 * is then used as the value on the map for the key.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param self
	 * @param key
	 * @param create
	 * @return The value stored to the map by the key.
	 */
	static <K,V> V getOrCreate(Map<K,V> self, K key, Closure create) {

		if (!self[key]) {
			self[key] = create()
		}
		return self[key]
	}
}
