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
import org.thymeleaf.extras.eclipse.autocomplete.proposals.AttributeRestrictionCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.w3c.dom.Node

import jakarta.inject.Inject
import jakarta.inject.Named

/**
 * Proposal generator for Thymeleaf attribute restrictions.
 * 
 * @author Emanuel Rabina
 */
@Named
class AttributeRestrictionProposalGenerator implements ProposalGenerator<AttributeRestrictionCompletionProposal> {

	@Inject
	private final DialectCache dialectCache
	@Inject
	private final ImageRegistry imageRegistry
	@Inject
	private final IWorkbench workbench

	@Override
	List<AttributeRestrictionCompletionProposal> generate(Node node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IDocument document, int cursorPosition) {

		if (node.elementNode && textRegion?.xmlAttribute) {
			def textRegions = documentRegion.regions
			def attributeNameTextRegion = textRegions.get(textRegions.indexOf(textRegion) - 2)
			def attributeName = document.get(documentRegion.startOffset + attributeNameTextRegion.start,
				attributeNameTextRegion.textLength)

			def attributeProcessor = dialectCache.getProcessor(workbench.currentJavaProject, node.knownNamespaces, attributeName)
			if (attributeProcessor?.isSetRestrictions()) {

				def restrictions = attributeProcessor.restrictions
				if (restrictions.isSetValues()) {

					def proposals = new ArrayList<AttributeRestrictionCompletionProposal>()
					for (def value: restrictions.values) {
						proposals.add(new AttributeRestrictionCompletionProposal(imageRegistry, value,
							documentRegion.getStartOffset(textRegion) + 1, textRegion.textLength - 2, cursorPosition))
					}
					return proposals
				}
			}
		}
		return []
	}
}
