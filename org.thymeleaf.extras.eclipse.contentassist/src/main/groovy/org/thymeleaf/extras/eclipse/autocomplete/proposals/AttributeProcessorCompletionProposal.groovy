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

package org.thymeleaf.extras.eclipse.autocomplete.proposals

import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.jface.text.IDocument
import org.eclipse.swt.graphics.Point
import org.thymeleaf.extras.eclipse.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor

/**
 * A completion proposal for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
class AttributeProcessorCompletionProposal extends AbstractCompletionProposal {

	final String displayString

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf attribute
	 * processor.
	 * 
	 * @param imageRegistry
	 * @param processor
	 *   Attribute processor being proposed.
	 * @param charsEntered
	 *   How much of the entire proposal has already been entered by the user.
	 * @param cursorPosition
	 * @param dataAttr
	 *   Whether the data-* version of this processor should be used for the
	 *   proposal.
	 */
	AttributeProcessorCompletionProposal(ImageRegistry imageRegistry, AttributeProcessor processor, int charsEntered,
		int cursorPosition, boolean dataAttr) {

		super(processor,
			!dataAttr ? processor.fullName.substring(charsEntered) :
			            processor.fullDataName.substring(charsEntered),
			cursorPosition,
			imageRegistry.get(ContentAssistPlugin.IMAGE_ATTRIBUTE_PROCESSOR))

		this.displayString = !dataAttr ? processor.fullName : processor.fullDataName
	}

	@Override
	void apply(IDocument document, char trigger, int offset) {

		document.replace(offset, 0, replacementString.substring(offset - cursorPosition) + '=""')
	}

	@Override
	Point getSelection(IDocument document) {

		return new Point(cursorPosition + replacementString.length() + 2, 0)
	}
}
