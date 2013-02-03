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
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.thymeleaf.extras.eclipse.dialect.DialectLocator;
import org.xml.sax.InputSource;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Locates Thymeleaf dialect XML help files from a project's dependencies.
 * 
 * @author Emanuel Rabina
 */
public class ProjectDependencyDialectLocator implements DialectLocator<InputStream> {

	private static final String DIALECT_EXTRAS_NAMESPACE = "http://www.thymeleaf.org/extras/dialect";

	private final IJavaProject project;
	private final XPathExpression namespaceexpression;

	/**
	 * Constructor, sets which project will be scanned for Thymeleaf dialect
	 * help XML files.
	 * 
	 * @param project
	 */
	public ProjectDependencyDialectLocator(IJavaProject project) {

		this.project = project;
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			namespaceexpression = xpath.compile("namespace-uri(/*)");
		}
		catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns whether or not the given resource is a Thymeleaf dialect help XML
	 * file.
	 * 
	 * @param resource
	 * @return <tt>true</tt> if the resource is an XML file in the
	 * 		   <tt>http://www.thymeleaf.org/extras/dialect</tt> namespace.
	 */
	private boolean isDialectHelpXMLFile(IStorage resource) {

		InputStream resourcestream = null;
		try {
			// Check it's an XML file
			if (((resource instanceof IJarEntryResource && ((IJarEntryResource)resource).isFile()) ||
				resource instanceof IFile) && resource.getName().endsWith(".xml")) {

				// Check if the XML file namespace is correct
				resourcestream = resource.getContents();
				String namespace = namespaceexpression.evaluate(new InputSource(resourcestream));
				if (namespace.equals(DIALECT_EXTRAS_NAMESPACE)) {
					return true;
				}
			}
			return false;
		}
		catch (XPathExpressionException ex) {
			logError("Unable to execute XPath expression", ex);
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
			for (final IPackageFragment packagefragment: project.getPackageFragments()) {
				scannertasks.add(executorservice.submit(new Callable<IStorage>() {
					@Override
					public IStorage call() throws Exception {

						for (Object resource: packagefragment.getNonJavaResources()) {
							IStorage fileorjarentry = (IStorage)resource;
							if (isDialectHelpXMLFile(fileorjarentry)) {
								logInfo("Help file found: " + fileorjarentry.getName());
								return fileorjarentry;
							}
						}
						return null;
					}
				}));
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
}
