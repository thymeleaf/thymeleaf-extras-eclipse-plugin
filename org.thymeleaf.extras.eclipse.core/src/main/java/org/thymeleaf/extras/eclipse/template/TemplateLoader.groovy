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

package org.thymeleaf.extras.eclipse.template

import javax.inject.Named
import org.attoparser.ParseException
import org.attoparser.config.ParseConfiguration
import org.attoparser.dom.DOMMarkupParser
import org.attoparser.dom.Document
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.CoreException
import org.thymeleaf.extras.eclipse.resources.ResourceLoader
import org.thymeleaf.extras.eclipse.template.model.Template

/**
 * Creates template metadata for any templates picked out by a template locator.
 * 
 * @author Emanuel Rabina
 */
@Named
class TemplateLoader implements ResourceLoader<Template, ProjectTemplateLocator> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<Template> load(ProjectTemplateLocator locator) {

		return locator.locate().collect { file ->
			return new BufferedReader(new InputStreamReader(file.contents)).withReader { reader ->
				def parser = new DOMMarkupParser(ParseConfiguration.htmlConfiguration())
				def document = parser.parse(file.name, reader)
				return new Template(file.fullPath, document)
			}
		}
	}
}
