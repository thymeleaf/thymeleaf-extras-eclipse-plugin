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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.thymeleaf.extras.eclipse.SpringContainer
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.ExpressionObjectMethodCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache

/**
 * Proposal generator for Thymeleaf expression objects.
 * 
 * @author Emanuel Rabina
 */
class ExpressionObjectProposalGenerator extends AbstractItemProposalGenerator<ExpressionObjectMethodCompletionProposal> {

	private final DialectCache dialectCache = SpringContainer.instance.getBean(DialectCache)
	
	/**
	 * Collect expression object method suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorPosition
	 * @return List of expression object method suggestions
	 */
	private List<ExpressionObjectMethodCompletionProposal> computeExpressionObjectMethodSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorPosition) {

		def pattern = document.findExpressionObjectMethodNamePattern(cursorPosition)
		return dialectCache.getExpressionObjectMethods(ContentAssistPlugin.findCurrentJavaProject(), node.knownNamespaces, pattern)
			.collect { expressionObject ->
				return new ExpressionObjectMethodCompletionProposal(expressionObject, pattern.length(), cursorPosition)
			}
	}

	@Override
	List<ExpressionObjectMethodCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textRegion, IStructuredDocumentRegion documentRegion, IStructuredDocument document,
		int cursorPosition) {

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
	private static boolean makeExpressionObjectMethodSuggestions(IDOMNode node, ITextRegion textRegion) {

		return node.nodeType == IDOMNode.ELEMENT_NODE && textRegion?.type == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE
	}
}
