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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.thymeleaf.extras.eclipse.dialect.xml.UtilityMethod;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

/**
 * A completion proposal for Thymeleaf expression object utility methods.
 * 
 * @author Emanuel Rabina
 */
public class UtilityMethodCompletionProposal extends AbstractCompletionProposal {

	private final String fullutilitymethodname;

	/**
	 * Constructor, set the utility method information.
	 * 
	 * @param utilitymethod	 Utility method being proposed.
	 * @param charsentered	 How much of the entire proposal has already been
	 * 						 entered by the user.
	 * @param cursorposition
	 */
	public UtilityMethodCompletionProposal(UtilityMethod utilitymethod,
		int charsentered, int cursorposition) {

		super(utilitymethod.getDialect(), ("#" + utilitymethod.getName()).substring(charsentered),
				utilitymethod.getDocumentation(), cursorposition);

		fullutilitymethodname = utilitymethod.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		document.replace(offset, 0, replacementstring.substring(offset - cursorposition) + "()");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return fullutilitymethodname;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {

		return getDefault().getImageRegistry().get(IMAGE_UTILITY_METHOD);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(cursorposition + replacementstring.length() + 1, 0);
	}
}
