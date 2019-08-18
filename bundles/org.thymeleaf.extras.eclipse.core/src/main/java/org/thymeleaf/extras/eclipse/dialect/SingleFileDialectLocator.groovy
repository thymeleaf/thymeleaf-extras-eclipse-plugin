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

package org.thymeleaf.extras.eclipse.dialect

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import static org.thymeleaf.extras.eclipse.CorePlugin.*

/**
 * A dialect locator target at a single, already-known, dialect file.
 * 
 * @author Emanuel Rabina
 */
class SingleFileDialectLocator implements DialectLocator<InputStream> {

	private final IPath dialectFilePath

	/**
	 * Constructor, set the dialect file path that this locator will retrieve.
	 * 
	 * @param dialectfilepath
	 */
	SingleFileDialectLocator(IPath dialectFilePath) {

		this.dialectFilePath = dialectFilePath
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<InputStream> locateDialects() {

		def dialectStreams = []
		try {
			dialectStreams.add(ResourcesPlugin.workspace.root.getFile(dialectFilePath).contents)
		}
		catch (CoreException ex) {
			logError("Dialect file ${dialectFilePath.lastSegment()} could not be read", ex)
		}
		return dialectStreams
	}
}
