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

package org.thymeleaf.extras.eclipse.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;
import static org.eclipse.core.resources.IResource.*;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for projects with the Thymeleaf nature applied to them.  Removes
 * validation markers over HTML files which complain about non-namespaced items
 * that belong to dialects the project knows about.
 * 
 * @author Emanuel Rabina
 */
public class ThymeleafBuilder extends IncrementalProjectBuilder {

	private static final String HTML_VALIDATION_MARKER        = "org.eclipse.wst.html.core.validationMarker";
	private static final String FILE_EXTENSION_HTML           = "html";
	private static final String MARKER_ATTRIBUTE_NAME_MESSAGE = "message";

	private static final Pattern UNDEFINED_ATTRIBUTE_PATTERN = Pattern.compile(
			"Undefined attribute name \\((.*?:.*?)\\)\\.");

	/**
	 * Remove HTML validation messages that refer to unknown attributes, when
	 * those attributes are in-fact Thymeleaf processors.
	 * 
	 * @param kind
	 * @param args
	 * @param monitor
	 * @return <tt>null</tt>
	 * @throws CoreException
	 */
	@Override
	protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor)
		throws CoreException {

		logInfo("Unvalidating HTML files for Thymeleaf attribute processors.  Build kind: " + (
				kind == INCREMENTAL_BUILD ? "incremental" :
				kind == AUTO_BUILD ? "auto" :
				kind == CLEAN_BUILD ? "clean" :
				"full"));

		IProject project = getProject();
		IJavaProject javaproject = JavaCore.create(project);

		// Retrieve markers from the delta or the project
		if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			unvalidateDelta(getDelta(project), javaproject, monitor);
		}
		else {
			unvalidateProject(javaproject, monitor);
		}

		return null;
	}

	/**
	 * Traverse the given delta and its children, searching for resource markers
	 * to remove.
	 * 
	 * @param delta
	 * @param javaproject
	 * @param monitor
	 * @throws CoreException
	 */
	private static void unvalidateDelta(IResourceDelta delta, IJavaProject javaproject,
		IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		IResource resource = delta.getResource();
		IPath resourcepath = resource.getFullPath();

		// Check markers of HTML resources
		String fileextension = resourcepath.getFileExtension();
		if (fileextension != null && fileextension.equals(FILE_EXTENSION_HTML)) {
			logInfo("Checking resource: " + resourcepath.toString());
			IMarker[] markers = delta.getResource().findMarkers(HTML_VALIDATION_MARKER, true, DEPTH_ZERO);
			for (IMarker marker: markers) {
				unvalidateMarker(marker, javaproject, monitor);
			}
		}

		// Check child resources if this is a folder/container
		else if (resource instanceof IContainer) {
			IResourceDelta[] childdeltas = delta.getAffectedChildren();
			for (IResourceDelta childdelta: childdeltas) {
				unvalidateDelta(childdelta, javaproject, monitor);
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
	 * @throws CoreException
	 */
	private static void unvalidateMarker(IMarker marker, IJavaProject javaproject,
		IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		String message = (String)marker.getAttribute(MARKER_ATTRIBUTE_NAME_MESSAGE);
		if (message != null) {
			Matcher matcher = UNDEFINED_ATTRIBUTE_PATTERN.matcher(message);
			if (matcher.matches()) {
				logInfo("Checking marker w/ message: " + message);
				String processor = matcher.group(1);
				if (DialectCache.getAttributeProcessor(javaproject, processor) != null) {
					marker.delete();
				}
			}
		}
	}

	/**
	 * Traverse the resources of a project, searching for resource markers to
	 * remove.
	 * 
	 * @param javaproject
	 * @param monitor
	 * @throws CoreException
	 */
	private static void unvalidateProject(IJavaProject javaproject,
		IProgressMonitor monitor) throws CoreException {

		IProject project = javaproject.getProject();
		logInfo("Checking project: " + project.getName());

		IMarker[] markers = project.findMarkers(HTML_VALIDATION_MARKER, true, DEPTH_INFINITE);
		for (IMarker marker: markers) {
			unvalidateMarker(marker, javaproject, monitor);
		}
	}
}
