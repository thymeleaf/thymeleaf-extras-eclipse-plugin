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

package org.thymeleaf.extras.eclipse.dialect.cache

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import org.thymeleaf.extras.eclipse.dialect.ProjectDependencyDialectLocator
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod
import org.thymeleaf.extras.eclipse.dialect.xml.Processor
import org.thymeleaf.extras.eclipse.nature.ThymeleafNature
import static org.eclipse.core.resources.IResourceChangeEvent.*

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Named
import javax.xml.namespace.QName

/**
 * A basic in-memory store of all known Thymeleaf dialects and their processors
 * and expression object methods.
 * 
 * @author Emanuel Rabina
 */
@Named
class DialectCache {

	@Inject
	private final XmlDialectLoader xmlDialectLoader
	@Inject
	private final DialectTree dialectTree
	@Inject
	private final DialectChangeListener dialectChangeListener
	@Inject
	private final DialectItemProcessor dialectItemProcessor

	/**
	 * Initialize the cache.
	 */
	@PostConstruct
	void init() {

		ResourcesPlugin.workspace.addResourceChangeListener(dialectChangeListener, POST_CHANGE | PRE_CLOSE | PRE_DELETE)
	}

	/**
	 * Shutdown method of the cache, cleans up any processes that need
	 * cleaning-up.
	 */
	@PreDestroy
	void close() {

		ResourcesPlugin.workspace.removeResourceChangeListener(dialectChangeListener)
	}

	/**
	 * Checks if the dialect is in the list of given namespaces.
	 * 
	 * @param dialect
	 * @param namespaces
	 * @return <tt>true</tt> if the dialect prefix (and namespace if the dialect
	 * 		   is namespace strict) are listed in the <tt>namespaces</tt>
	 * 		   collection.
	 */
	private boolean dialectInNamespace(Dialect dialect, List<QName> namespaces) {

		return namespaces.any { namespace ->
			return dialect.prefix == namespace.prefix && 
				(!dialect.namespaceStrict || dialect.namespaceUri == namespace.namespaceURI)
		}
	}

	/**
	 * Checks if the expression object method matches the given name.
	 * 
	 * @param method
	 * @param name
	 * @return <tt>true</tt> if the name matches the expression object name.
	 */
	private boolean expressionObjectMethodMatchesName(ExpressionObjectMethod method, String name) {

		return name == method.fullName
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
	private boolean expressionObjectMethodMatchesPattern(ExpressionObjectMethod method, String pattern) {

		return pattern && method.fullName.startsWith(pattern)
	}

	/**
	 * Retrieve the attribute processor in the given project, with the full
	 * matching name.
	 * 
	 * @param project		    The current project.
	 * @param processorName	Full name of the attribute processor.
	 * @return Attribute processor with the given name, or <tt>null</tt> if no
	 * 		   processor could be found.
	 */
	Processor getAttributeProcessor(IJavaProject project, String processorName) {

		loadDialectsFromProject(project)

		return dialectTree.getAttributeProcessorsForProject(project).find { processor ->
			return processor.fullName == processorName
		}
	}

	/**
	 * Retrieve all attribute processors for the given project, whose names
	 * match the starting pattern.
	 * 
	 * @param project    The current project.
	 * @param namespaces List of namespaces available at the current point in
	 *                   the document.
	 * @param pattern    Start-of-string pattern to match.
	 * @return List of all matching attribute processors.
	 */
	List<AttributeProcessor> getAttributeProcessors(IJavaProject project, List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project)

		return dialectTree.getAttributeProcessorsForProject(project).findAll { processor ->
			return (thymeleafNatureEnabled(project) || dialectInNamespace(processor.dialect, namespaces)) &&
				processorMatchesPattern(processor, pattern)
		}
	}

	/**
	 * Retrieve all element processors for the given project, whose names match
	 * the starting pattern.
	 * 
	 * @param project    The current project.
	 * @param namespaces List of namespaces available at the current point in
	 *                   the document.
	 * @param pattern	   Start-of-string pattern to match.
	 * @return List of all matching element processors
	 */
	List<ElementProcessor> getElementProcessors(IJavaProject project, List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project)

