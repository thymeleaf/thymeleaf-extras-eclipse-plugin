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
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.ExpressionObjectMethodCompletionProposal;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;

import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.findCurrentJavaProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proposal generator for Thymeleaf expression objects.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class ExpressionObjectProposalGenerator
	extends AbstractItemProposalGenerator<ExpressionObjectMethodCompletionProposal> {

	/**
	 * Collect expression object method suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorposition
	 * @return List of expression object method suggestions
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private static List<ExpressionObjectMethodCompletionProposal> computeExpressionObjectMethodSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorposition) throws BadLocationException {

		String pattern = findExpressionObjectMethodNamePattern(document, cursorposition);

		List<ExpressionObjectMethod> expressionobjectmethods = DialectCache.getExpressionObjectMethods(
				findCurrentJavaProject(), findNodeNamespaces(node), pattern);
		if (!expressionobjectmethods.isEmpty()) {
			ArrayList<ExpressionObjectMethodCompletionProposal> proposals =
					new ArrayList<ExpressionObjectMethodCompletionProposal>();
			for (ExpressionObjectMethod expressionobject: expressionobjectmethods) {
				proposals.add(new ExpressionObjectMethodCompletionProposal(expressionobject,
						pattern.length(), cursorposition));
			}
			return proposals;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * Return the expression object method name pattern before the cursor
	 * position.
	 * 
	 * @param document
	 * @param cursorposition
	 * @return The text entered up to the document offset, if the text could
	 * 		   constitute an expression object method name.
	 * @throws BadLocationException
	 */
	private static String findExpressionObjectMethodNamePattern(IDocument document, int cursorposition)
		throws BadLocationException {

		int position = cursorposition;
		int length = 0;
		while (--position > 0 && isExpressionObjectMethodChar(document.getChar(position))) {
			length++;
		}
		return document.get(position + 1, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ExpressionObjectMethodCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion, IStructuredDocument document,
		int cursorposition) throws BadLocationException {

		return makeExpressionObjectMethodSuggestions(node, textregion) ?
				computeExpressionObjectMethodSuggestions(node, document, cursorposition) :
				Collections.EMPTY_LIST;
	}

	/**
	 * Check if, given everything, expression object method suggestions should
	 * be made.
	 * 
	 * @param node
	 * @param textregion
	 * @return <tt>true</tt> if expression object method suggestions should be
	 * 		   made.
	 */
	private static boolean makeExpressionObjectMethodSuggestions(IDOMNode node, ITextRegion textregion) {

		if (node.getNodeType() == IDOMNode.ELEMENT_NODE &&
			textregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
			return true;
		}

		return false;
	}
}
