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
import org.attoparser.IAttoHandler;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.html.AbstractDetailedNonValidatingHtmlAttoHandler;
import org.attoparser.markup.html.HtmlParsing;
import org.thymeleaf.extras.eclipse.scanner.ResourceLoader;
import org.thymeleaf.extras.eclipse.template.model.Template;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates template metadata for any templates picked out by a template locator.
 * 
 * @author Emanuel Rabina
 */
public class TemplateLoader implements ResourceLoader<ProjectTemplateLocator, Template> {

	private static final IAttoParser parser = new MarkupAttoParser();
	private static final IAttoHandler fragmenthandler = new ThymeleafFragmentHandler();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Template> loadResources(ProjectTemplateLocator locator) {

		ArrayList<Template> templates = new ArrayList<Template>();
		for (InputStream templatefilestream: locator.locateResources()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(templatefilestream));
			try {
				parser.parse(reader, fragmenthandler);
			}
			catch (AttoParseException ex) {
				logError("Error reading the template file", ex);
			}
			finally {
				try {
					reader.close();
				}
				catch (IOException ex) {
					logError("Unable to close the template file input stream", ex);
				}
			}
		}

		return templates;
	}

	/**
	 * An HTML handler for picking out Thymeleaf fragments.
	 */
	private static class ThymeleafFragmentHandler extends AbstractDetailedNonValidatingHtmlAttoHandler {

		private static final String FRAGMENT_PROCESSOR      = "th:fragment";
		private static final String FRAGMENT_PROCESSOR_DATA = "data-th-fragment";

		private final ArrayList<String> fragmentsignatures = new ArrayList<String>();

		/**
		 * Constructor, create a handler for lenient HTML operations.
		 */
		private ThymeleafFragmentHandler() {

			super(HtmlParsing.htmlParsingConfiguration());
		}

		/**
		 * Handle an HTML attribute.  If the attribute is for the Thymeleaf
		 * <tt>th:fragment</tt> attribute processor, store the value of the
		 * fragment so a signature can be constructed from it later.
		 * 
		 * @param buffer
		 * @param nameOffset
		 * @param nameLen
		 * @param nameLine
		 * @param nameCol
		 * @param operatorOffset
		 * @param operatorLen
		 * @param operatorLine
		 * @param operatorCol
		 * @param valueContentOffset
		 * @param valueContentLen
		 * @param valueOuterOffset
		 * @param valueOuterLen
		 * @param valueLine
		 * @param valueCol
		 */
		@Override
		public void handleHtmlAttribute(char[] buffer, int nameOffset, int nameLen, int nameLine,
			int nameCol, int operatorOffset, int operatorLen, int operatorLine, int operatorCol,
			int valueContentOffset, int valueContentLen, int valueOuterOffset, int valueOuterLen,
			int valueLine, int valueCol) {

			String attributename = new String(buffer, nameOffset, nameLen);
			if (attributename.equals(FRAGMENT_PROCESSOR) ||
				attributename.equals(FRAGMENT_PROCESSOR_DATA)) {
				fragmentsignatures.add(new String(buffer, valueContentOffset, valueContentLen));
			}
		}
	}
}
