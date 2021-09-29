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
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod

/**
 * A completion proposal for Thymeleaf expression object methods.
 * 
 * @author Emanuel Rabina
 */
class ExpressionObjectMethodCompletionProposal extends AbstractCompletionProposal {

	final String displayString
	private final boolean javaBeanProperty

	/**
	 * Constructor, set the expression object method information.
	 * 
	 * @param imageRegistry
	 * @param method
	 *   Expression object method being proposed.
	 * @param charsEntered
	 *   How much of the entire proposal has already been entered by the user.
	 * @param cursorPosition
	 */
	ExpressionObjectMethodCompletionProposal(ImageRegistry imageRegistry, ExpressionObjectMethod method, int charsEntered, int cursorPosition) {

		super(method, method.getFullName().substring(charsEntered), cursorPosition,
			imageRegistry.get(ContentAssistPlugin.IMAGE_EXPRESSION_OBJECT_METHOD))

		displayString = method.name
		javaBeanProperty = method.javaBeanProperty
	}

	@Override
	void apply(IDocument document, char trigger, int offset) {

		document.replace(offset, 0, replacementString.substring(offset - cursorPosition) + (!javaBeanProperty ? '()' : ''))
	}

	@Override
	Point getSelection(IDocument document) {

		return new Point(cursorPosition + replacementString.length() + (!javaBeanProperty ? 1 : 0), 0)
	}
}
