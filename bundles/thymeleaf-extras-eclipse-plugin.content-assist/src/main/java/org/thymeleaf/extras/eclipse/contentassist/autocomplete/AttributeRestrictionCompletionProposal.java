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
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

/**
 * A completion proposal for Thymeleaf attribute processors that can take only
 * certain values.
 * 
 * @author Emanuel Rabina
 */
public class AttributeRestrictionCompletionProposal extends AbstractCompletionProposal {

	private final String value;
	private final int offsetstart;
	private final int offsetlength;

	/**
	 * Constructor, creates a proposal for a Thymeleaf attribute processor
	 * value.
	 * 
	 * @param value 		 A value that the attribute processor can take.
	 * @param offsetstart
	 * @param offsetlength
	 * @param cursorposition
	 */
	public AttributeRestrictionCompletionProposal(String value, int offsetstart, int offsetlength,
		int cursorposition) {

		super(value, cursorposition);

		this.value        = value;
		this.offsetstart  = offsetstart;
		this.offsetlength = offsetlength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		int diff = offset - cursorposition;
		document.replace(offsetstart, offsetlength + diff, value.substring(diff));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {

		return getDefault().getImageRegistry().get(IMAGE_ATTRIBUTE_RESTRICTION_VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(offsetstart + value.length(), 0);
	}
}
