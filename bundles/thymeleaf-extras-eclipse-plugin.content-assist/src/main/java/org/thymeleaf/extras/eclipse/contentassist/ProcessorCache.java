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

import org.eclipse.core.resources.IProject;
import org.thymeleaf.extras.eclipse.dialect.DialectLoader;
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
	private static final HashMap<IProject,List<Dialect>> projectdialects =
			new HashMap<IProject,List<Dialect>>();

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
	public static List<AttributeProcessor> getAttributeProcessors(IProject project,
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
	public static List<ElementProcessor> getElementProcessors(IProject project,
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
	public static Processor getProcessor(IProject project, List<QName> namespaces, String processorname) {

		loadDialectsFromProject(project);

		for (Processor processor: processors) {
			if (processorInProject(processor, project) &&
				processorInNamespace(processor, namespaces) &&
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
	public static <P extends Processor> List<P> getProcessors(IProject project,
		List<QName> namespaces, String pattern, Class<P> type) {

		loadDialectsFromProject(project);

		ArrayList<P> matchedprocessors = new ArrayList<P>();
		for (Processor processor: processors) {
			if (processor.getClass().equals(type) &&
				processorInProject(processor, project) &&
				processorInNamespace(processor, namespaces) &&
				processorMatchesPattern(processor, pattern)) {
				matchedprocessors.add((P)processor);
			}
		}
		return matchedprocessors;
	}

	/**
	 * Initialize the processor cache with the Thymeleaf dialects bundled with
	 * this plugin.
	 */
	public static void initialize() {

		ContentAssistPlugin.logInfo("Loading bundled dialect files");

		List<Dialect> dialects = dialectloader.loadDialects(new BundledDialectLocator());
		for (Dialect dialect: dialects) {
			bundleddialects.add(dialect);
			processors.addAll(dialect.getProcessors());
		}
	}

	/**
	 * Gather all dialect information from the given project, if we haven't got
	 * information on that project in the first place.
	 * 
	 * @param project Project to scan for dialect information.
	 */
	private static void loadDialectsFromProject(IProject project) {

		if (!projectdialects.containsKey(project)) {
			List<Dialect> dialects = dialectloader.loadDialects(
					new ProjectDependencyDialectLocator(project));
			projectdialects.put(project, dialects);
			for (Dialect dialect: dialects) {
				processors.addAll(dialect.getProcessors());
			}
		}
	}

	/**
	 * Checks if the processor's dialect is in the list of given namespaces.
	 * 
	 * @param processor
	 * @param namespaces
	 * @return <tt>true</tt> if the processor's dialect prefix and namespace are
	 * 		   listed in the <tt>namespaces</tt> collection.
	 */
	private static boolean processorInNamespace(Processor processor, List<QName> namespaces) {

		Dialect dialect = processor.getDialect();
		for (QName namespace: namespaces) {
			if (dialect.getPrefix().equals(namespace.getPrefix()) &&
				dialect.getNamespaceUri().equals(namespace.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the processor's dialect is included in the given project.
	 * 
	 * @param processor
	 * @param project
	 * @return <tt>true</tt> if the project includes the processor's dialect.
	 * 		   (Dialects bundled with this plugin are always associated with the
	 * 		   project.)
	 */
	private static boolean processorInProject(Processor processor, IProject project) {

		Dialect dialect = processor.getDialect();
		return bundleddialects.contains(dialect) || projectdialects.get(project).contains(dialect);
	}

	/**
	 * Checks if the processor name (prefix:name) matches the given name.
	 * 
	 * @param processor
	 * @param processorname
	 * @return <tt>true</tt> if the name matches the full processor name.
	 */
	private static boolean processorMatchesName(Processor processor, String processorname) {

		int separatorindex = processorname.indexOf(':');
		return separatorindex != -1 &&
				processor.getDialect().getPrefix().equals(processorname.substring(0, separatorindex)) &&
				processor.getName().equals(processorname.substring(separatorindex + 1));
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
}
