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

package org.thymeleaf.extras.eclipse.autocomplete

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.thymeleaf.extras.eclipse.autocomplete.generators.AttributeProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.AttributeRestrictionProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.ElementProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.ExpressionObjectProposalGenerator
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

import javax.inject.Inject

/**
 * Tests for the {@link CompletionProposalComputer}, which aggregates
 * autocomplete suggestions from all other proposal computers.
 * 
 * @author Emanuel Rabina
 */
@SpringJUnitConfig(classes = [TestCoreConfig, CompletionProposalComputerTestsConfig.class])
class CompletionProposalComputerTests {

	@Configuration
	static class CompletionProposalComputerTestsConfig {
		@Bean
		CompletionProposalComputer completionProposalComputer() {
			return new CompletionProposalComputer(
				mock(ElementProcessorProposalGenerator),
				mock(AttributeProcessorProposalGenerator),
				mock(AttributeRestrictionProposalGenerator),
				mock(ExpressionObjectProposalGenerator)
			)
		}
	}

	@Inject
	private final CompletionProposalComputer completionProposalComputer

	@Test
	void computeCompletionProposalsAggregatesResults() {

//		def mockTextViewer = mock(ITextViewer)
//		when(mockTextViewer.getNodeAt(anyInt())).thenReturn(null)
		def mockCompletionProposalInvocationContext = mock(CompletionProposalInvocationContext)
//		when(mockCompletionProposalInvocationContext.getViewer()).thenReturn(null)

		def results = completionProposalComputer.computeCompletionProposals(
			mockCompletionProposalInvocationContext, mock(IProgressMonitor))
		assertEquals(results, [])
	}
}
