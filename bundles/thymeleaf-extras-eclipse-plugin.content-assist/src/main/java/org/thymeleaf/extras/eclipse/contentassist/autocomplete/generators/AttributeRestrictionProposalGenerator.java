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
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.AttributeRestrictionCompletionProposal;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeRestrictions;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proposal generator for Thymeleaf attribute restrictions.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class AttributeRestrictionProposalGenerator
	extends AbstractItemProposalGenerator<AttributeRestrictionCompletionProposal> {

	/**
	 * Collect attribute restriction suggestions.
	 * 
	 * @param node
	 * @param textregion
	 * @param documentregion
	 * @param document
	 * @param cursorposition
	 * @return List of attribute restriction suggestions.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private static List<AttributeRestrictionCompletionProposal> computeAttributeRestrictionSuggestions(
		IDOMNode node, ITextRegion textregion, IStructuredDocumentRegion documentregion,
		IStructuredDocument document, int cursorposition) throws BadLocationException {

		try {
			ITextRegionList textregions = documentregion.getRegions();
			ITextRegion attributenametextregion = textregions.get(textregions.indexOf(textregion) - 2);
			String attributename = document.get(documentregion.getStartOffset() +
					attributenametextregion.getStart(), attributenametextregion.getTextLength());

			AttributeProcessor attributeprocessor = (AttributeProcessor)DialectCache.getProcessor(
					findCurrentJavaProject(), findNodeNamespaces(node), attributename);
			if (attributeprocessor != null && attributeprocessor.isSetRestrictions()) {

				AttributeRestrictions restrictions = attributeprocessor.getRestrictions();
				if (restrictions.isSetValues()) {

					ArrayList<AttributeRestrictionCompletionProposal> proposals =
							new ArrayList<AttributeRestrictionCompletionProposal>();
					for (String value: restrictions.getValues()) {
						proposals.add(new AttributeRestrictionCompletionProposal(value,
								documentregion.getStartOffset(textregion) + 1,
								textregion.getTextLength() - 2, cursorposition));
					}
					return proposals;
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException ex) {
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<AttributeRestrictionCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion, IStructuredDocument document,
		int cursorposition) throws BadLocationException {

		return makeAttributeRestrictionSuggestions(node, textregion) ?
				computeAttributeRestrictionSuggestions(node, textregion, documentregion, document, cursorposition) :
				Collections.EMPTY_LIST;
	}

	/**
	 * Check if, given everything, attribute restriction suggestions should be
	 * made.
	 * 
	 * @param node
	 * @param textregion
	 * @return <tt>true</tt> if attribute processor suggestions should be made.
	 */
	private static boolean makeAttributeRestrictionSuggestions(IDOMNode node, ITextRegion textregion) {

		if (node.getNodeType() == IDOMNode.ELEMENT_NODE &&
			textregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
			return true;
		}
		return false;
	}
}
