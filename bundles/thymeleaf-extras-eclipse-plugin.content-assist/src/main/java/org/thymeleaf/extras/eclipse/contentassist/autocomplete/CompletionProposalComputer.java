/*
 * Copyright 2013, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.dialect.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeRestrictions;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;
import org.w3c.dom.NamedNodeMap;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Auto-completion proposal generator for Thymeleaf processors and expression
 * object methods.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class CompletionProposalComputer extends AbstractComputer implements ICompletionProposalComputer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {

		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {
			ITextViewer viewer = context.getViewer();
			IStructuredDocument document = (IStructuredDocument)context.getDocument();
			int cursorposition = context.getInvocationOffset();

			IDOMNode node = (IDOMNode)ContentAssistUtils.getNodeAt(viewer, cursorposition);
			IStructuredDocumentRegion documentregion = ContentAssistUtils.getStructuredDocumentRegion(
					viewer, cursorposition);
			ITextRegion textregion = documentregion.getRegionAtCharacterOffset(cursorposition);

			// Figure out which type of suggestions to make
			if (makeElementProcessorSuggestions(node, textregion, documentregion, document, cursorposition)) {
				proposals.addAll(computeElementProcessorSuggestions(node, document, cursorposition));
			}
			else if (makeAttributeProcessorSuggestions(node, textregion, documentregion, document, cursorposition)) {
				proposals.addAll(computeAttributeProcessorSuggestions(node, document, cursorposition));
			}
			else {
				// Allow the combining of restriction suggestions and expression object suggestions
				if (makeAttributeRestrictionSuggestions(node, textregion)) {
					proposals.addAll(computeAttributeRestrictionSuggestions(node, textregion,
							documentregion, document, cursorposition));
				}
				if (makeExpressionObjectMethodSuggestions(node, textregion)) {
					proposals.addAll(computeExpressionObjectMethodSuggestions(node, document, cursorposition));
				}
			}
		}
		catch (BadLocationException ex) {
			logError("Unable to retrieve data at the current document position", ex);
		}

		return proposals;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {

		return Collections.EMPTY_LIST;
	}

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

			for (AttributeProcessor processor: processors) {
				AttributeProcessorCompletionProposal proposal = new AttributeProcessorCompletionProposal(
						processor, pattern.length(), cursorposition);

				// Only include the proposal if it isn't already in the element
				if (existingattributes.getNamedItem(processor.getFullName()) == null) {
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
								if (attribute.startsWith("-")) {
									if (existingattributes.getNamedItem(attribute.substring(1)) != null) {
										restricted = true;
										break;
									}
								}
								else if (existingattributes.getNamedItem(attribute) == null) {
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
			return proposals;
		}

		return Collections.EMPTY_LIST;
	}

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
	 * Return the processor name pattern before the cursor position.
	 * 
	 * @param document
	 * @param cursorposition
	 * @return The text entered up to the document offset, if the text could
	 * 		   constitute a processor name.
	 * @throws BadLocationException
	 */
	private static String findProcessorNamePattern(IDocument document, int cursorposition)
		throws BadLocationException {

		int position = cursorposition;
		int length = 0;
		while (--position > 0 && isProcessorChar(document.getChar(position))) {
			length++;
		}
		return document.get(position + 1, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getErrorMessage() {

		return null;
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

	/**
	 * Do nothing.
	 */
	@Override
	public void sessionEnded() {
	}

	/**
	 * Do nothing.
	 */
	@Override
	public void sessionStarted() {
	}
}
