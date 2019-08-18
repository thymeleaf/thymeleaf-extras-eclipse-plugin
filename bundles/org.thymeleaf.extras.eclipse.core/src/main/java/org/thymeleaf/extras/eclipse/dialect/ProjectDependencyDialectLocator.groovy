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
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import static org.thymeleaf.extras.eclipse.CorePlugin.*

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
class ProjectDependencyDialectLocator implements DialectLocator<InputStream> {

	private static final String XML_FEATURE_LOAD_DTD_GRAMMAR =
			"http://apache.org/xml/features/nonvalidating/load-dtd-grammar"
	private static final String XML_FEATURE_LOAD_EXTERNAL_DTD =
			"http://apache.org/xml/features/nonvalidating/load-external-dtd"

	private static final String DIALECT_EXTRAS_NAMESPACE = "http://www.thymeleaf.org/extras/dialect"

	private static final SAXParser saxParser
	static {
		def parserFactory = SAXParserFactory.newInstance()
		parserFactory.setNamespaceAware(true)
		parserFactory.setFeature(XML_FEATURE_LOAD_DTD_GRAMMAR, false)
		parserFactory.setFeature(XML_FEATURE_LOAD_EXTERNAL_DTD, false)
		saxParser = parserFactory.newSAXParser()
	}

	final IJavaProject project
	final ArrayList<IPath> dialectFilePaths = new ArrayList<IPath>()

	/**
	 * Constructor, sets which project will be scanned for Thymeleaf dialect
	 * help XML files.
	 * 
	 * @param project
	 */
	ProjectDependencyDialectLocator(IJavaProject project) {

		this.project = project
	}

	/**
	 * Returns whether or not the given resource is a Thymeleaf dialect help XML
	 * file.
	 * 
	 * @param resource
	 * @return <tt>true</tt> if the resource is an XML file in the
	 * 		   <tt>http://www.thymeleaf.org/extras/dialect</tt> namespace.
	 */
	private static boolean isDialectHelpXmlFile(IStorage resource) {

		try {
			if (((resource instanceof IJarEntryResource && resource.isFile()) ||
				resource instanceof IFile) && resource.name.endsWith('.xml')) {
				resource.contents.withStream { resourceStream ->
					def handler = new NamespaceHandler()
					saxParser.parse(resourceStream, handler)
					return handler.namespace == DIALECT_EXTRAS_NAMESPACE
				}
			}
		}
		catch (Exception ex) {
			logError("Unable to read XML file", ex)
		}
		return false
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<InputStream> locateDialects() {

		logInfo('Scanning for dialect help files on project dependencies')
		long start = System.currentTimeMillis()

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
		final ArrayList<InputStream> dialectStreams = new ArrayList<InputStream>()

		try {
			// Multi-threaded search for dialect files - there are a lot of package
			// fragments to get through, and the I/O namespace check is a blocker.
			ArrayList<Future<IStorage>> scannerTasks = new ArrayList<Future<IStorage>>()

			for (IPackageFragmentRoot packageFragmentRoot: project.getAllPackageFragmentRoots()) {
				for (IJavaElement child: packageFragmentRoot.getChildren()) {
					final IPackageFragment packageFragment = (IPackageFragment)child

					scannerTasks.add(executorService.submit(new Callable<IStorage>() {
						@Override
						IStorage call() throws Exception {

							for (Object resource: packageFragment.getNonJavaResources()) {
								IStorage fileOrJarEntry = (IStorage)resource
								if (isDialectHelpXmlFile(fileOrJarEntry)) {
									logInfo("Help file found: ${fileOrJarEntry.name}")
									dialectFilePaths.add(fileOrJarEntry.fullPath)
									return fileOrJarEntry
								}
							}
							return null
						}
					}))
				}
			}

			// Collate scanner results
			for (Future<IStorage> scannertask: scannerTasks) {
				try {
					IStorage fileorjarentry = scannertask.get()
					if (fileorjarentry != null) {
						dialectStreams.add(fileorjarentry.getContents())
					}
				}
				catch (ExecutionException ex) {
					logError("Unable to execute scanning task", ex)
				}
				catch (InterruptedException ex) {
					logError("Unable to execute scanning task", ex)
				}
			}
		}
		catch (CoreException ex) {
			// If we get here, the project cannot be read.  Return the empty list.
			logError("Project ${project.project.name} could not be read", ex)
		}
		finally {
			executorService.shutdown()
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow()
			}
		}

		logInfo("Scanning complete.  Execution time: ${System.currentTimeMillis() - start}ms")
		return dialectStreams
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
