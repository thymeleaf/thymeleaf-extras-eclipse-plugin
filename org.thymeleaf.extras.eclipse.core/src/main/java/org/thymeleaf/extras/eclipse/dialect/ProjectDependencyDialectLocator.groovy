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

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IStorage
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJarEntryResource
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import groovy.transform.TupleConstructor
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

/**
 * Locates Thymeleaf dialect XML help files from a project's dependencies.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ProjectDependencyDialectLocator implements DialectLocator {

	private static final String XML_FEATURE_LOAD_DTD_GRAMMAR =
			"http://apache.org/xml/features/nonvalidating/load-dtd-grammar"
	private static final String XML_FEATURE_LOAD_EXTERNAL_DTD =
			"http://apache.org/xml/features/nonvalidating/load-external-dtd"

	private static final String DIALECT_EXTRAS_NAMESPACE = "http://www.thymeleaf.org/extras/dialect"

	private static final Logger logger = LoggerFactory.getLogger(ProjectDependencyDialectLocator)

	private static final SAXParserFactory saxParserFactory
	static {
		saxParserFactory = SAXParserFactory.newInstance()
		saxParserFactory.setNamespaceAware(true)
		saxParserFactory.setFeature(XML_FEATURE_LOAD_DTD_GRAMMAR, false)
		saxParserFactory.setFeature(XML_FEATURE_LOAD_EXTERNAL_DTD, false)
	}

	final IJavaProject project

	/**
	 * Returns whether or not the given resource is a Thymeleaf dialect help XML
	 * file.
	 * 
	 * @param resource
	 * @return <tt>true</tt> if the resource is an XML file in the
	 * 		   <tt>http://www.thymeleaf.org/extras/dialect</tt> namespace.
	 */
	private static boolean isDialectHelpXmlFile(IStorage resource) {

		if (((resource instanceof IJarEntryResource && resource.file) ||
			resource instanceof IFile) && resource.name.endsWith('.xml')) {
			return resource.contents.withStream { resourceStream ->
				def saxParser = saxParserFactory.newSAXParser()
				def handler = new NamespaceHandler()
				saxParser.parse(resourceStream, handler)
				return handler.namespace == DIALECT_EXTRAS_NAMESPACE
			}
		}
		return false
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<PathAndStream> locate() {

		logger.info('Scanning for dialect help files on project dependencies')

		return time('Scanning for dialects') { ->

			// Multi-threaded search for dialect files - there are a lot of package
			// fragments to get through, and the I/O namespace check is a blocker.
			def executorService = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors())
			return executorService.executeAndShutdown { ExecutorService executor ->

				def scannerTasks = new ArrayList<Future<IStorage>>()
				project.allPackageFragmentRoots.each { packageFragmentRoot ->
					packageFragmentRoot.children.each { packageFragment ->
						scannerTasks << executor.submit({ ->
							return packageFragment.nonJavaResources.findResult { fileOrJarEntry ->
								if (isDialectHelpXmlFile(fileOrJarEntry)) {
									logger.info("Help file found: ${fileOrJarEntry.name}")
									return fileOrJarEntry
								}
								return null
							}
						} as Callable)
					}
				}
				return scannerTasks.inject([]) { acc, scannerTask ->
					def dialectHelpXmlFile = scannerTask.get()
					if (dialectHelpXmlFile) {
						acc << new PathAndStream(dialectHelpXmlFile.fullPath, dialectHelpXmlFile.contents)
					}
					return acc
				}
			}
		}
	}

	/**
	 * Basic SAX handler that cares only about the document namespace.
	 */
	private static class NamespaceHandler extends DefaultHandler {

		private String namespace

		/**
		 * Saves the document namespace, then does nothing after that.
		 * 
		 * @param uri
		 * @param localName
		 * @param qName
		 * @param attributes
		 */
		@Override
		void startElement(String uri, String localName, String qName, Attributes attributes) {

			if (namespace == null) {
				namespace = uri
			}
		}
	}
}
