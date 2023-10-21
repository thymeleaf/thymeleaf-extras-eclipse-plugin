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

package org.thymeleaf.extras.eclipse.autocomplete

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer
import org.thymeleaf.extras.eclipse.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.autocomplete.generators.ProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.AttributeProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.AttributeRestrictionProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.ElementProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.autocomplete.generators.ExpressionObjectProposalGenerator

/**
 * Auto-completion proposal generator for Thymeleaf processors and expression
 * object methods.
 * 
 * @author Emanuel Rabina
 */
class CompletionProposalComputer implements ICompletionProposalComputer {

	private final List<ProposalGenerator> proposalGenerators

	final String errorMessage = null

	/**
	 * Constructor, used by Eclipse to create an instance of this class, so
	 * defaults to using the real proposal generators for generating autocomplete
	 * results.
	 */
	CompletionProposalComputer() {

		this(
			ContentAssistPlugin.default.getBean(ElementProcessorProposalGenerator),
			ContentAssistPlugin.default.getBean(AttributeProcessorProposalGenerator),
			ContentAssistPlugin.default.getBean(AttributeRestrictionProposalGenerator),
			ContentAssistPlugin.default.getBean(ExpressionObjectProposalGenerator)
		)
	}

	/**
	 * Constructor, create a new proposal computer using the given proposal
	 * generators.
	 * 
	 * @param elementProcessorProposalGenerator
	 * @param attributeProcessorProposalGenerator
	 * @param attributeRestrictionProposalGenerator
	 * @param expressionObjectProposalGenerator
	 */
	CompletionProposalComputer(
		ElementProcessorProposalGenerator elementProcessorProposalGenerator,
		AttributeProcessorProposalGenerator attributeProcessorProposalGenerator,
		AttributeRestrictionProposalGenerator attributeRestrictionProposalGenerator,
		ExpressionObjectProposalGenerator expressionObjectProposalGenerator
	) {

		proposalGenerators = [
		  elementProcessorProposalGenerator,
			attributeProcessorProposalGenerator,
			attributeRestrictionProposalGenerator,
			expressionObjectProposalGenerator
		]
	}

	@Override
	List computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {

		def viewer = context.viewer
		if (viewer) {
			def cursorPosition = context.invocationOffset
			def documentRegion = viewer.getStructuredDocumentRegion(cursorPosition)
			if (documentRegion) {
				def node = viewer.getNodeAt(cursorPosition)
				def textRegion = documentRegion.getRegionAtCharacterOffset(cursorPosition)
				def document = context.document

				// Create proposals from the generators given to us by the computers
				return proposalGenerators.inject([]) { acc, proposalGenerator ->
					return acc + proposalGenerator.generate(node, textRegion, documentRegion, document, cursorPosition)
				}
			}
		}
		return Collections.EMPTY_LIST
	}

	@Override
	List computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {

		return Collections.EMPTY_LIST
	}

	/**
	 * Do nothing.
	 */
	@Override
	void sessionEnded() {
	}

	/**
	 * Do nothing.
	 */
	@Override
	void sessionStarted() {
	}
}
