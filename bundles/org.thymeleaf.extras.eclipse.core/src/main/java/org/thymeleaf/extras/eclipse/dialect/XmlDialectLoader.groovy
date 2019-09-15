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

package org.thymeleaf.extras.eclipse.dialect

import nz.net.ultraq.jaxb.XmlReader
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.resources.ResourceLoader

/**
 * Loads dialect help/documentation XML files from those returned by a
 * {@link DialectLocator}.
 * 
 * @author Emanuel Rabina
 */
class XmlDialectLoader implements ResourceLoader<PathAndDialect, DialectLocator> {

	private static final XmlReader<Dialect> xmlReader = new XmlReader<>(Dialect)

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<PathAndDialect> load(DialectLocator locator) {

		return locator.locate().collect { pathAndStream ->
			return pathAndStream.stream.withStream { stream ->
				// Link processors and expression objects/methods with their dialect
				// TODO: Unnecessary with the XML slurper?
				def dialect = xmlReader.read(stream)
				dialect.dialectItems.each { dialectItem ->
					dialectItem.dialect = dialect
				}
				return new PathAndDialect(pathAndStream.path, dialect)
			}
		}
	}
}
