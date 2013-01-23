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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;

/**
 * A completion proposal for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class ElementProcessorCompletionProposal extends AbstractProcessorCompletionProposal {

	private final boolean addendtag;

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf element
	 * processor.
	 * 
	 * @param processor		  Element processor being proposed.
	 * @param charsentered	  How much of the entire proposal has already been
	 * 						  entered by the user.
	 * @param cursorposition
	 */
	public ElementProcessorCompletionProposal(ElementProcessor processor,
		int charsentered, int cursorposition) {

		super(processor, charsentered, cursorposition);
		addendtag = HTMLUIPlugin.getDefault().getPreferenceStore().getBoolean(
				HTMLUIPreferenceNames.TYPING_COMPLETE_ELEMENTS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		String replacement = replacementstring.substring(offset - cursorposition) + ">";
		if (addendtag) {
			replacement += "</" + fullprocessorname + ">";
		}
		document.replace(offset, 0, replacement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(cursorposition + replacementstring.length() + 1, 0);
	}
}
