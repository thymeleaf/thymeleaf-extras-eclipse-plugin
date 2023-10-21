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

package org.thymeleaf.extras.eclipse.builder

import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResourceDelta
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.thymeleaf.extras.eclipse.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import static org.eclipse.core.resources.IResource.*

import java.util.regex.Pattern

/**
 * Builder for projects with the Thymeleaf nature applied to them.  Removes
 * validation markers over HTML files which complain about non-namespaced items
 * that belong to dialects the project knows about.
 * 
 * @author Emanuel Rabina
 */
class ThymeleafBuilder extends IncrementalProjectBuilder {

	private static final String HTML_VALIDATION_MARKER        = 'org.eclipse.wst.html.core.validationMarker'
	private static final String FILE_EXTENSION_HTML           = 'html'
	private static final String MARKER_ATTRIBUTE_NAME_MESSAGE = 'message'

	private static final Pattern UNDEFINED_ATTRIBUTE_PATTERN = ~/Undefined attribute name \((.*?:.*?)\)\./

	private static final Logger logger = LoggerFactory.getLogger(ThymeleafBuilder)

	private final DialectCache dialectCache = ContentAssistPlugin.default.getBean(DialectCache)

	/**
	 * Remove HTML validation messages that refer to unknown attributes, when
	 * those attributes are in fact Thymeleaf processors.
	 * 
	 * @param kind
	 * @param args
	 * @param monitor
	 * @return <tt>null</tt>
	 */
	@Override
	protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor) {

		logger.info('Unvalidating HTML files for Thymeleaf attribute processors.  Build kind: ' + (
				kind == INCREMENTAL_BUILD ? 'incremental' :
				kind == AUTO_BUILD ? 'auto' :
				kind == CLEAN_BUILD ? 'clean' :
				'full')
		)

		def project = getProject()
		def javaProject = JavaCore.create(project)

		// Retrieve markers from the delta or the project
		if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			unvalidateDelta(getDelta(project), javaProject, monitor)
		}
		else {
			unvalidateProject(javaProject, monitor)
		}

		return null
	}

	/**
	 * Traverse the given delta and its children, searching for resource markers
	 * to remove.
	 * 
	 * @param delta
	 * @param javaProject
	 * @param monitor
	 * @throws OperationCanceledException If the current action is being cancelled.
	 */
	private void unvalidateDelta(IResourceDelta delta, IJavaProject javaProject, IProgressMonitor monitor) {

		if (monitor.canceled) {
			throw new OperationCanceledException()
		}

		def resource = delta.resource
		def resourcePath = resource.fullPath

		// Check markers of HTML resources
		def fileExtension = resourcePath.fileExtension
		if (fileExtension?.equals(FILE_EXTENSION_HTML)) {
			logger.info("Unvalidating resource: ${resourcePath}")
			resource.findMarkers(HTML_VALIDATION_MARKER, true, DEPTH_ZERO).each { marker ->
				unvalidateMarker(marker, javaProject, monitor)
			}
		}

		// Check child resources if this is a folder/container
		else if (resource instanceof IContainer) {
			delta.affectedChildren.each { childDelta ->
				unvalidateDelta(childDelta, javaProject, monitor)
			}
		}
	}

	/**
	 * Check a marker to see if it's a complaint about a known attribute
	 * processor.
	 * 
	 * @param marker
	 * @param javaproject
	 * @param monitor
	 * @throws OperationCanceledException If the current action is being cancelled.
	 */
	private void unvalidateMarker(IMarker marker, IJavaProject javaProject, IProgressMonitor monitor) {

		if (monitor.canceled) {
			throw new OperationCanceledException()
		}

		def message = marker.getAttribute(MARKER_ATTRIBUTE_NAME_MESSAGE)
		if (message) {
			def matcher = UNDEFINED_ATTRIBUTE_PATTERN.matcher(message)
			if (matcher.matches()) {
				logger.info("Checking marker w/ message: ${message}")
				def processor = matcher.group(1)
				if (dialectCache.getAttributeProcessor(javaProject, processor)) {
					marker.delete()
				}
			}
		}
	}

	/**
	 * Traverse the resources of a project, searching for resource markers to
	 * remove.
	 * 
	 * @param javaProject
	 * @param monitor
	 */
	private void unvalidateProject(IJavaProject javaProject, IProgressMonitor monitor) {

		def project = javaProject.project
		logger.info("Checking project: ${project.name}")

		project.findMarkers(HTML_VALIDATION_MARKER, true, DEPTH_INFINITE).each { marker ->
			unvalidateMarker(marker, javaProject, monitor)
		}
	}
}
