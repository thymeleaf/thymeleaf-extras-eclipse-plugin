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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals

import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IDocument
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*

/**
 * A completion proposal for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
class AttributeProcessorCompletionProposal extends AbstractCompletionProposal {

	final String displayString
	final Image image = getDefault().imageRegistry.get(IMAGE_ATTRIBUTE_PROCESSOR)

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf attribute
	 * processor.
	 * 
	 * @param processor
	 *   Attribute processor being proposed.
	 * @param charsEntered
	 *   How much of the entire proposal has already been entered by the user.
	 * @param cursorPosition
	 * @param dataAttr
	 *   Whether the data-* version of this processor should be used for the
	 *   proposal.
	 */
	AttributeProcessorCompletionProposal(AttributeProcessor processor,
		int charsEntered, int cursorPosition, boolean dataAttr) {

		super(processor,
				!dataAttr ? processor.fullName.substring(charsEntered) :
				            processor.fullDataName.substring(charsEntered),
				cursorPosition)

		this.displayString = !dataAttr ? processor.fullName : processor.fullDataName
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) {

		document.replace(offset, 0, replacementString.substring(offset - cursorPosition) + '=\'\'')
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Point getSelection(IDocument document) {

		return new Point(cursorPosition + replacementString.length() + 2, 0)
	}
}
