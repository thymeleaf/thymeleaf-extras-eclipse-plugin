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

import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.dom.DOMMarkupParser;
import org.attoparser.dom.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.thymeleaf.extras.eclipse.scanner.ResourceLoader;
import org.thymeleaf.extras.eclipse.template.model.Template;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates template metadata for any templates picked out by a template locator.
 * 
 * @author Emanuel Rabina
 */
public class TemplateLoader implements ResourceLoader<IFile, ProjectTemplateLocator, Template> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Template> loadResources(ProjectTemplateLocator locator) {

		ArrayList<Template> templates = new ArrayList<Template>();
		for (IFile file: locator.locateResources()) {
			String fileName = file.getName();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()))) {
				DOMMarkupParser parser = new DOMMarkupParser(ParseConfiguration.htmlConfiguration());
				Document document = parser.parse(fileName, reader);
				templates.add(new Template(document));
			}
			catch (CoreException | IOException | ParseException ex) {
				logError("An error occured while reading " + fileName, ex);
			}
		}

		return templates;
	}
}
