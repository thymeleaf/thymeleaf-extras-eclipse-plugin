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

import org.eclipse.jface.text.IDocument
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor

/**
 * A completion proposal for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
class ElementProcessorCompletionProposal extends AbstractCompletionProposal {

	final String displayString
	final Image image = ContentAssistPlugin.default.imageRegistry.get(ContentAssistPlugin.IMAGE_ELEMENT_PROCESSOR)
	private final boolean addEndTag = HTMLUIPlugin.default.preferenceStore.getBoolean(HTMLUIPreferenceNames.TYPING_COMPLETE_ELEMENTS)

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf element
	 * processor.
	 * 
	 * @param processor
	 *   Element processor being proposed.
	 * @param charsEntered
	 *   How much of the entire proposal has already been entered by the user.
	 * @param cursorposition
	 */
	ElementProcessorCompletionProposal(ElementProcessor processor, int charsEntered, int cursorPosition) {

		super(processor, processor.fullName.substring(charsEntered), cursorPosition)

		this.displayString = processor.fullName
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) {

		def replacement = "${replacementString.substring(offset - cursorPosition)}>"
		if (addEndTag) {
			replacement += "</${displayString}>"
		}
		document.replace(offset, 0, replacement)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Point getSelection(IDocument document) {

		return new Point(cursorPosition + replacementString.length() + 1, 0)
	}
}
