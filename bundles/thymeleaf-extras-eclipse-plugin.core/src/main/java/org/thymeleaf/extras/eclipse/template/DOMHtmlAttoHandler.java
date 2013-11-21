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

package org.thymeleaf.extras.eclipse.template;

import org.attoparser.AttoParseException;
import org.attoparser.markup.dom.IDocument;
import org.attoparser.markup.html.AbstractStandardNonValidatingHtmlAttoHandler;
import org.attoparser.markup.html.HtmlParsing;
import org.attoparser.markup.html.elements.IHtmlElement;

import java.util.Map;

/**
 * Constructs a DOM of an HTML file as an AttoParser handler.
 * 
 * @author Emanuel Rabina
 */
public class DOMHtmlAttoHandler extends AbstractStandardNonValidatingHtmlAttoHandler {

	private IDocument document;

	/**
	 * Constructor, create a handler for lenient HTML operations.
	 */
	public DOMHtmlAttoHandler() {

		super(HtmlParsing.htmlParsingConfiguration());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCDATASection(final char[] buffer, final int offset, final int len, 
		final int line, final int col) throws AttoParseException {


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleComment(final char[] buffer, final int offset, final int len, 
		final int line, final int col) throws AttoParseException {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleDocType(final String elementName, final String publicId,
		final String systemId, final String internalSubset, final int line, final int col)
		throws AttoParseException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlCloseElement(final IHtmlElement element, final String elementName, 
		final int line, final int col) throws AttoParseException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlOpenElement(final IHtmlElement element, final String elementName, 
		final Map<String,String> attributes, final int line, final int col) throws AttoParseException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlStandaloneElement(final IHtmlElement element, final boolean minimized,
		final String elementName, final Map<String,String> attributes, final int line, final int col)
		throws AttoParseException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleProcessingInstruction(final String target, final String content, 
		final int line, final int col) throws AttoParseException {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleXmlDeclaration(final String version, final String encoding, 
		final String standalone, final int line, final int col) throws AttoParseException {

		// Does nothing - don't care about this one right now
	}
}
