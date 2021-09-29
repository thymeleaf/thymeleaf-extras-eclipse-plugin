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

import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ContextInformation
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem

/**
 * Common code for all processor and expression object completion proposals.
 * 
 * @author Emanuel Rabina
 */
abstract class AbstractCompletionProposal implements ICompletionProposal, ICompletionProposalExtension {

	protected final String replacementString
	protected final int cursorPosition

	final String additionalProposalInfo
	final IContextInformation contextInformation = new ContextInformation('Context string', 'Information string')
	final int contextInformationPosition = -1
	final Image image
	final char[] triggerCharacters = null

	/**
	 * Subclass constructor, set completion information.
	 * 
	 * @param replacementString
	 *   Value to be entered into the document if this proposal is selected.
	 * @param cursorPosition
	 * @param image
	 */
	protected AbstractCompletionProposal(String replacementString, int cursorPosition, Image image) {

		this(null, replacementString, cursorPosition, image)
	}

	/**
	 * Subclass constructor, set completion information.
	 * 
	 * @param dialectItem
	 * @param replacementString
	 *   Value to be entered into the document if this proposal is selected.
	 * @param cursorPosition
	 * @param image
	 */
	protected AbstractCompletionProposal(DialectItem dialectItem, String replacementString, int cursorPosition,
		Image image) {

		this.replacementString = replacementString
		this.cursorPosition = cursorPosition
		this.additionalProposalInfo = !dialectItem ? generateDocumentation(dialectItem) : null
		this.image = image
	}

	@Override
	void apply(IDocument document) {

		apply(document, '\0' as char, cursorPosition)
	}

	/**
	 * Creates the documentation/help text, either from a {@code <documentation>}
	 * element in a dialect XML help file, or if that isn't present, the
	 * Javadocs of that item if it's source code is available on a project.
	 * 
	 * @param dialectItem
	 * @return Documentation string.
	 */
	private static String generateDocumentation(DialectItem dialectItem) {

		def docText = new StringBuilder()

		// Documentation from <documentation> element
		if (dialectItem.isSetDocumentation()) {
			def documentation = dialectItem.documentation

			docText.append(documentation.value)

			// Generate 'see also' text
			if (documentation.isSetSeeAlso()) {
				docText.append('<br/><dl><dt>See also:</dt><dd>')
				def seeAlsoList = documentation.seeAlso
				seeAlsoList.eachWithIndex { seeAlso, index ->
					if (!seeAlso.contains('.') && !seeAlso.contains(':')) {
						docText.append(dialectItem.dialect.prefix + ':')
					}
					docText.append(index < seeAlsoList.size() - 1 ? seeAlso + ', ' : seeAlso)
				}
				docText.append('</dd>')
			}

			// Generate 'document reference' text
			if (documentation.isSetReference()) {
				docText.append((documentation.isSetSeeAlso() ? '<dt>' : '<br/><dl><dt>') + 'Reference:</dt><dd>')
				docText.append(documentation.reference).append('</dd>')
			}

			if (documentation.isSetSeeAlso() || documentation.isSetReference()) {
				docText.append('</dl>')
			}
		}

		// Generate 'restrictions' text
		if (dialectItem instanceof AttributeProcessor) {
			def attributeProcessor = (AttributeProcessor)dialectItem
			if (attributeProcessor.isSetRestrictions()) {
				def restrictions = ((AttributeProcessor)dialectItem).restrictions
				if (restrictions.isSetTags() || restrictions.isSetAttributes() || restrictions.isSetValues()) {
					docText.append('<dl>')
				}

				// Tags the processor can/can't appear in
				if (restrictions.isSetTags()) {
					docText.append(generateDocumentationRestrictions(restrictions.tags,
							'Must appear in tag(s):', 'Cannot appear in tag(s):'))
				}

				// Attributes the processor can/can't appear alongside
				if (restrictions.isSetAttributes()) {
					docText.append(generateDocumentationRestrictions(restrictions.attributes,
							'Must appear with attribute(s):', 'Cannot appear with attribute(s):'))
				}

				// Values the processor is restricted to
				if (restrictions.isSetValues()) {
					docText.append('<dt>Possible value(s):</dt><dd>')
					def values = restrictions.values
					values.eachWithIndex { value, index ->
						docText.append(index < values.size() - 1 ? value + ', ' : value)
					}
					docText.append('</dd>')
				}

				if (restrictions.isSetTags() || restrictions.isSetAttributes() || restrictions.isSetValues()) {
					docText.append('</dl>')
				}
			}
		}

		return docText.length() > 0 ? docText.toString() : null
	}

	/**
	 * Creates the help text around attribute restriction tags/attributes.
	 * 
	 * @param restrictions
	 *   Space-separated list of tags/attributes that the processor can/cannot
	 *   appear alongside.
	 * @param yesText
	 *   Text for when the restriction indicates the processor must appear with
	 *   that restriction.
	 * @param notext
	 *   Text for when the restriction indicates the processor must not appear
	 *   with that restriction.
	 * @return <tt>StringBuilder</tt> containing the restriction text.
	 */
	private static StringBuilder generateDocumentationRestrictions(List<String> restrictions, String yesText, String noText) {

		def docText = new StringBuilder()

		def yesTags = restrictions.findAll { tag -> !tag.startsWith('-') }
		def noTags  = restrictions.findAll { tag -> tag.startsWith('-') }

		if (!yesTags.empty) {
			docText.append("<dt>${yesText}</dt>")
			yesTags.each { yesTag ->
				docText.append("<dd>${yesTag}</dd>")
			}
		}
		if (!noTags.empty) {
			docText.append("<dt>${noText}</dt>")
			noTags.each { noTag ->
				docText.append("<dd>${noTag.substring(1)}</dd>")
			}
		}

		return docText
	}

	@Override
	boolean isValidFor(IDocument document, int offset) {

		// Use this proposal if the characters typed since it was suggested still
		// match the string this proposal will insert into the document
		return replacementString.startsWith(document.get(cursorPosition, offset - cursorPosition))
	}
}
