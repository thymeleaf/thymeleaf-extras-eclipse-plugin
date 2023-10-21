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
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.thymeleaf.extras.eclipse.autocomplete.proposals.AttributeProcessorCompletionProposal
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.wrappers.JavaProjectLocator
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

import jakarta.inject.Inject
import jakarta.inject.Named

/**
 * Proposal generator for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
@Named
class AttributeProcessorProposalGenerator implements ProposalGenerator<AttributeProcessorCompletionProposal> {

	@Inject
	private final DialectCache dialectCache
	@Inject
	private final ImageRegistry imageRegistry
	@Inject
	private final JavaProjectLocator javaProjectLocator

	/**
	 * Check if attribute processor suggestions can be made.
	 * 
	 * @param node
	 * @param textRegion
	 * @param documentRegion
	 * @param document
	 * @param cursorPosition
	 * @return {@code true} if attribute processor suggestions can be made.
	 */
	private static boolean canMakeProposals(Node node, ITextRegion textRegion, IStructuredDocumentRegion documentRegion,
		IDocument document, int cursorPosition) {

		if (node.elementNode) {
			if (cursorPosition > 0 && document.getChar(cursorPosition - 1).whitespace) {
				return true
			}
			if (textRegion) {
				if (textRegion.xmlAttribute) {
					return true
				}
				def textRegions = documentRegion.regions
				def currentTextRegionIndex = textRegions.indexOf(textRegion)
				if (currentTextRegionIndex > 0) {
					def previousRegion = textRegions.get(currentTextRegionIndex - 1)
					if (previousRegion.xmlAttribute) {
						return true
					}
				}
			}
		}
		return false
	}

	/**
	 * Creates and adds attribute processor proposals for whether or not they
	 * should use the standard or data-* version.
	 * 
	 * @param pattern
	 *   The input string entered by the user so far.
	 * @param processors
	 *   List of processors that matched the pattern.
	 * @param existingAttributes
	 * @param node
	 * @param cursorPosition
	 * @param proposals
	 *   List of proposals to add to.
	 * @param dataAttr
	 *   Use the data-* version of the processor.
	 */
	private ArrayList<AttributeProcessorCompletionProposal> createAttributeProcessorSuggestions(String pattern,
		List<AttributeProcessor> processors, NamedNodeMap existingAttributes, Node node, int cursorPosition,
		boolean dataAttr) {

		def proposals = []
		for (def processor: processors) {

			// Double check that the processor type being used this time around matches the pattern
			if ((!dataAttr && !processor.fullName.startsWith(pattern)) ||
				(dataAttr && !processor.fullDataName.startsWith(pattern))) {
				continue
			}

			def proposal = new AttributeProcessorCompletionProposal(imageRegistry, processor, pattern.length(), cursorPosition, dataAttr)

			// Only include the proposal if it isn't already in the element
			if (existingAttributes.getNamedItem(proposal.displayString) == null) {
				boolean restricted = false

				// If a restriction is present, make sure it is satisfied before including the proposal
				if (processor.isSetRestrictions()) {
					def restrictions = processor.restrictions

					if (restrictions.isSetTags()) {
						def tags = restrictions.tags
						def elementName = node.nodeName

						for (def tag: tags) {
							if (tag.startsWith('-')) {
								if (tag.substring(1) == elementName) {
									restricted = true
									break
								}
							}
							else {
								if (tag == elementName) {
									restricted = false
									break
								}
								restricted = true
							}
						}
					}

					if (restrictions.isSetAttributes()) {
						for (def attribute: restrictions.attributes) {
							if (!matchAttributeRestriction(attribute, existingAttributes)) {
								restricted = true
								break
							}
						}
					}
				}

				if (!restricted) {
					proposals.add(proposal)
				}
			}
		}
		return proposals
	}

	@Override
	List<AttributeProcessorCompletionProposal> generate(Node node, ITextRegion textRegion,
		IStructuredDocumentRegion documentRegion, IDocument document, int cursorPosition) {

		if (canMakeProposals(node, textRegion, documentRegion, document, cursorPosition)) {
			def pattern = document.findProcessorNamePattern(cursorPosition)

			def processors = dialectCache.getAttributeProcessors(javaProjectLocator.locate(), node.knownNamespaces, pattern)
			if (processors) {
				def proposals = []
				def existingAttributes = node.attributes

				// Go through twice so that we create data-* suggestions as well
				proposals.addAll(createAttributeProcessorSuggestions(pattern, processors, existingAttributes, node, cursorPosition, false))
				proposals.addAll(createAttributeProcessorSuggestions(pattern, processors, existingAttributes, node, cursorPosition, true))

				return proposals
			}
		}
		return []
	}

	/**
	 * Checks if an attribute processor proposal should be made given the
	 * attribute restriction.
	 * 
	 * @param restriction
	 * @param existingAttributes
	 * @return <tt>true</tt> if an attribute processor can be proposed because
	 *         it passed the current restriction.
	 */
	private static boolean matchAttributeRestriction(String restriction, NamedNodeMap existingAttributes) {

		// Break the restriction into its parts
		String restrictionName
		String restrictionValue
		if (restriction.contains('=')) {
			int indexOfEq = restriction.indexOf('=')
			restrictionName  = restriction.substring(0, indexOfEq)
			restrictionValue = restriction.substring(indexOfEq + 1)
		}
		else {
			restrictionName  = restriction
			restrictionValue = null
		}

		// Flag to indicate if this is a restriction that the attribute _shouldn't_ be there
		def negate = restriction.startsWith('-')
		if (negate) {
			restrictionName = restrictionName.substring(1)
		}

		// Check restriction against other attributes in the element
		def attribute = existingAttributes.getNamedItem(restrictionName)
		def allow = true
		if (!attribute) {
			allow = false
		}
		else if (!restrictionValue && attribute.nodeValue != restrictionValue) {
			allow = false
		}

		return negate ? !allow : allow
	}
}
