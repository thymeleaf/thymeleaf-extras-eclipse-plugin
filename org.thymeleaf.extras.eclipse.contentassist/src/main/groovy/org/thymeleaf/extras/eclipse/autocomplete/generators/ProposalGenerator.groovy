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

import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.w3c.dom.Node

/**
 * Class for generating Eclipse autocompletion proposals.
 * 
 * @author Emanuel Rabina
 * @param <P> The type of item proposal being generated.
 */
interface ProposalGenerator<P extends ICompletionProposal> {

	/**
	 * Generate the autocomplete proposals.
	 * 
	 * @param node
	 * @param textRegion
	 * @param documentRegion
	 * @param document
	 * @param cursorPosition
	 * @return List of autocomplete proposals.
	 */
	List<P> generateProposals(Node node, ITextRegion textRegion, IStructuredDocumentRegion documentRegion,
		IDocument document, int cursorPosition)
}
