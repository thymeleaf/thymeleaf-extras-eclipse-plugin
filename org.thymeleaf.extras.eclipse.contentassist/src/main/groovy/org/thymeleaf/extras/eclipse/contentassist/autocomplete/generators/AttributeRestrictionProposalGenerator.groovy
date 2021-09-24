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

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.AttributeRestrictionCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache

import javax.inject.Inject

/**
 * Proposal generator for Thymeleaf attribute restrictions.
 * 
 * @author Emanuel Rabina
 */
class AttributeRestrictionProposalGenerator implements ProposalGenerator<AttributeRestrictionCompletionProposal> {

	@Inject
	private final DialectCache dialectCache

	/**
	 * Collect attribute restriction suggestions.
	 * 
	 * @param node
	 * @param textRegion
	 * @param documentRegion
	 * @param document
	 * @param cursorPosition
	 * @return List of attribute restriction suggestions.
	 */
	private List<AttributeRestrictionCompletionProposal> computeAttributeRestrictionSuggestions(
		IDOMNode node, ITextRegion textRegion, IStructuredDocumentRegion documentRegion,
		IStructuredDocument document, int cursorPosition) {

		def textRegions = documentRegion.regions
		def attributeNameTextRegion = textRegions.get(textRegions.indexOf(textRegion) - 2)
		def attributeName = document.get(documentRegion.startOffset + attributeNameTextRegion.start,
			 attributeNameTextRegion.textLength)

		def attributeProcessor = dialectCache.getProcessor(ContentAssistPlugin.findCurrentJavaProject(), node.knownNamespaces, attributeName)
		if (attributeProcessor?.isSetRestrictions()) {

			def restrictions = attributeProcessor.restrictions
			if (restrictions.isSetValues()) {

				def proposals = new ArrayList<AttributeRestrictionCompletionProposal>()
				for (def value: restrictions.values) {
					proposals.add(new AttributeRestrictionCompletionProposal(value,
							documentRegion.getStartOffset(textRegion) + 1,
							textRegion.textLength - 2, cursorPosition))
				}
				return proposals
			}
		}

		return Collections.EMPTY_LIST
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<AttributeRestrictionCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textRegion, IStructuredDocumentRegion documentRegion, IStructuredDocument document,
		int cursorPosition) {

		return textRegion && makeAttributeRestrictionSuggestions(node, textRegion) ?
			computeAttributeRestrictionSuggestions(node, textRegion, documentRegion, document, cursorPosition) :
			Collections.EMPTY_LIST
	}

	/**
	 * Check if, given everything, attribute restriction suggestions should be
	 * made.
	 * 
	 * @param node
	 * @param textRegion
	 * @return <tt>true</tt> if attribute processor suggestions should be made.
	 */
	private static boolean makeAttributeRestrictionSuggestions(IDOMNode node, ITextRegion textRegion) {

		return node.nodeType == IDOMNode.ELEMENT_NODE && textRegion.type == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE
	}
}
