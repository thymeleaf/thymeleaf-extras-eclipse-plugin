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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.thymeleaf.extras.eclipse.dialect.SingleFileDialectLocator
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import static org.eclipse.core.resources.IResourceChangeEvent.*

import groovy.transform.MapConstructor
import groovy.transform.TupleConstructor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Named

/**
 * A resource change listener, acting on changes made to any dialect files,
 * updating the entries in the dialect tree as necessary.
 * 
 * @author Emanuel Rabina
 */
@Named
class DialectChangeListener implements IResourceChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(DialectChangeListener)

	private final ExecutorService resourceChangeExecutor = Executors.newSingleThreadExecutor()
	private final ConcurrentHashMap<IPath,IProject> dialectFilesToTrack = new ConcurrentHashMap<>()

	@Inject
	private final DialectItemProcessor dialectItemProcessor
	@Inject
	private final DialectTree dialectTree
	@Inject
	private final XmlDialectLoader xmlDialectLoader

	/**
	 * Stops the resource change executor.
	 */
	@PreDestroy
	void close() {

		resourceChangeExecutor.shutdownAwaitTermination()
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
				for (def dialectFilePath: dialectFilesToTrack.keySet()) {
					def dialectFileDelta = event.delta.findMember(dialectFilePath)
					if (dialectFileDelta) {
						logger.info("Dialect file ${dialectFilePath.lastSegment()} changed, reloading dialect")
						def javaProject = JavaCore.create(dialectFileDelta.resource.project)

						def locator = new SingleFileDialectLocator(dialectFilePath)
						def updateDialect = xmlDialectLoader.load(locator.locate())
						def updatedDialectItems = dialectItemProcessor.processDialectItems(updateDialect.first(), javaProject)
						dialectTree.updateDialect(dialectFilePath, updatedDialectItems)
					}
				}
				break

			// If a project containing a dialect is changing, remove the dialect from the dialect tree.
			case PRE_CLOSE:
			case PRE_DELETE:
				def project = (IProject)event.resource
				for (def dialectFilePath: dialectFilesToTrack.keySet()) {
					def dialectProject = dialectFilesToTrack.get(dialectFilePath)
					if (project == dialectProject) {
						logger.info("Project containing dialect file ${dialectFilePath.lastSegment()} has been closed/deleted, removing dialect.")
						dialectTree.updateDialect(dialectFilePath, null)
						dialectFilesToTrack.remove(dialectFilePath)
					}
				}
				break
			}
		}
	}

	/**
	 * Track a dialect file for changes.
	 * 
	 * @param dialectFilePath
	 * @param project
	 */
	void trackDialectFileForChanges(IPath dialectFilePath, IJavaProject project) {

		dialectFilesToTrack.put(dialectFilePath, project.project)
	}
}
