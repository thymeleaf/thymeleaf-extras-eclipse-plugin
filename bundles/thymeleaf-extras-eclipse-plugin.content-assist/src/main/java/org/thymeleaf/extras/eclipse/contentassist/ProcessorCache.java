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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.thymeleaf.extras.eclipse.dialect.BundledDialectLocator;
import org.thymeleaf.extras.eclipse.dialect.DialectLoader;
import org.thymeleaf.extras.eclipse.dialect.ProjectDependencyDialectLocator;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import org.thymeleaf.extras.eclipse.dialect.xml.UtilityMethod;
import org.thymeleaf.extras.eclipse.dialect.xml.UtilityObjectReference;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/**
 * A basic in-memory store of all the Thymeleaf processors.
 * 
 * @author Emanuel Rabina
 */
public class ProcessorCache {

	private static final DialectLoader dialectloader = new DialectLoader();

	// List of bundled dialects
	private static final ArrayList<Dialect> bundleddialects = new ArrayList<Dialect>();

	// Mapping of projects that contain certain dialects
	private static final HashMap<IJavaProject,List<Dialect>> projectdialects =
			new HashMap<IJavaProject,List<Dialect>>();

	// Collection of processors in alphabetical order
	private static final TreeSet<Processor> processors = new TreeSet<Processor>(new Comparator<Processor>() {
		@Override
		public int compare(Processor p1, Processor p2) {
			Dialect d1 = p1.getDialect();
			Dialect d2 = p2.getDialect();
			return d1 == d2 ?
					p1.getName().compareTo(p2.getName()) :
					d1.getPrefix().compareTo(d2.getPrefix());
		}
	});

	// Collection of utility methods in alphabetical order
	private static final TreeSet<UtilityMethod> utilitymethods = new TreeSet<UtilityMethod>(new Comparator<UtilityMethod>() {
		@Override
		public int compare(UtilityMethod m1, UtilityMethod m2) {
			return m1.getName().compareTo(m2.getName());
		}
	});

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
	 * Checks if the dialect is included in the given project.
	 * 
	 * @param dialect
	 * @param project
	 * @return <tt>true</tt> if the project includes the dialect.  (Dialects
	 * 		   bundled with this plugin are always associated with the project.)
	 */
	private static boolean dialectInProject(Dialect dialect, IJavaProject project) {

		return bundleddialects.contains(dialect) || projectdialects.get(project).contains(dialect);
	}

	/**
	 * Creates utility method suggestions from a utility object reference.
	 * 
	 * @param dialect	Parent dialect.
	 * @param objectref The utility object reference.
	 * @return Set of utility method suggestions based on the visible methods
	 * 		   of the utility object.
	 */
	private static HashSet<UtilityMethod> generateUtilityMethods(Dialect dialect,
		UtilityObjectReference objectref) {

		HashSet<UtilityMethod> generatedmethods = new HashSet<UtilityMethod>();

		String classname = objectref.getClazz();
		IJavaProject project = findCurrentJavaProject();
		try {
			IType type = project.findType(classname);
			if (type != null) {
				for (IMethod method: type.getMethods()) {
					if (!method.isConstructor()) {

						UtilityMethod utilitymethod = new UtilityMethod();
						utilitymethod.setDialect(dialect);

						// For Java bean methods, convert the suggestion to a property
						String methodname = method.getElementName();
						int propertypoint =
								methodname.startsWith("get") || methodname.startsWith("set") ? 3 :
								methodname.startsWith("is") ? 2 :
								-1;

						if (propertypoint != -1 && methodname.length() > propertypoint &&
							Character.isUpperCase(methodname.charAt(propertypoint))) {

							StringBuilder propertyname = new StringBuilder(methodname.substring(propertypoint));
							propertyname.insert(0, Character.toLowerCase(propertyname.charAt(0)));
							propertyname.deleteCharAt(1);
							utilitymethod.setName(objectref.getName() + "." + propertyname);
							utilitymethod.setJavaBeanProperty(true);
						}
						else {
							utilitymethod.setName(objectref.getName() + "." + methodname);
						}

						utilitymethods.add(utilitymethod);
					}
				}
			}
		}
		catch (JavaModelException ex) {
			logError("Unable to locate utility object reference: " + classname, ex);
		}

		return generatedmethods;
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

		return getProcessors(project, namespaces, pattern, AttributeProcessor.class);
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

		return getProcessors(project, namespaces, pattern, ElementProcessor.class);
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

		for (Processor processor: processors) {
			Dialect dialect = processor.getDialect();
			if (dialectInProject(dialect, project) &&
				dialectInNamespace(processor.getDialect(), namespaces) &&
				processorMatchesName(processor, processorname)) {
				return processor;
			}
		}
		return null;
	}

