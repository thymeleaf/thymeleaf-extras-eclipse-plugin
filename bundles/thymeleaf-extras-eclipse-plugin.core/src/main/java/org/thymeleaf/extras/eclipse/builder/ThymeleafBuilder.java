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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Map;

/**
 * Builder for projects with the Thymeleaf nature applied to them.  Removes
 * validation markers over HTML files which complain about non-namespaced items
 * that belong to dialects the project knows about.
 * 
 * @author Emanuel Rabina
 */
public class ThymeleafBuilder extends IncrementalProjectBuilder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor)
		throws CoreException {

		// Traverse the resource delta, finding html files with namespace warning
		// markers related to known Thymeleaf dialect prefixes
		IProject project = getProject();
		IResourceDelta delta = getDelta(project);


		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
	}
}
