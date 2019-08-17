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

package org.thymeleaf.extras.eclipse.dialect;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Locates Thymeleaf dialect XML help files from a project's dependencies.
 * 
 * @author Emanuel Rabina
 */
public class ProjectDependencyDialectLocator implements DialectLocator<InputStream> {

	private static final String XML_FEATURE_LOAD_DTD_GRAMMAR =
			"http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
	private static final String XML_FEATURE_LOAD_EXTERNAL_DTD =
			"http://apache.org/xml/features/nonvalidating/load-external-dtd";

	private static final String DIALECT_EXTRAS_NAMESPACE = "http://www.thymeleaf.org/extras/dialect";

	private static final SAXParserFactory parserfactory;
	static {
		try {
			parserfactory = SAXParserFactory.newInstance();
			parserfactory.setNamespaceAware(true);
			parserfactory.setFeature(XML_FEATURE_LOAD_DTD_GRAMMAR, false);
			parserfactory.setFeature(XML_FEATURE_LOAD_EXTERNAL_DTD, false);
		}
		catch (SAXException ex) {
			throw new RuntimeException(ex);
		}
		catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private final IJavaProject project;
	private final ArrayList<IPath> dialectfilepaths = new ArrayList<IPath>();

	/**
	 * Constructor, sets which project will be scanned for Thymeleaf dialect
	 * help XML files.
	 * 
	 * @param project
	 */
	public ProjectDependencyDialectLocator(IJavaProject project) {

		this.project = project;
	}

	/**
	 * Return a list of the dialect file paths that were encountered during a
	 * run of {@link #locateDialects}.  The order of the paths matches the order
	 * of the dialects returned by <tt>locateDialects()</tt>.
	 * 
	 * @return List of dialect file paths.
	 */
	public List<IPath> getDialectFilePaths() {

		return dialectfilepaths;
	}

	/**
	 * Returns whether or not the given resource is a Thymeleaf dialect help XML
	 * file.
	 * 
	 * @param resource
	 * @return <tt>true</tt> if the resource is an XML file in the
	 * 		   <tt>http://www.thymeleaf.org/extras/dialect</tt> namespace.
	 */
	private static boolean isDialectHelpXMLFile(IStorage resource) {

		InputStream resourcestream = null;
		try {
			// Check it's an XML file
			if (((resource instanceof IJarEntryResource && ((IJarEntryResource)resource).isFile()) ||
				resource instanceof IFile) && resource.getName().endsWith(".xml")) {

				// Check if the XML file namespace is correct
				resourcestream = resource.getContents();
				SAXParser parser = parserfactory.newSAXParser();
				NamespaceHandler handler = new NamespaceHandler();
				parser.parse(resourcestream, handler);
				if (handler.namespace != null && handler.namespace.equals(DIALECT_EXTRAS_NAMESPACE)) {
					return true;
				}
			}
			return false;
		}
		catch (ParserConfigurationException ex) {
			logError("Unable to read XML file", ex);
			return false;
		}
		catch (IOException ex) {
			logError("Unable to read XML file", ex);
			return false;
		}
		catch (SAXException ex) {
			logError("Unable to read XML file", ex);
			return false;
		}
		catch (CoreException ex) {
			logError("Unable to open an input stream over resource", ex);
			return false;
		}
		finally {
			if (resourcestream != null) {
				try {
					resourcestream.close();
				}
				catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InputStream> locateDialects() {

		logInfo("Scanning for dialect help files on project dependencies");
		long start = System.currentTimeMillis();

		ExecutorService executorservice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		final ArrayList<InputStream> dialectstreams = new ArrayList<InputStream>();

		try {
			// Multi-threaded search for dialect files - there are a lot of package
			// fragments to get through, and the I/O namespace check is a blocker.
			ArrayList<Future<IStorage>> scannertasks = new ArrayList<Future<IStorage>>();

			for (IPackageFragmentRoot packagefragmentroot: project.getAllPackageFragmentRoots()) {
				for (IJavaElement child: packagefragmentroot.getChildren()) {
					final IPackageFragment packagefragment = (IPackageFragment)child;

					scannertasks.add(executorservice.submit(new Callable<IStorage>() {
						@Override
						public IStorage call() throws Exception {

							for (Object resource: packagefragment.getNonJavaResources()) {
								IStorage fileorjarentry = (IStorage)resource;
								if (isDialectHelpXMLFile(fileorjarentry)) {
									logInfo("Help file found: " + fileorjarentry.getName());
									dialectfilepaths.add(fileorjarentry.getFullPath());
									return fileorjarentry;
								}
							}
							return null;
						}
					}));
				}
			}

			// Collate scanner results
			for (Future<IStorage> scannertask: scannertasks) {
				try {
					IStorage fileorjarentry = scannertask.get();
					if (fileorjarentry != null) {
						dialectstreams.add(fileorjarentry.getContents());
					}
				}
				catch (ExecutionException ex) {
					logError("Unable to execute scanning task", ex);
				}
				catch (InterruptedException ex) {
					logError("Unable to execute scanning task", ex);
				}
			}
		}
		catch (CoreException ex) {
			// If we get here, the project cannot be read.  Return the empty list.
			logError("Project " + project.getProject().getName() + " could not be read", ex);
		}
		finally {
			executorservice.shutdown();
			try {
				if (!executorservice.awaitTermination(5, TimeUnit.SECONDS)) {
					executorservice.shutdownNow();
				}
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}

		logInfo("Scanning complete.  Execution time: " + (System.currentTimeMillis() - start) + "ms");
		return dialectstreams;
	}

	/**
	 * Basic SAX handler that cares only about the document namespace.
	 */
	private static class NamespaceHandler extends DefaultHandler {

		private String namespace;

		/**
		 * Saves the document namespace, then does nothing after that.
		 * 
		 * @param uri
		 * @param localName
		 * @param qName
		 * @param attributes
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {

			if (namespace == null) {
				namespace = uri;
			}
		}
	}
}
