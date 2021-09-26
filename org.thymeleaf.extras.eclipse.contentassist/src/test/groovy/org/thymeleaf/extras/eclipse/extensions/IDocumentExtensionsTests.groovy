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

import org.eclipse.jface.text.IDocument
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

/**
 * Tests for the common proposal generator methods.
 * 
 * @author Emanuel Rabina
 */
class IDocumentExtensionsTests {

	@ParameterizedTest
	@ValueSource(ints = [0, 1, 3, 7])
	void findProcessorNamePatternReturnsTextUpToCursor(int cursorPosition) {

		def testString = 'th:test'
		def document = mock(IDocument)
		when(document.getChar(anyInt())).thenAnswer({ invocation ->
			def index = invocation.getArgument(0)
			return testString.charAt(index)
		})
		when(document.get(anyInt(), anyInt())).thenAnswer({ invocation ->
			def (start, end) = invocation.arguments
			return testString[start..<end]
		})

		def result = IDocumentExtensions.findProcessorNamePattern(document, cursorPosition)

		assertEquals(testString.substring(0, cursorPosition), result)
	}
}
