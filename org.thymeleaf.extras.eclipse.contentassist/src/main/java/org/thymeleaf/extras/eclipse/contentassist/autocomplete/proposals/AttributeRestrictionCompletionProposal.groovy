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
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin

/**
 * A completion proposal for Thymeleaf attribute processors that can take only
 * certain values.
 * 
 * @author Emanuel Rabina
 */
class AttributeRestrictionCompletionProposal extends AbstractCompletionProposal {

	final String displayString
	final Image image = ContentAssistPlugin.default.imageRegistry.get(ContentAssistPlugin.IMAGE_ATTRIBUTE_RESTRICTION_VALUE)
	private final int offsetStart
	private final int offsetLength

	/**
	 * Constructor, creates a proposal for a Thymeleaf attribute processor
	 * value.
	 * 
	 * @param value
	 *   A value that the attribute processor can take.
	 * @param offsetStart
	 * @param offsetLength
	 * @param cursorPosition
	 */
	// TODO: @MapConstructor?
	AttributeRestrictionCompletionProposal(String displayString, int offsetStart, int offsetLength, int cursorPosition) {

		super(displayString, cursorPosition)

		this.displayString = displayString
		this.offsetStart   = offsetStart
		this.offsetLength  = offsetLength
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) {

		int diff = offset - cursorPosition
		document.replace(offsetStart, offsetLength + diff, displayString.substring(diff))
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Point getSelection(IDocument document) {

		return new Point(offsetStart + displayString.length(), 0)
	}
}
