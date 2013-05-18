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

package org.thymeleaf.extras.eclipse.dialect.cache;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.thymeleaf.extras.eclipse.dialect.xml.Dialect;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.logInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A resource change listener, acting on changes made to any dialect files,
 * updating the entries in the dialect tree as necessary.
 * 
 * @author Emanuel Rabina
 */
public class DialectChangeListener implements IResourceChangeListener {

	// Collection of dialect files that will be watched for updates to keep the cache up-to-date
	private final CopyOnWriteArraySet<IPath> dialectfilepaths = new CopyOnWriteArraySet<IPath>();

	private final ExecutorService resourcechangeexecutor = Executors.newSingleThreadExecutor();

	/**
	 * When notified of a resource change, redirect the work to the change
	 * executor thread so as to not block the event change thread.
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {

/*		resourcechangeexecutor.execute(new Runnable() {
			@Override
			public void run() {

				if (event.getType() == POST_CHANGE) {
					IResourceDelta delta = event.getDelta();
					for (final IPath dialectfilepath: dialectfilepaths) {
						IResourceDelta dialectfiledelta = delta.findMember(dialectfilepath);
						if (dialectfiledelta != null) {
							logInfo("Dialect file " + dialectfilepath.lastSegment() + " changed, reloading dialect");

							List<Dialect> updateddialects = xmldialectloader.loadDialects(
									new SingleFileDialectLocator(dialectfilepath));
							for (Dialect updateddialect: updateddialects) {
								IProject dialectfileproject = dialectfiledelta.getResource().getProject();
								IJavaProject javaproject = JavaCore.create(dialectfileproject);
								removeDialectItems(updateddialect);
								loadDialectItems(updateddialect, javaproject);
							}
						}
					}
				}
			}
		});
*/	}

	/**
	 * Stops the resource change executor.
	 */
	void shutdown() {

		resourcechangeexecutor.shutdown();
		try {
			if (resourcechangeexecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				resourcechangeexecutor.shutdownNow();
			}
		}
		catch (InterruptedException ex) {
			// Do nothing
		}
	}
}
