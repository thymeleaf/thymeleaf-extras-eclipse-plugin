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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.TestCoreConfig
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

import javax.inject.Inject

/**
 * Tests for the {@link AttributeProcessorProposalGenerator}, which comes up
 * with autocomplete entries for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
@SpringJUnitConfig(classes = [TestCoreConfig, AttributeProcessorProposalGeneratorConfig.class])
class AttributeProcessorProposalGeneratorTests {

	@Configuration
	static class AttributeProcessorProposalGeneratorConfig {
		@Bean
		AttributeProcessorProposalGenerator attributeProcessorProposalGenerator() {
			return new AttributeProcessorProposalGenerator()
		}
	}

	@Inject
	private final AttributeProcessorProposalGenerator attributeProcessorProposalGenerator

	@Test
	void generateProposals() {

		def results = attributeProcessorProposalGenerator.generateProposals(
			mock(IDOMNode), mock(ITextRegion), mock(IStructuredDocumentRegion),
			mock(IStructuredDocument), 0)
		assertEquals(results, [])
	}
}
