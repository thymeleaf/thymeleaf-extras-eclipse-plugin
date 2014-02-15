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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AttributeProcessorProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AttributeRestrictionProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.ElementProcessorProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.ExpressionObjectProposalGenerator;

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

	@SuppressWarnings("rawtypes")
	private static AbstractItemProposalGenerator[] proposalgenerators = {
		new ElementProcessorProposalGenerator(),
		new AttributeProcessorProposalGenerator(),
		new AttributeRestrictionProposalGenerator(),
		new ExpressionObjectProposalGenerator()
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

			// Create proposals from the generators given to us by the computers
			// TODO: Can this part be made multi-threaded?  Is there any benefit
			//       in making it so?  Probably need to have some kind of
			//       ordering on returned proposals so that the results list is
			//       predictable.
			for (AbstractItemProposalGenerator proposalgenerator: proposalgenerators) {
				proposals.addAll(proposalgenerator.generateProposals(node, textregion, documentregion,
						document, cursorposition));
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
