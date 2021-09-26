/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package org.thymeleaf.extras.eclipse.dialect.cache

import org.eclipse.jdt.core.IJavaProject
import org.junit.jupiter.api.Test
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.thymeleaf.extras.eclipse.TestCoreConfig
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

import javax.inject.Inject

/**
 * Tests for the {@link DialectCache} class which is a store of dialect
 * information in the Eclipse workspace.
 * 
 * @author Emanuel Rabina
 */
@SpringJUnitConfig(classes = [TestCoreConfig])
class DialectCacheTests {

	@Inject
	DialectCache dialectCache

	@Test
	void doSomething() {

		System.out.println('Hello!')
		assertEquals(dialectCache.getAttributeProcessor(mock(IJavaProject), null), null)
	}
}
