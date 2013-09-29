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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache;

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

		IProject project = getProject();

		// Retrieve markers from the delta or the project
		IMarker[] markers = kind == INCREMENTAL_BUILD || kind == AUTO_BUILD ?
				getDelta(project).getResource().findMarkers(HTML_VALIDATION_MARKER, true, IResource.DEPTH_INFINITE) :
				project.findMarkers(HTML_VALIDATION_MARKER, true, IResource.DEPTH_INFINITE);

		if (markers.length > 0) {
			IJavaProject javaproject = JavaCore.create(project);

			for (IMarker marker: markers) {

				// Quit if the user asked to cancel the task
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}

				String message = (String)marker.getAttribute(MARKER_ATTRIBUTE_NAME_MESSAGE);
				Matcher matcher = UNDEFINED_ATTRIBUTE_PATTERN.matcher(message);
				if (matcher.matches()) {
					String processor = matcher.group(1);
					if (DialectCache.getAttributeProcessor(javaproject, processor) != null) {
						marker.delete();
					}
				}
			}
		}

		return null;
	}
}
