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

package org.thymeleaf.extras.eclipse.contentassist.extensions

/**
 * Extensions to Java's {@link Character} class.
 * 
 * @author Emanuel Rabina
 */
class CharacterExtensions {

	/**
	 * Returns whether or not the character is a valid expression object method
	 * name character.
	 * 
	 * @return {@code true} if this character is an alphanumeric character, or the
	 *         hash or period symbols
	 */
	static boolean isExpressionObjectMethodCharacter(Character self) {

		return self.isLetterOrDigit() || self == '#' || self == '.'
	}

	/**
	 * Returns whether or not the given character is a valid processor name
	 * character.
	 * 
	 * @return {@code true} if this character is an alphanumeric character, or the
	 *         colon or hyphen symbols.
	 */
	static boolean isProcessorNameCharacter(Character self) {

		return self.isLetterOrDigit() || self == ':' || self == '-'
	}
}
