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

package org.thymeleaf.extras.eclipse.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.thymeleaf.extras.eclipse.dialect.DialectLocator;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Locates Thymeleaf dialect help XML files from a project's dependencies.
 * 
 * @author Emanuel Rabina
 */
public class ProjectDependencyDialectLocator implements DialectLocator {

	private static final String JAVA_PROJECT_NATURE = "org.eclipse.jdt.core.javanature";
	private static final String WEB_PROJECT_NATURE  = "org.eclipse.wst.common.project.facet.core.nature";

	private static final String DIALECT_EXTRAS_NAMESPACE = "http://www.thymeleaf.org/extras/dialect";

	private static final XPathExpression namespaceexpression;
	static {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			namespaceexpression = xpath.compile("namespace-uri(/*)");
		}
		catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
	}

	private final IProject project;

	/**
	 * Constructor, sets which project will be scanned for Thymeleaf dialect
	 * help XML files.
	 * 
	 * @param project
	 */
	public ProjectDependencyDialectLocator(IProject project) {

		this.project = project;
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
				String namespace = namespaceexpression.evaluate(new InputSource(resourcestream));
				if (namespace.equals(DIALECT_EXTRAS_NAMESPACE)) {
					return true;
				}
			}
			return false;
		}
		catch (XPathExpressionException ex) {
			ex.printStackTrace();
			return false;
		}
		catch (CoreException ex) {
			ex.printStackTrace();
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

		ContentAssistPlugin.logInfo("Scanning for dialect help files on project dependencies...");

		long start = System.currentTimeMillis();
		ArrayList<InputStream> dialectstreams = new ArrayList<InputStream>();

		try {
			// Proceed only if this is a Java web project
			if (project.isNatureEnabled(JAVA_PROJECT_NATURE) &&
				project.isNatureEnabled(WEB_PROJECT_NATURE)) {

				IJavaProject javaproject = JavaCore.create(project);
				IPackageFragment[] packagefragments = javaproject.getPackageFragments();

				// NOTE: This loop could be super slow since I introduced an IO element
				//       to the XML file checking.  Might need to run it in parallel.
				for (IPackageFragment packagefragment: packagefragments) {
					for (Object resource: packagefragment.getNonJavaResources()) {
						IStorage fileorjarentry = (IStorage)resource;
						if (isDialectHelpXMLFile(fileorjarentry)) {
							ContentAssistPlugin.logInfo("Help file found: " + fileorjarentry.getName());
							dialectstreams.add((fileorjarentry).getContents());
						}
					}
				}
			}
		}
		catch (CoreException ex) {
			// If we get here, the project cannot be read.  Return the empty list.
			ex.printStackTrace();
		}

		ContentAssistPlugin.logInfo("Scanning time: " + (System.currentTimeMillis() - start) + "ms");
		return dialectstreams;
	}
}