		return dialectTree.getElementProcessorsForProject(project).findAll { processor ->
			return (thymeleafNatureEnabled(project) || dialectInNamespace(processor.dialect, namespaces)) &&
				processorMatchesPattern(processor, pattern)
		}
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
	ExpressionObjectMethod getExpressionObjectMethod(IJavaProject project,
		List<QName> namespaces, String methodname) {

		loadDialectsFromProject(project)

		return dialectTree.getExpressionObjectMethodsForProject(project).find { expressionObject ->
			return (thymeleafNatureEnabled(project) || dialectInNamespace(expressionObject.dialect, namespaces)) &&
				expressionObjectMethodMatchesName(expressionObject, methodname)
		}
	}

	/**
	 * Retrieve all expression object methods for the given project, whose names
	 * match the starting pattern.
	 * 
	 * @param project    The current project.
	 * @param namespaces List of namespaces available at the current point in
	 *                   the document.
	 * @param pattern    Start-of-string pattern to match.
	 * @return List of all matching expression object methods.
	 */
	List<ExpressionObjectMethod> getExpressionObjectMethods(IJavaProject project,
		List<QName> namespaces, String pattern) {

		loadDialectsFromProject(project)

		return dialectTree.getExpressionObjectMethodsForProject(project).findAll { expressionObjectMethod ->
			return (thymeleafNatureEnabled(project) || dialectInNamespace(expressionObjectMethod.dialect, namespaces)) &&
				expressionObjectMethodMatchesPattern(expressionObjectMethod, pattern)
		}
	}

	/**
	 * Retrieve the processor with the full matching name.
	 * 
	 * @param project       The current project.
	 * @param namespaces    List of namespaces available at the current point in
	 *                      the document.
	 * @param processorName	Full name of the processor.
	 * @return Processor for the given prefix and name, or <tt>null</tt> if no
	 * 		   processor matches.
	 */
	Processor getProcessor(IJavaProject project, List<QName> namespaces, String processorName) {

		loadDialectsFromProject(project)

		def processors =
			dialectTree.getAttributeProcessorsForProject(project) +
			dialectTree.getElementProcessorsForProject(project)
		return processors.find { processor ->
			return (thymeleafNatureEnabled(project) || dialectInNamespace(processor.dialect, namespaces)) &&
				processorMatchesName(processor, processorName)
		}
	}

	/**
	 * Gather all dialect information from the given project, if we haven't got
	 * information on that project in the first place.
	 * 
	 * @param project Project to scan for dialect information.
	 */
	// TODO: Is it possible to make this use @Lazy so it's not being called from
	//       all the other methods?
	private void loadDialectsFromProject(IJavaProject project) {

		if (!dialectTree.containsProject(project)) {
			def pathsAndDialects = xmlDialectLoader.load(new ProjectDependencyDialectLocator(project))
			if (pathsAndDialects.size() > 0) {
				pathsAndDialects.eachWithIndex { pathAndDialect, index ->
					dialectTree.addProjectDialect(project, pathAndDialect.path,
						dialectItemProcessor.processDialectItems(pathAndDialect.dialect, project))
					dialectChangeListener.trackDialectFileForChanges(pathAndDialect.path, project)
				}
			}
			else {
				dialectTree.addProjectDialect(project, null, new ArrayList<DialectItem>())
			}
		}
	}

	/**
	 * Checks if the processor name (prefix:name or data-prefix-name) matches
	 * the given name.
	 * 
	 * @param processor
	 * @param name
	 * @return <tt>true</tt> if the name matches the full processor name.
	 */
	private boolean processorMatchesName(Processor processor, String name) {

		return processor.fullName == name ||
			(processor instanceof AttributeProcessor && processor.fullDataName == name)
	}

	/**
	 * Checks if the processor name (prefix:name or data-prefix-name) matches
	 * the given start-of-string pattern.
	 * 
	 * @param processor
	 * @param pattern
	 * @return <tt>true</tt> if the pattern matches against the processor prefix
	 * 		   and name.
	 */
	private boolean processorMatchesPattern(Processor processor, String pattern) {

		return processor.fullName.startsWith(pattern) ||
			(processor instanceof AttributeProcessor && processor.fullDataName.startsWith(pattern))
	}

	/**
	 * Check if the Thymeleaf nature has been applied to the given project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project has the Thymeleaf nature.
	 */
	private boolean thymeleafNatureEnabled(IJavaProject project) {

		return project.project.hasNature(ThymeleafNature.THYMELEAF_NATURE_ID)
	}
}
