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

import org.eclipse.jface.text.IDocument

/**
 * Extensions to Eclipse's {@link IDocument} class to read from it more easily.
 * 
 * @author Emanuel Rabina
 */
class IDocumentExtensions {

	/**
	 * Return the expression object method name pattern before the cursor
	 * position.
	 * 
	 * @param document
	 * @param cursorPosition
	 * @return The text entered up to the document offset, if the text could
	 *         constitute an expression object method name.
	 */
	static String findExpressionObjectMethodNamePattern(IDocument self, int cursorPosition) {

		def position = cursorPosition
		def length = 0
		while (--position >= 0 && self.getChar(position).isExpressionObjectMethodCharacter()) {
			length++
		}
		return self.get(position + 1, length)
	}

	/**
	 * Read the string before the current cursor position, returning it if it
	 * forms a partial or complete Thymeleaf processor name pattern.
	 * 
	 * @param self
	 * @param cursorPosition
	 * @return The text entered up to the cursor position, if the text could
	 *         constitute a processor name.
	 */
	static String findProcessorNamePattern(IDocument self, int cursorPosition) {

		def position = cursorPosition
		def length = 0
		while (--position >= 0 && self.getChar(position).isProcessorNameCharacter()) {
			length++
		}
		return self.get(position + 1, length)
	}
}
