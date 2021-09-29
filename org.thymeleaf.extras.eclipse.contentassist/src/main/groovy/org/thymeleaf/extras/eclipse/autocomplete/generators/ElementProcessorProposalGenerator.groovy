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

package org.thymeleaf.extras.eclipse.autocomplete.generators

import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.IWorkbench
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.thymeleaf.extras.eclipse.autocomplete.proposals.ElementProcessorCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.w3c.dom.Node

import javax.inject.Inject
import javax.inject.Named

/**
 * Proposal generator for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
@Named
class ElementProcessorProposalGenerator implements ProposalGenerator<ElementProcessorCompletionProposal> {

	@Inject
	private final DialectCache dialectCache
	@Inject
	private final ImageRegistry imageRegistry
	@Inject
	private final IWorkbench workbench

	/**
	 * Collect element processor suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorPosition
	 * @return List of element processor suggestions.
	 */
	private List<ElementProcessorCompletionProposal> computeElementProcessorSuggestions(Node node, IDocument document,
		int cursorPosition) {

		def pattern = document.findProcessorNamePattern(cursorPosition)
		return dialectCache.getElementProcessors(workbench.currentJavaProject, node.knownNamespaces, pattern)
			.collect { processor ->
				return new ElementProcessorCompletionProposal(imageRegistry, processor, pattern.length(), cursorPosition)
			}
	}

	@Override
	List<ElementProcessorCompletionProposal> generate(Node node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IDocument document, int cursorPosition) {

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
	private static boolean makeElementProcessorSuggestions(Node node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IDocument document, int cursorPosition) {

		// If we're in a text node, then the first non-whitespace character before
		// the cursor in the document should be an opening bracket
		if (node.textNode) {
			def position = cursorPosition - 1
			while (position >= 0 && document.getChar(position).whitespace) {
				position--
			}
			return document.getChar(position) == '<'
		}

		// If we're in an element node, then the previous text region should be an
		// opening XML tag
		if (node.elementNode) {
			if (textRegion) {
				def textRegions = documentRegion.regions
				def currentTextRegionIndex = textRegions.indexOf(textRegion)
				if (currentTextRegionIndex > 1) {
					def previousRegion = textRegions.get(currentTextRegionIndex - 1)
					return (previousRegion.type == DOMRegionContext.XML_TAG_OPEN) &&
						!document.getChar(cursorPosition - 1).whitespace
				}
			}
		}

		return false
	}
}
