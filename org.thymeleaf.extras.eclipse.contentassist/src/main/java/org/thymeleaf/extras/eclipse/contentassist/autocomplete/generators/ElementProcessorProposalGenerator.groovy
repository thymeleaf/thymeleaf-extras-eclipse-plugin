/* 
 * Copyright 2014, The Thymeleaf Project (http://www.thymeleaf.org/)
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

import org.eclipse.jface.text.BadLocationException
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.thymeleaf.extras.eclipse.SpringContainer
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.ElementProcessorCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor

/**
 * Proposal generator for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings('restriction')
class ElementProcessorProposalGenerator extends AbstractItemProposalGenerator<ElementProcessorCompletionProposal> {

	private final DialectCache dialectCache = SpringContainer.instance.getBean(DialectCache)

	/**
	 * Collect element processor suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorPosition
	 * @return List of element processor suggestions.
	 */
	private List<ElementProcessorCompletionProposal> computeElementProcessorSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorPosition) {

		def pattern = findProcessorNamePattern(document, cursorPosition)
		return dialectCache.getElementProcessors(ContentAssistPlugin.findCurrentJavaProject(), findNodeNamespaces(node), pattern)
			.collect { processor ->
				return new ElementProcessorCompletionProposal(processor, pattern.length(), cursorPosition)
			}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<ElementProcessorCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textRegion, IStructuredDocumentRegion documentRegion,
		IStructuredDocument document, int cursorPosition) {

		return makeElementProcessorSuggestions(node, textRegion, documentRegion, document, cursorPosition) ?
				computeElementProcessorSuggestions(node, document, cursorPosition) :
				Collections.EMPTY_LIST
	}

	/**
	 * Check if, given everything, element processor suggestions should be made.
	 * 
	 * @param node
	 * @param textregion
	 * @param documentregion
	 * @param document
	 * @param cursorposition
	 * @return <tt>true</tt> if element processor suggestions should be made.
	 */
	private static boolean makeElementProcessorSuggestions(IDOMNode node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IStructuredDocument document, int cursorPosition) {

		switch (node.nodeType) {

		// If we're in a text node, then the first non-whitespace character before
		// the cursor in the document should be an opening bracket
		case IDOMNode.TEXT_NODE:
			def position = cursorPosition - 1
			while (position >= 0 && Character.isWhitespace(document.getChar(position))) {
				position--
			}
			return document.getChar(position) == '<'

		// If we're in an element node, then the previous text region should be an
		// opening XML tag
		case IDOMNode.ELEMENT_NODE:
			def textRegionList = documentRegion.regions
			def currentRegionIndex = textRegionList.indexOf(textRegion)
			def previousRegion = textRegionList.get(currentRegionIndex - 1)
			return (previousRegion.type == DOMRegionContext.XML_TAG_OPEN) &&
				!Character.isWhitespace(document.getChar(cursorPosition - 1))
		}

		return false
	}
}