	/**
	 * Retrieve all processors of the given type, for the given project, and
	 * whose names match the starting pattern.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param pattern	 Start-of-string pattern to match.
	 * @param type		 Processor type to retrieve.
	 * @param <P>		 Processor type.
	 * @return List of all matching processors.
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Processor> List<P> getProcessors(IJavaProject project,
		List<QName> namespaces, String pattern, Class<P> type) {

		loadDialectsFromProject(project);

		ArrayList<P> matchedprocessors = new ArrayList<P>();
		for (Processor processor: processors) {
			Dialect dialect = processor.getDialect();
			if (processor.getClass().equals(type) &&
				dialectInProject(dialect, project) &&
				dialectInNamespace(dialect, namespaces) &&
				processorMatchesPattern(processor, pattern)) {
				matchedprocessors.add((P)processor);
			}
		}
		return matchedprocessors;
	}

	/**
	 * Retrieve the utility method with the full matching name.
	 * 
	 * @param project			The current project.
	 * @param namespaces		List of namespaces available at the current
	 * 							point in the document.
	 * @param utilitymethodname Full name of the utility method.
	 * @return Expression object with the given name, or <tt>null</tt> if no
	 * 		   expression object matches.
	 */
	public static UtilityMethod getUtilityMethod(IJavaProject project, List<QName> namespaces,
		String utilitymethodname) {

		loadDialectsFromProject(project);

		for (UtilityMethod expressionobject: utilitymethods) {
			if (dialectInProject(expressionobject.getDialect(), project) &&
				dialectInNamespace(expressionobject.getDialect(), namespaces) &&
				utilityMethodMatchesName(expressionobject, utilitymethodname)) {
				return expressionobject;
			}
		}
		return null;
	}

	/**
	 * Retrieve all utility methods for the given project, whose names match
	 * the starting pattern.
	 * 
	 * @param project	 The current project.
	 * @param namespaces List of namespaces available at the current point in
	 * 					 the document.
	 * @param pattern	 Start-of-string pattern to match.
	 * @return List of all matching utility methods.
	 */
	public static List<UtilityMethod> getUtilityMethods(IJavaProject project,
		List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project);

		ArrayList<UtilityMethod> matchedexpressionobjects = new ArrayList<UtilityMethod>();
		for (UtilityMethod utilitymethod: utilitymethods) {
			Dialect dialect = utilitymethod.getDialect();
			if (dialectInProject(dialect, project) &&
				dialectInNamespace(dialect, namespaces) &&
				utilityMethodMatchesPattern(utilitymethod, pattern)) {
				matchedexpressionobjects.add(utilitymethod);
			}
		}
		return matchedexpressionobjects;
	}

	/**
	 * Initialize the processor cache with the Thymeleaf dialects bundled with
	 * this plugin.
	 */
	public static void initialize() {

		logInfo("Loading bundled dialect files");

		List<Dialect> dialects = dialectloader.loadDialects(new BundledDialectLocator());
		for (Dialect dialect: dialects) {
			bundleddialects.add(dialect);
			loadDialectItems(dialect);
		}
	}

	/**
	 * Puts dialect items into their rightful collections.
	 * 
	 * @param dialect
	 */
	private static void loadDialectItems(Dialect dialect) {

		for (Object dialectitem: dialect.getDialectItems()) {
			if (dialectitem instanceof Processor) {
				processors.add((Processor)dialectitem);
			}
			else if (dialectitem instanceof UtilityMethod) {
				utilitymethods.add((UtilityMethod)dialectitem);
			}

			// Generate utility methods from the given class reference
			else if (dialectitem instanceof UtilityObjectReference) {
				utilitymethods.addAll(generateUtilityMethods(dialect, (UtilityObjectReference)dialectitem));
			}
		}
	}

	/**
	 * Gather all dialect information from the given project, if we haven't got
	 * information on that project in the first place.
	 * 
	 * @param project Project to scan for dialect information.
	 */
	private static void loadDialectsFromProject(IJavaProject project) {

		if (!projectdialects.containsKey(project)) {
			List<Dialect> dialects = dialectloader.loadDialects(
					new ProjectDependencyDialectLocator(project));
			projectdialects.put(project, dialects);
			for (Dialect dialect: dialects) {
				loadDialectItems(dialect);
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

		return pattern != null && (processor.getDialect().getPrefix() + ":" + processor.getName()).startsWith(pattern);
	}

	/**
	 * Checks if the utility method matches the given name.
	 * 
	 * @param utilitymethod
	 * @param name
	 * @return <tt>true</tt> if the name matches the expression object name.
	 */
	private static boolean utilityMethodMatchesName(UtilityMethod utilitymethod, String name) {

		if (name == null || name.isEmpty()) {
			return false;
		}
		return name.indexOf('#') == 0 && utilitymethod.getName().equals(name.substring(1));
	}

	/**
	 * Checks if the utility method name matches the given start-of-string
	 * pattern.
	 * 
	 * @param utilitymethod
	 * @param pattern
	 * @return <tt>true</tt> if the pattern matches against the expression
	 * 		   object name.
	 */
	private static boolean utilityMethodMatchesPattern(UtilityMethod utilitymethod, String pattern) {

		return pattern != null && ("#" + utilitymethod.getName()).startsWith(pattern);
	}
}
