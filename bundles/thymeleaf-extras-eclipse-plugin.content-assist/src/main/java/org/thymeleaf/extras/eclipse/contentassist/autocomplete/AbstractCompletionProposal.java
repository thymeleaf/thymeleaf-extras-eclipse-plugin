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
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.Documentation;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.List;

/**
 * Common code for all processor and expression object completion proposals.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractCompletionProposal implements ICompletionProposal, ICompletionProposalExtension {

	protected final String replacementstring;
	protected final int cursorposition;

	protected final String additionalproposalinfo;
	protected final IContextInformation contextinformation;

	/**
	 * Subclass constructor, set completion information.
	 * 
	 * @param dialectitem
	 * @param replacementstring Value to be entered into the document if this
	 * 							proposal is selected.
	 * @param cursorposition
	 */
	protected AbstractCompletionProposal(DialectItem dialectitem, String replacementstring,
		int cursorposition) {

		this.replacementstring = replacementstring;
		this.cursorposition    = cursorposition;

		this.additionalproposalinfo = generateDocumentation(dialectitem);
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
			applyImpl(document, trigger, offset);
		}
		catch (BadLocationException ex) {
			logError("Unable to apply proposal", ex);
		}
	}

	/**
	 * Applies the proposal to the document.
	 * 
	 * @param document
	 * @param trigger
	 * @param offset
	 * @throws BadLocationException
	 */
	protected abstract void applyImpl(IDocument document, char trigger, int offset)
		throws BadLocationException;

	/**
	 * Creates the documentation/help text, either from a &lt;documentation&gt;
	 * element in a dialect XML help file, or if that isn't present, the
	 * Javadocs of that item if it's source code is available on a project.
	 * 
	 * @param dialectitem
	 * @return Documentation string.
	 */
	private static String generateDocumentation(DialectItem dialectitem) {

		// Documentation from <documentation> element
		if (dialectitem.isSetDocumentation()) {
			Documentation documentation = dialectitem.getDocumentation();

			StringBuilder doctext = new StringBuilder(documentation.getValue());

			// Generate 'see also' text
			if (documentation.isSetSeeAlso()) {
				doctext.append("<br/><dl><dt>See also:</dt><dd>");

				List<Object> seealsolist = documentation.getSeeAlso();
				for (int i = 0; i < seealsolist.size(); i++) {
					String seealso = ((DialectItem)seealsolist.get(i)).getName();
					if (!seealso.contains(".")) {
						doctext.append(dialectitem.getDialect().getPrefix() + ":");
					}
					doctext.append((i < seealsolist.size() - 1) ? seealso + ", " : seealso);
				}
				doctext.append("</dd>");
			}

			// Generate 'document reference' text
			if (documentation.isSetReference()) {
				doctext.append((documentation.isSetSeeAlso() ? "<dt>" : "<br/><dl><dt>") + "Reference:</dt><dd>");
				doctext.append(documentation.getReference()).append("</dd>");
			}

			if (documentation.isSetSeeAlso() || documentation.isSetReference()) {
				doctext.append("</dl>");
			}

			return doctext.toString();
		}

		return null;
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
