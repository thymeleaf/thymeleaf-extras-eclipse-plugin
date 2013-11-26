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
import org.attoparser.markup.MarkupAttoParser;
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

	private static final MarkupAttoParser parser = new MarkupAttoParser();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Template> loadResources(ProjectTemplateLocator locator) {

		ArrayList<Template> templates = new ArrayList<Template>();
		for (IFile file: locator.locateResources()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(file.getContents()));
				DOMHtmlAttoHandler handler = new DOMHtmlAttoHandler(file.getName());
				parser.parse(reader, handler);
				templates.add(new Template(handler.getDocument()));
			}
			catch (CoreException ex) {
				logError("File " + file.getName() + " could not be read", ex);
			}
			catch (AttoParseException ex) {
				logError("Error reading the template file", ex);
			}
			finally {
				try {
					if (reader != null) {
						reader.close();
					}
				}
				catch (IOException ex) {
					logError("Unable to close the template file input stream", ex);
				}
			}
		}

		return templates;
	}
}
