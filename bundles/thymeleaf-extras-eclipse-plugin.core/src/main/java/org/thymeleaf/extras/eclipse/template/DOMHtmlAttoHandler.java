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

import org.attoparser.markup.dom.INestableNode;
import org.attoparser.markup.dom.impl.Document;
import org.attoparser.markup.dom.impl.Element;
import org.attoparser.markup.html.AbstractStandardNonValidatingHtmlAttoHandler;
import org.attoparser.markup.html.HtmlParsing;
import org.attoparser.markup.html.HtmlParsingConfiguration;
import org.attoparser.markup.html.elements.IHtmlElement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Constructs a DOM of an HTML file as an AttoParser handler.
 * 
 * @author Emanuel Rabina
 */
public class DOMHtmlAttoHandler extends AbstractStandardNonValidatingHtmlAttoHandler {

	private final String documentname;
	private Document document;
	private Element currentelement;
	private Deque<INestableNode> nestednodestack = new ArrayDeque<INestableNode>();

	/**
	 * Constructor, create a handler for lenient HTML operations.
	 * 
	 * @param documentname
	 */
	public DOMHtmlAttoHandler(String documentname) {

		super(HtmlParsing.htmlParsingConfiguration());
		this.documentname = documentname;
	}

	/**
	 * Return the HTML document created from parsing an HTML file.
	 * 
	 * @return HTML document.
	 */
	public Document getDocument() {

		return document;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCDATASection(char[] buffer, int offset, int len, int line, int col) {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleComment(char[] buffer, int offset, int len, int line, int col) {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleDocType(String elementName, String publicId, String systemId,
		String internalSubset, int line, int col) {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col,
		HtmlParsingConfiguration parsingConfiguration) {

		document = (Document)nestednodestack.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleDocumentStart(long startTimeNanos, int line, int col,
		HtmlParsingConfiguration parsingConfiguration) {

		nestednodestack.push(new Document(documentname));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlCloseElement(final IHtmlElement htmlElement, final String elementName, 
		final int line, final int col) {

		Element element = (Element)nestednodestack.pop();
		currentelement = (Element)nestednodestack.peek();
		currentelement.addChild(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlOpenElement(IHtmlElement htmlElement, String elementName, 
		Map<String,String> attributes, int line, int col) {

		Element element = new Element(elementName);
		element.addAttributes(attributes);
		nestednodestack.push(element);

		currentelement = element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleHtmlStandaloneElement(IHtmlElement htmlElement, boolean minimized,
		String elementName, Map<String,String> attributes, int line, int col) {

		Element element = new Element(elementName);
		element.addAttributes(attributes);
		currentelement.addChild(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleProcessingInstruction(String target, String content, int line, int col) {

		// Does nothing - don't care about this one right now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleXmlDeclaration(String version, String encoding, String standalone,
		int line, int col) {

		// Does nothing - don't care about this one right now
	}
}
