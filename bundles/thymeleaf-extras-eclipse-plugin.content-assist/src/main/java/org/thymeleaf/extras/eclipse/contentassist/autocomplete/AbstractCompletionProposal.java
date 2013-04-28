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
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeRestrictions;
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem;
import org.thymeleaf.extras.eclipse.dialect.xml.Documentation;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
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
	 * @param replacementstring Value to be entered into the document if this
	 * 							proposal is selected.
	 * @param cursorposition
	 */
	protected AbstractCompletionProposal(String replacementstring, int cursorposition) {

		this(null, replacementstring, cursorposition);
	}

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

		this.additionalproposalinfo = dialectitem != null ? generateDocumentation(dialectitem) : null;
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

		StringBuilder doctext = new StringBuilder();

		// Documentation from <documentation> element
		if (dialectitem.isSetDocumentation()) {
			Documentation documentation = dialectitem.getDocumentation();

			doctext.append(documentation.getValue());

			// Generate 'see also' text
			if (documentation.isSetSeeAlso()) {
				doctext.append("<br/><dl><dt>See also:</dt><dd>");
				List<Object> seealsolist = documentation.getSeeAlso();
				for (int i = 0; i < seealsolist.size(); i++) {
					String seealso = ((DialectItem)seealsolist.get(i)).getName();
					if (!seealso.contains(".")) {
						doctext.append(dialectitem.getDialect().getPrefix() + ":");
					}
					doctext.append(i < seealsolist.size() - 1 ? seealso + ", " : seealso);
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
		}

		// Generate 'restrictions' text
		if (dialectitem instanceof AttributeProcessor) {
			AttributeProcessor attributeprocessor = (AttributeProcessor)dialectitem;
			if (attributeprocessor.isSetRestrictions()) {
				AttributeRestrictions restrictions = ((AttributeProcessor)dialectitem).getRestrictions();
				if (restrictions.isSetTags() || restrictions.isSetAttributes() || restrictions.isSetValues()) {
					doctext.append("<dl>");
				}

				// Tags the processor can/can't appear in
				if (restrictions.isSetTags()) {
					doctext.append(generateDocumentationRestrictions(restrictions.getTags(),
							"Must appear in tag(s):", "Cannot appear in tag(s):"));
				}

				// Attributes the processor can/can't appear alongside
				if (restrictions.isSetAttributes()) {
					doctext.append(generateDocumentationRestrictions(restrictions.getAttributes(),
							"Must appear with attribute(s):", "Cannot appear with attribute(s):"));
				}

				// Values the processor is restricted to
				if (restrictions.isSetValues()) {
					doctext.append("<dt>Possible value(s):</dt><dd>");
					List<String> values = restrictions.getValues();
					for (int i = 0; i < values.size(); i++) {
						String value = values.get(i);
						doctext.append(i < values.size() - 1 ? value + ", " : value);
					}
					doctext.append("</dd>");
				}

				if (restrictions.isSetTags() || restrictions.isSetAttributes() || restrictions.isSetValues()) {
					doctext.append("</dl>");
				}
			}
		}

		return doctext.length() > 0 ? doctext.toString() : null;
	}

	/**
	 * Creates the help text around attribute restriction tags/attributes.
	 * 
	 * @param restrictions Space-separated list of tags/attributes that the
	 * 					   processor can/cannot appear alongside.
	 * @param yestext	   Text for when the restriction indicates the processor
	 * 					   must appear with that restriction.
	 * @param notext	   Text for when the restriction indicates the processor
	 * 					   must not appear with that restriction.
	 * @return <tt>StringBuilder</tt> containing the restriction text.
	 */
	private static StringBuilder generateDocumentationRestrictions(List<String> restrictions,
		String yestext, String notext) {

		StringBuilder doctext = new StringBuilder();

		ArrayList<String> yestags = new ArrayList<String>();
		ArrayList<String> notags  = new ArrayList<String>();
		for (String tag: restrictions) {
			if (tag.startsWith("-")) {
				notags.add(tag);
			}
			else {
				yestags.add(tag);
			}
		}
		if (!yestags.isEmpty()) {
			doctext.append("<dt>" + yestext + "</dt>");
			for (String yestag: yestags) {
				doctext.append("<dd>&lt;" + yestag + "&gt;</dd>");
			}
		}
		if (!notags.isEmpty()) {
			doctext.append("<dt>" + notext + "</dt>");
			for (String notag: notags) {
				doctext.append("<dd>&lt;" + notag.substring(1) + "&gt;</dd>");
			}
		}

		return doctext;
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

//		return contextinformation;
		return new ContextInformation("Context string", "Information string");
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
