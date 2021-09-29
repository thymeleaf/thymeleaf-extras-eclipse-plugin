/* 
 * Copyright 2014, The Thymeleaf Project (http://www.thymeleaf.org/)
 * 
 * Licensed under the Apache License, Version 2.0 (the 'License')
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thymeleaf.extras.eclipse.autocomplete.generators

import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.IWorkbench
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.thymeleaf.extras.eclipse.autocomplete.proposals.ExpressionObjectMethodCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.w3c.dom.Node

import javax.inject.Inject
import javax.inject.Named

/**
 * Proposal generator for Thymeleaf expression objects.
 * 
 * @author Emanuel Rabina
 */
@Named
class ExpressionObjectProposalGenerator implements ProposalGenerator<ExpressionObjectMethodCompletionProposal> {

	@Inject
	private final DialectCache dialectCache
	@Inject
	private final ImageRegistry imageRegistry
	@Inject
	private final IWorkbench workbench
	
	/**
	 * Collect expression object method suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorPosition
	 * @return List of expression object method suggestions
	 */
	private List<ExpressionObjectMethodCompletionProposal> computeExpressionObjectMethodSuggestions(Node node,
		IDocument document, int cursorPosition) {

		def pattern = document.findExpressionObjectMethodNamePattern(cursorPosition)
		return dialectCache.getExpressionObjectMethods(workbench.currentJavaProject, node.knownNamespaces, pattern)
			.collect { expressionObject ->
				return new ExpressionObjectMethodCompletionProposal(imageRegistry, expressionObject, pattern.length(), cursorPosition)
			}
	}

	@Override
	List<ExpressionObjectMethodCompletionProposal> generate(Node node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IDocument document, int cursorPosition) {

		return makeExpressionObjectMethodSuggestions(node, textRegion) ?
				computeExpressionObjectMethodSuggestions(node, document, cursorPosition) :
				Collections.EMPTY_LIST
	}

	/**
	 * Check if, given everything, expression object method suggestions should
	 * be made.
	 * 
	 * @param node
	 * @param textRegion
	 * @return <tt>true</tt> if expression object method suggestions should be
	 * 		   made.
	 */
	private static boolean makeExpressionObjectMethodSuggestions(Node node, ITextRegion textRegion) {

		return node.elementNode && textRegion.xmlAttribute
	}
}
