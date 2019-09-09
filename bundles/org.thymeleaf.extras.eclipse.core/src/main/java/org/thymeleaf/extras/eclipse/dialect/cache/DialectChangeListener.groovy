/* 
 * Copyright 2013, The Thymeleaf Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResourceChangeEvent
import org.eclipse.core.resources.IResourceChangeListener
import org.eclipse.core.resources.IResourceDelta
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.thymeleaf.extras.eclipse.dialect.SingleFileDialectLocator
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import static org.eclipse.core.resources.IResourceChangeEvent.*
import static org.thymeleaf.extras.eclipse.CorePlugin.*
import static org.thymeleaf.extras.eclipse.dialect.cache.DialectItemProcessor.*

import groovy.transform.MapConstructor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A resource change listener, acting on changes made to any dialect files,
 * updating the entries in the dialect tree as necessary.
 * 
 * @author Emanuel Rabina
 */
class DialectChangeListener implements IResourceChangeListener {

	private final ExecutorService resourceChangeExecutor = Executors.newSingleThreadExecutor()

	// Collection of dialect files that will be watched for updates to keep the cache up-to-date
	private final ConcurrentHashMap<IPath,IProject> dialectFilePaths = new ConcurrentHashMap<>()

	final DialectTree dialectTree
	final XmlDialectLoader xmlDialectLoader

	/**
	 * @param dialectTree
	 * @param xmlDialectLoader
	 */
	DialectChangeListener(DialectTree dialectTree, XmlDialectLoader xmlDialectLoader) {

		this.dialectTree = dialectTree
		this.xmlDialectLoader = xmlDialectLoader
	}

	/**
	 * When notified of a resource change, redirect the work to the change
	 * executor thread so as to not block the event change thread.
	 */
	@Override
	void resourceChanged(final IResourceChangeEvent event) {

		resourceChangeExecutor.execute { ->
			switch (event.type) {

			// If a dialect file has changed, update the dialect items associated with it
			case POST_CHANGE:
				for (def dialectFilePath: dialectFilePaths.keySet()) {
					def dialectFileDelta = event.delta.findMember(dialectFilePath)
					if (dialectFileDelta) {
						logInfo("Dialect file ${dialectFilePath.lastSegment()} changed, reloading dialect")
						def javaProject = JavaCore.create(dialectFileDelta.resource.project)

						def locator = new SingleFileDialectLocator(dialectFilePath)
						def updateDialect = xmlDialectLoader.loadDialects(locator.locateDialects())
						def updatedDialectItems = processDialectItems(updateDialect.first(), javaProject)
						dialectTree.updateDialect(dialectFilePath, updatedDialectItems)
					}
				}
				break

			// If a project containing a dialect is changing, remove the dialect from the dialect tree.
			case PRE_CLOSE:
			case PRE_DELETE:
				def project = (IProject)event.resource
				for (def dialectFilePath: dialectFilePaths.keySet()) {
					def dialectProject = dialectFilePaths.get(dialectFilePath)
					if (project == dialectProject) {
						logInfo("Project containing dialect file ${dialectFilePath.lastSegment()} has been closed/deleted, removing dialect.")
						dialectTree.updateDialect(dialectFilePath, null)
						dialectFilePaths.remove(dialectFilePath)
					}
				}
				break
			}
		}
	}

	/**
	 * Stops the resource change executor.
	 */
	void shutdown() {

		resourceChangeExecutor.shutdown()
		if (resourceChangeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
			resourceChangeExecutor.shutdownNow()
		}
	}

	/**
	 * Track a dialect file for changes.
	 * 
	 * @param dialectFilePath
	 * @param project
	 */
	void trackDialectFileForChanges(IPath dialectFilePath, IJavaProject project) {

		dialectFilePaths.put(dialectFilePath, project.project)
	}
}
