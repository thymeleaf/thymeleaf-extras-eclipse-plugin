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

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath

import groovy.transform.TupleConstructor
import jakarta.inject.Inject

/**
 * A dialect locator target at a single, already-known, dialect file.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SingleFileDialectLocator implements DialectLocator {

	@Inject
	private final IWorkspace workspace

	final IPath dialectFilePath

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<PathAndStream> locate() {

		return [
			new PathAndStream(dialectFilePath, workspace.root.getFile(dialectFilePath).contents)
		]
	}
}
