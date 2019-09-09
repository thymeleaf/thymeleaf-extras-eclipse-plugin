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

package org.thymeleaf.extras.eclipse.template

import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject
import org.thymeleaf.extras.eclipse.scanner.ResourceLocator
import static org.thymeleaf.extras.eclipse.CorePlugin.*

import groovy.transform.TupleConstructor
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Locates Thymeleaf templates in the current project.  Basically, all HTML
 * files.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ProjectTemplateLocator implements ResourceLocator<IFile> {

	final IJavaProject project

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<IFile> locateResources() {

		logInfo("Scanning for Thymeleaf templates in the project")

		return time('Scanning for templates') { ->

			// Multi-threaded search for template files - there can be a lot of files
			// to get through.
			def executorService = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors())
			return executorService.executeAndShutdown { ExecutorService executor ->

				def scannerTasks = new ArrayList<Future<List<IFile>>>()
				def addTasks
				addTasks = { IContainer container ->
					if (container instanceof IProject || container instanceof IFolder) {
						scannerTasks << executor.submit({ ->
							def files = new ArrayList<IFile>()
							container.members().each { resource ->
								if (resource instanceof IContainer) {
									addTasks(container)
								}
								else if (resource instanceof IFile && resource.name.endsWith('.html')) {
									files << resource
								}
							}
							return files
						} as Callable)
					}
				}
				addTasks(project.project)

				return scannerTasks.inject([]) { acc, scannerTask ->
					return acc + scannerTask.get()
				}
			}
		}
	}
}
