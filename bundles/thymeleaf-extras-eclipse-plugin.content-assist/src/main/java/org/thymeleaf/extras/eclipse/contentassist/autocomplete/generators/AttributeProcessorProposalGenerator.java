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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.AttributeProcessorCompletionProposal;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeRestrictions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.findCurrentJavaProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proposal generator for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class AttributeProcessorProposalGenerator
	extends AbstractItemProposalGenerator<AttributeProcessorCompletionProposal> {

	/**
	 * Collect attribute processor suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorposition
	 * @return List of attribute processor suggestions.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private static List<AttributeProcessorCompletionProposal> computeAttributeProcessorSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorposition) throws BadLocationException {

		String pattern = findProcessorNamePattern(document, cursorposition);

		List<AttributeProcessor> processors = DialectCache.getAttributeProcessors(
				findCurrentJavaProject(), findNodeNamespaces(node), pattern);
		if (!processors.isEmpty()) {
			ArrayList<AttributeProcessorCompletionProposal> proposals =
					new ArrayList<AttributeProcessorCompletionProposal>();
			NamedNodeMap existingattributes = node.getAttributes();

			// Go through twice so that we create data-* suggestions as well
			createAttributeProcessorSuggestions(pattern, processors, existingattributes,
					node, cursorposition, proposals, false);
			createAttributeProcessorSuggestions(pattern, processors, existingattributes,
					node, cursorposition, proposals, true);

			return proposals;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * Creates and adds attribute processor proposals for whether or not they
	 * should use the standard or data-* version.
	 * 
	 * @param pattern            The input string entered by the user so far.
	 * @param processors         List of processors that matched the pattern.
	 * @param existingattributes
	 * @param node
	 * @param cursorposition
	 * @param proposals          List of proposals to add to.
	 * @param dataattr           Use the data-* version of the processor.
	 */
	private static void createAttributeProcessorSuggestions(String pattern,
		List<AttributeProcessor> processors, NamedNodeMap existingattributes, IDOMNode node,
		int cursorposition, ArrayList<AttributeProcessorCompletionProposal> proposals,
		boolean dataattr) {

		for (AttributeProcessor processor: processors) {

			// Double check that the processor type being used this time around
			// matches the pattern
			if ((!dataattr && !processor.getFullName().startsWith(pattern)) ||
				(dataattr && !processor.getFullDataName().startsWith(pattern))) {
				continue;
			}

			AttributeProcessorCompletionProposal proposal = new AttributeProcessorCompletionProposal(
					processor, pattern.length(), cursorposition, dataattr);

			// Only include the proposal if it isn't already in the element
			if (existingattributes.getNamedItem(proposal.getDisplayString()) == null) {
				boolean restricted = false;

				// If a restriction is present, make sure it is satisfied before including the proposal
				if (processor.isSetRestrictions()) {
					AttributeRestrictions restrictions = processor.getRestrictions();

					if (restrictions.isSetTags()) {
						List<String> tags = restrictions.getTags();
						String elementname = node.getNodeName();

						for (String tag: tags) {
							if (tag.startsWith("-")) {
								if (tag.substring(1).equals(elementname)) {
									restricted = true;
									break;
								}
							}
							else if (!tag.equals(elementname)) {
								restricted = true;
								break;
							}
						}
					}

					if (restrictions.isSetAttributes()) {
						for (String attribute: restrictions.getAttributes()) {
							if (!matchAttributeRestriction(attribute, existingattributes)) {
								restricted = true;
								break;
							}
						}
					}
				}

				if (!restricted) {
					proposals.add(proposal);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<AttributeProcessorCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion,
		IStructuredDocument document, int cursorposition) throws BadLocationException {

		return makeAttributeProcessorSuggestions(node, textregion, documentregion, document, cursorposition) ?
				computeAttributeProcessorSuggestions(node, document, cursorposition) :
				Collections.EMPTY_LIST;
	}

	/**
	 * Check if, given everything, attribute processor suggestions should be
	 * made.
	 * 
	 * @param node
	 * @param textregion
	 * @param documentregion
	 * @param document
	 * @param cursorposition
	 * @return <tt>true</tt> if attribute processor suggestions should be made.
	 * @throws BadLocationException
	 */
	private static boolean makeAttributeProcessorSuggestions(IDOMNode node, ITextRegion textregion,
		IStructuredDocumentRegion documentregion, IStructuredDocument document, int cursorposition)
		throws BadLocationException {

		if (node.getNodeType() == IDOMNode.ELEMENT_NODE) {
			if (Character.isWhitespace(document.getChar(cursorposition - 1))) {
				return true;
			}
			if (textregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				return true;
			}
			ITextRegionList textregionlist = documentregion.getRegions();
			ITextRegion previousregion = textregionlist.get(textregionlist.indexOf(textregion) - 1);
			if (previousregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if an attribute processor proposal should be made given the
	 * attribute restriction.
	 * 
	 * @param restriction
	 * @param existingattributes
	 * @return <tt>true</tt> if an attribute processor can be proposed because
	 *         it passed the current restriction.
	 */
	private static boolean matchAttributeRestriction(String restriction,
		NamedNodeMap existingattributes) {

		// Break the restriction into its parts
		String restrictionName;
		String restrictionValue;
		if (restriction.contains("=")) {
			int indexOfEq = restriction.indexOf('=');
			restrictionName  = restriction.substring(0, indexOfEq);
			restrictionValue = restriction.substring(indexOfEq + 1);
		}
		else {
			restrictionName  = restriction;
			restrictionValue = null;
		}

		// Flag to indicate if this is a restriction that the attribute _shouldn't_ be there
		boolean negate = restriction.startsWith("-");
		if (negate) {
			restrictionName = restrictionName.substring(1);
		}

		// Check restriction against other attributes in the element
		Node attribute = existingattributes.getNamedItem(restrictionName);
		boolean allow = true;
		if (attribute == null) {
			allow = false;
		}
		else if (restrictionValue != null) {
			String attributeValue = attribute.getNodeValue();
			if (attributeValue != null && !attributeValue.equals(restrictionValue)) {
				allow = false;
			}
		}

		return negate ? !allow : allow;
	}
}
