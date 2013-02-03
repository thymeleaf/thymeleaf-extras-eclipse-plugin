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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.contentassist.ProcessorCache;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;
import org.w3c.dom.Node;
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
			IDocument document = context.getDocument();
			int cursorposition = context.getInvocationOffset();
			Node node = (Node)ContentAssistUtils.getNodeAt(context.getViewer(), cursorposition);
			String pattern = findPattern(document, cursorposition);

			// Make processor proposals
			if (isProcessorNamePattern(pattern)) {

				// Collect attribute processors if we're in an HTML element
				if (node instanceof IDOMElement) {

					// Make sure there's some whitespace before new attribute suggestions
					if (!pattern.isEmpty() || (pattern.isEmpty() && (cursorposition == 0 ||
							Character.isWhitespace(document.getChar(cursorposition - 1))))) {

						List<AttributeProcessor> processors = ProcessorCache.getAttributeProcessors(
								findCurrentJavaProject(), findNodeNamespaces(node), pattern);
						for (AttributeProcessor processor: processors) {
							proposals.add(new AttributeProcessorCompletionProposal(processor,
									pattern.length(), cursorposition));
						}
					}
				}

				// Collect element processors if we're in an HTML text node
				else if (node instanceof IDOMText) {
					List<ElementProcessor> processors = ProcessorCache.getElementProcessors(
							findCurrentJavaProject(), findNodeNamespaces(node), pattern);
					for (ElementProcessor processor: processors) {
						proposals.add(new ElementProcessorCompletionProposal(processor,
								pattern.length(), cursorposition));
					}
				}
			}

			// Make an expression object method proposal
			else if (isExpressionObjectMethodPattern(pattern)) {
				List<ExpressionObjectMethod> expressionobjectmethods =
						ProcessorCache.getExpressionObjectMethods(findCurrentJavaProject(),
								findNodeNamespaces(node), pattern);
				for (ExpressionObjectMethod expressionobject: expressionobjectmethods) {
					proposals.add(new ExpressionObjectMethodCompletionProposal(expressionobject,
							pattern.length(), cursorposition));
				}
			}
		}
		catch (BadLocationException ex) {
			ex.printStackTrace();
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
	 * Return the pattern before the cursor position.
	 * 
	 * @param document
	 * @param cursorposition
	 * @return The text entered up to the document offset, if the text could
	 * 		   constitute a processor or expression object name.
	 * @throws BadLocationException
	 */
	private static String findPattern(IDocument document, int cursorposition) throws BadLocationException {

		// Trace backwards from the cursor until we hit a character that can't be in
		// a processor or expression object name
		int position = cursorposition;
		int length = 0;
		while (--position > 0 && isProcessorOrExpressionObjectMethodChar(document.getChar(position))) {
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
