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

package org.thymeleaf.extras.eclipse.template;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.thymeleaf.extras.eclipse.scanner.ResourceLocator;
import static org.thymeleaf.extras.eclipse.CorePlugin.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Locates Thymeleaf templates in the current project.  Basically, all HTML
 * files.
 * 
 * @author Emanuel Rabina
 */
public class ProjectTemplateLocator implements ResourceLocator {

	private static final String HTML_FILE_EXTENSION = ".html";

	private final IJavaProject project;

	/**
	 * Constructor, sets the project to scan for templates.
	 * 
	 * @param project
	 */
	public ProjectTemplateLocator(IJavaProject project) {

		this.project = project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InputStream> locateResources() {

		logInfo("Scanning for Thymeleaf templates in the project");
		long start = System.currentTimeMillis();

		ExecutorService executorservice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		final ArrayList<InputStream> templatestreams = new ArrayList<InputStream>();

		try {
			// Multi-threaded search for template files - there can be a lot of files to get through
			ArrayList<Future<List<IFile>>> scannertasks = new ArrayList<Future<List<IFile>>>();

			scanContainer(project.getProject(), scannertasks, executorservice);

			// Collect all file results
			for (Future<List<IFile>> scannertask: scannertasks) {
				try {
					for (IFile file: scannertask.get()) {
						templatestreams.add(file.getContents());
					}
				}
				catch (ExecutionException ex) {
					logError("Unable to execute scanning task", ex);
				}
				catch (InterruptedException ex) {
					logError("Unable to execute scanning task", ex);
				}
			}
		}
		catch (CoreException ex) {
			// If we get here, the project cannot be read.  Return the empty list.
			logError("Project " + project.getProject().getName() + " could not be read", ex);
		}
		finally {
			executorservice.shutdown();
			try {
				if (!executorservice.awaitTermination(5, TimeUnit.SECONDS)) {
					executorservice.shutdownNow();
				}
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}

		logInfo("Scanning complete.  Execution time: " + (System.currentTimeMillis() - start) + "ms");
		return templatestreams;
	}

	/**
	 * Recursive scan of a container resource (currently only folders and
	 * projects), searches for files to pass along to the {@link scanFile(IFile)}
	 * method.
	 * 
	 * @param container
	 * @param scannertasks
	 * @param executorservice
	 */
	private static void scanContainer(final IContainer container,
		final ArrayList<Future<List<IFile>>> scannertasks, final ExecutorService executorservice) {

		// Projects and folders
		if (container instanceof IProject || container instanceof IFolder) {
			scannertasks.add(executorservice.submit(new Callable<List<IFile>>() {
				@Override
				public List<IFile> call() throws Exception {

					ArrayList<IFile> files = new ArrayList<IFile>();
					for (IResource resource: container.members()) {

						// Recurse folder scanning
						if (resource instanceof IContainer) {
							scanContainer((IContainer)resource, scannertasks, executorservice);
						}

						// Accept files
						else if (resource instanceof IFile) {
							IFile file = (IFile)resource;
							if (file.getName().endsWith(HTML_FILE_EXTENSION)) {
								files.add(file);
							}
						}
					}
					return files;
				}
			}));
		}
	}
}
