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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AttributeProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AttributeRestrictionProposalGenerator
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.ElementProcessorProposalGenerator
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.ExpressionObjectProposalGenerator

/**
 * Auto-completion proposal generator for Thymeleaf processors and expression
 * object methods.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
class CompletionProposalComputer extends AbstractComputer implements ICompletionProposalComputer {

	private static List<AbstractItemProposalGenerator> proposalGenerators = [
		new ElementProcessorProposalGenerator(),
		new AttributeProcessorProposalGenerator(),
		new AttributeRestrictionProposalGenerator(),
		new ExpressionObjectProposalGenerator()
	]

	final String errorMessage = null

	/**
	 * {@inheritDoc}
	 */
	@Override
	List computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {

		def viewer = context.viewer
		def document = context.document
		def cursorPosition = context.invocationOffset

		def node = ContentAssistUtils.getNodeAt(viewer, cursorPosition)
		def documentRegion = ContentAssistUtils.getStructuredDocumentRegion(viewer, cursorPosition)
		def textRegion = documentRegion.getRegionAtCharacterOffset(cursorPosition)

		// Create proposals from the generators given to us by the computers
		// TODO: Can this part be made multi-threaded?  Is there any benefit
		//       in making it so?  Probably need to have some kind of
		//       ordering on returned proposals so that the results list is
		//       predictable.
		return proposalGenerators.inject([]) { acc, proposalGenerator ->
			return acc + proposalGenerator.generateProposals(node, textRegion, documentRegion, document, cursorPosition)
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
