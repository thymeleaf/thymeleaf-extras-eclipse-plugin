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

package org.thymeleaf.extras.eclipse.dialect.cache;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.thymeleaf.extras.eclipse.dialect.BundledDialectLocator;
import org.thymeleaf.extras.eclipse.dialect.ProjectDependencyDialectLocator;
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import org.thymeleaf.extras.eclipse.nature.ThymeleafNature;
import static org.eclipse.core.resources.IResourceChangeEvent.*;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * A basic in-memory store of all known Thymeleaf dialects and their processors
 * and expression object methods.
 * 
 * @author Emanuel Rabina
 */
public class DialectCache {

	private static XmlDialectLoader xmldialectloader = new XmlDialectLoader();
	private static DialectItemProcessor dialectitemprocessor = new DialectItemProcessor();

	// Tree structure of all dialects in the user's workspace
	private static DialectTree dialecttree;

	// Resource listener for changes to dialect projects and files
	private static DialectChangeListener dialectchangelistener;

	/**
	 * Checks if the dialect is in the list of given namespaces.
	 * 
	 * @param dialect
	 * @param namespaces
	 * @return <tt>true</tt> if the dialect prefix (and namespace if the dialect
	 * 		   is namespace strict) are listed in the <tt>namespaces</tt>
	 * 		   collection.
	 */
	private static boolean dialectInNamespace(Dialect dialect, List<QName> namespaces) {

		for (QName namespace: namespaces) {
			if (dialect.getPrefix().equals(namespace.getPrefix())) {
				if (!dialect.isNamespaceStrict()) {
					return true;
				}
				else if (dialect.getNamespaceUri().equals(namespace.getNamespaceURI())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the expression object method matches the given name.
	 * 
	 * @param method
	 * @param name
	 * @return <tt>true</tt> if the name matches the expression object name.
	 */
	private static boolean expressionObjectMethodMatchesName(ExpressionObjectMethod method, String name) {

		if (name == null || name.isEmpty()) {
			return false;
		}
		return name.equals(method.getFullName());
	}

	/**
	 * Checks if the expression object method name matches the given
	 * start-of-string pattern.
	 * 
	 * @param method
	 * @param pattern
	 * @return <tt>true</tt> if the pattern matches against the expression
	 * 		   object name.
	 */
	private static boolean expressionObjectMethodMatchesPattern(ExpressionObjectMethod method, String pattern) {

		return pattern != null && method.getFullName().startsWith(pattern);
	}

	/**
	 * Retrieve all attribute processors for the given project, whose names
	 * match the starting pattern.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param pattern	 Start-of-string pattern to match.
	 * @return List of all matching attribute processors.
	 */
	public static List<AttributeProcessor> getAttributeProcessors(IJavaProject project,
		List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project);

		ArrayList<AttributeProcessor> matchedprocessors = new ArrayList<AttributeProcessor>();
		for (AttributeProcessor processor: dialecttree.getAttributeProcessorsForProject(project)) {
			Dialect dialect = processor.getDialect();
			if ((thymeleafNatureEnabled(project) || dialectInNamespace(dialect, namespaces)) &&
				processorMatchesPattern(processor, pattern)) {
				matchedprocessors.add(processor);
			}
		}
		return matchedprocessors;
	}

	/**
	 * Retrieve all element processors for the given project, whose names match
	 * the starting pattern.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param pattern	 Start-of-string pattern to match.
	 * @return List of all matching element processors
	 */
	public static List<ElementProcessor> getElementProcessors(IJavaProject project,
		List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project);

		ArrayList<ElementProcessor> matchedprocessors = new ArrayList<ElementProcessor>();
		for (ElementProcessor processor: dialecttree.getElementProcessorsForProject(project)) {
			Dialect dialect = processor.getDialect();
			if ((thymeleafNatureEnabled(project) || dialectInNamespace(dialect, namespaces)) &&
				processorMatchesPattern(processor, pattern)) {
				matchedprocessors.add(processor);
			}
		}
		return matchedprocessors;
	}

	/**
	 * Retrieve the expression object method with the full matching name.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param methodname Full name of the expression object method.
	 * @return Expression object with the given name, or <tt>null</tt> if no
	 * 		   expression object matches.
	 */
	public static ExpressionObjectMethod getExpressionObjectMethod(IJavaProject project,
		List<QName> namespaces, String methodname) {

		loadDialectsFromProject(project);

		for (ExpressionObjectMethod expressionobject: dialecttree.getExpressionObjectMethodsForProject(project)) {
			if ((thymeleafNatureEnabled(project) || dialectInNamespace(expressionobject.getDialect(), namespaces)) &&
				expressionObjectMethodMatchesName(expressionobject, methodname)) {
				return expressionobject;
			}
		}
		return null;
	}

	/**
	 * Retrieve all expression object methods for the given project, whose names
	 * match the starting pattern.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param pattern	 Start-of-string pattern to match.
	 * @return List of all matching expression object methods.
	 */
	public static List<ExpressionObjectMethod> getExpressionObjectMethods(IJavaProject project,
		List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project);

		ArrayList<ExpressionObjectMethod> matchedexpressionobjects = new ArrayList<ExpressionObjectMethod>();
		for (ExpressionObjectMethod expressionobjectmethod: dialecttree.getExpressionObjectMethodsForProject(project)) {
			Dialect dialect = expressionobjectmethod.getDialect();
			if ((thymeleafNatureEnabled(project) || dialectInNamespace(dialect, namespaces)) &&
				expressionObjectMethodMatchesPattern(expressionobjectmethod, pattern)) {
				matchedexpressionobjects.add(expressionobjectmethod);
			}
		}
		return matchedexpressionobjects;
	}

	/**
	 * Retrieve the processor with the full matching name.
	 * 
	 * @param project		The current project.
	 * @param namespaces	List of namespaces available at the current point in
	 * 						the document.
	 * @param processorname	Full name of the processor.
	 * @return Processor for the given prefix and name, or <tt>null</tt> if no
	 * 		   processor matches.
	 */
	public static Processor getProcessor(IJavaProject project, List<QName> namespaces, String processorname) {

		loadDialectsFromProject(project);

		ArrayList<Processor> processors = new ArrayList<Processor>();
		processors.addAll(dialecttree.getAttributeProcessorsForProject(project));
		processors.addAll(dialecttree.getElementProcessorsForProject(project));

		for (Processor processor: processors) {
			if ((thymeleafNatureEnabled(project) || dialectInNamespace(processor.getDialect(), namespaces)) &&
				processorMatchesName(processor, processorname)) {
				return processor;
			}
		}
		return null;
	}

	/**
	 * Initialize the cache with the Thymeleaf dialects bundled with this
	 * plugin.
	 */
	public static void initialize() {

		dialecttree = new DialectTree();

		logInfo("Loading bundled dialect files");
		List<Dialect> dialects = xmldialectloader.loadDialects(new BundledDialectLocator());
		for (Dialect dialect: dialects) {
			dialecttree.addBundledDialect(dialect, dialectitemprocessor.processDialectItems(
					dialect, findCurrentJavaProject()));
		}

		dialectchangelistener = new DialectChangeListener(xmldialectloader, dialectitemprocessor, dialecttree);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(dialectchangelistener,
				POST_CHANGE | PRE_CLOSE | PRE_DELETE);
	}

	/**
	 * Gather all dialect information from the given project, if we haven't got
	 * information on that project in the first place.
	 * 
	 * @param project Project to scan for dialect information.
	 */
	private static void loadDialectsFromProject(IJavaProject project) {

		if (!dialecttree.containsProject(project)) {
			ProjectDependencyDialectLocator projectdialectlocator = new ProjectDependencyDialectLocator(project);
			List<Dialect> dialects = xmldialectloader.loadDialects(projectdialectlocator);
			List<IPath> dialectfilepaths = projectdialectlocator.getDialectFilePaths();

			for (int i = 0; i < dialects.size(); i++) {
				Dialect dialect = dialects.get(i);
				IPath dialectfilepath = dialectfilepaths.get(i);

				dialecttree.addProjectDialect(project, dialectfilepath,
						dialectitemprocessor.processDialectItems(dialect, project));
				dialectchangelistener.trackDialectFileForChanges(dialectfilepath);
			}
		}
	}

	/**
	 * Checks if the processor name (prefix:name) matches the given name.
	 * 
	 * @param processor
	 * @param name
	 * @return <tt>true</tt> if the name matches the full processor name.
	 */
	private static boolean processorMatchesName(Processor processor, String name) {

		if (name == null || name.isEmpty()) {
			return false;
		}
		int separatorindex = name.indexOf(':');
		return separatorindex != -1 &&
				processor.getDialect().getPrefix().equals(name.substring(0, separatorindex)) &&
				processor.getName().equals(name.substring(separatorindex + 1));
	}

	/**
	 * Checks if the processor name (prefix:name) matches the given
	 * start-of-string pattern.
	 * 
	 * @param processor
	 * @param pattern
	 * @return <tt>true</tt> if the pattern matches against the processor prefix
	 * 		   and name.
	 */
	private static boolean processorMatchesPattern(Processor processor, String pattern) {

		return pattern != null && processor.getFullName().startsWith(pattern);
	}

	/**
	 * Shutdown method of the cache, cleans up any processes that need
	 * cleaning-up.
	 */
	public static void shutdown() {

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(dialectchangelistener);
		dialectchangelistener.shutdown();
	}

	/**
	 * Check if the Thymeleaf nature has been applied to the given project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has the Thymeleaf nature.
	 */
	private static boolean thymeleafNatureEnabled(IJavaProject project) {

		return ThymeleafNature.thymeleafNatureEnabled(project.getProject());
	}
}
