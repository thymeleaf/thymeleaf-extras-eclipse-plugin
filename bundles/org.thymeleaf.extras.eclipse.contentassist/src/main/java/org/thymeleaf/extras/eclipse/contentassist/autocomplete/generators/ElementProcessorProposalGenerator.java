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
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.ElementProcessorCompletionProposal;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proposal generator for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class ElementProcessorProposalGenerator
	extends AbstractItemProposalGenerator<ElementProcessorCompletionProposal> {

	/**
	 * Collect element processor suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorposition
	 * @return List of element processor suggestions.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private static List<ElementProcessorCompletionProposal> computeElementProcessorSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorposition) throws BadLocationException {

		String pattern = findProcessorNamePattern(document, cursorposition);

		List<ElementProcessor> processors = DialectCache.getElementProcessors(
				findCurrentJavaProject(), findNodeNamespaces(node), pattern);
		if (!processors.isEmpty()) {
			ArrayList<ElementProcessorCompletionProposal> proposals =
					new ArrayList<ElementProcessorCompletionProposal>();
			for (ElementProcessor processor: processors) {
				proposals.add(new ElementProcessorCompletionProposal(processor,
						pattern.length(), cursorposition));
			}
			return proposals;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ElementProcessorCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion,
		IStructuredDocument document, int cursorposition) throws BadLocationException {

		return makeElementProcessorSuggestions(node, textregion, documentregion, document, cursorposition) ?
				computeElementProcessorSuggestions(node, document, cursorposition) :
				Collections.EMPTY_LIST;
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
	 * @throws BadLocationException
	 */
	private static boolean makeElementProcessorSuggestions(IDOMNode node, ITextRegion textregion,
		IStructuredDocumentRegion documentregion, IStructuredDocument document, int cursorposition)
		throws BadLocationException {

		switch (node.getNodeType()) {

		// If we're in a text node, then the first non-whitespace character before
		// the cursor in the document should be an opening bracket
		case IDOMNode.TEXT_NODE:
			int position = cursorposition - 1;
			while (position >= 0 && Character.isWhitespace(document.getChar(position))) {
				position--;
			}
			if (document.getChar(position) == '<') {
				return true;
			}
			break;

		// If we're in an element node, then the previous text region should be an
		// opening XML tag
		case IDOMNode.ELEMENT_NODE:
			ITextRegionList textregionlist = documentregion.getRegions();
			int currentregionindex = textregionlist.indexOf(textregion);
			try {
				ITextRegion previousregion = textregionlist.get(currentregionindex - 1);
				if ((previousregion.getType() == DOMRegionContext.XML_TAG_OPEN) &&
					!Character.isWhitespace(document.getChar(cursorposition - 1))) {
					return true;
				}
			}
			catch (ArrayIndexOutOfBoundsException ex) {
			}
			break;
		}

		return false;
	}
}
