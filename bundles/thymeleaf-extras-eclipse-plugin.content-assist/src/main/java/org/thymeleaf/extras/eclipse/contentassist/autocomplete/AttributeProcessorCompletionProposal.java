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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import org.thymeleaf.extras.eclipse.dialect.xml.ProcessorDocumentation;

import java.util.List;

/**
 * A completion proposal for the Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
public class AttributeProcessorCompletionProposal implements ICompletionProposal, ICompletionProposalExtension {

	private final String fullprocessorname;
	private final String replacementstring;
	private final int cursorposition;

	private final String additionalproposalinfo;
	private final IContextInformation contextinformation;

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf standard
	 * attribute processor.
	 * 
	 * @param processor		  Attribute processor being proposed.
	 * @param charsentered	  How much of the entire proposal has already been
	 * 						  entered by the user.
	 * @param cursorposition
	 */
	public AttributeProcessorCompletionProposal(AttributeProcessor processor,
		int charsentered, int cursorposition) {

		String dialectprefix = processor.getDialect().getPrefix();

		this.fullprocessorname = dialectprefix + ":" + processor.getName();
		this.replacementstring = fullprocessorname.substring(charsentered);
		this.cursorposition    = cursorposition;

		this.additionalproposalinfo = processor.isSetDocumentation() ?
				generateDocumentation(dialectprefix, processor.getDocumentation()) : null;
		this.contextinformation     = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(IDocument document) {

		apply(document, '\0', cursorposition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(IDocument document, char trigger, int offset) {

		try {
			document.replace(offset, 0, replacementstring.substring(offset - cursorposition) + "=\"\"");
		}
		catch (BadLocationException ex) {
			// Do nothing?
			ex.printStackTrace();
		}
	}

	/**
	 * Creates the documentation/help text to go alongside this suggestion.
	 * 
	 * @param dialectprefix
	 * @param documentation
	 * @return Documentation string.
	 */
	private static String generateDocumentation(String dialectprefix, ProcessorDocumentation documentation) {

		StringBuilder doctext = new StringBuilder(documentation.getValue());

		// Generate 'see also' text
		if (documentation.isSetSeeAlso()) {
			doctext.append("<br/><br/><b>See also:</b> ");

			List<Object> seealsolist = documentation.getSeeAlso();
			for (int i = 0; i < seealsolist.size(); i++) {
				String seealso = ((Processor)seealsolist.get(i)).getName();
				doctext.append(dialectprefix + ":" + ((i < seealsolist.size() - 1) ? seealso + ", " : seealso));
			}
		}

		// Generate 'document reference' text
		if (documentation.isSetReference()) {
			doctext.append((documentation.isSetSeeAlso() ? "<br/>" : "<br/><br/>") + "<b>Reference:</b> ");
			doctext.append(documentation.getReference());
		}

		return doctext.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAdditionalProposalInfo() {

		return additionalproposalinfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContextInformation getContextInformation() {

		return contextinformation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getContextInformationPosition() {

		return contextinformation == null ? -1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return fullprocessorname;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {

		return ContentAssistPlugin.getAttributeImage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(cursorposition + replacementstring.length() + 2, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char[] getTriggerCharacters() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidFor(IDocument document, int offset) {

		try {
			// Use this proposal if the characters typed since it was suggested still
			// match the string this proposal will insert into the document
			return replacementstring.startsWith(document.get(cursorposition, offset - cursorposition));
		}
		catch (BadLocationException ex) {
			return false;
		}
	}
}
