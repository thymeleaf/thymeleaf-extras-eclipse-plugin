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

package org.thymeleaf.extras.eclipse.dialect.cache

import org.eclipse.core.runtime.IPath
import org.thymeleaf.extras.eclipse.dialect.xml.AttributeProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.DialectItem
import org.thymeleaf.extras.eclipse.dialect.xml.ElementProcessor
import org.thymeleaf.extras.eclipse.dialect.xml.ExpressionObjectMethod

/**
 * Representation of a project that contains one or more files which in turn
 * contain dialect information.
 * 
 * @author Emanuel Rabina
 */
class DialectProject {

	private final HashMap<IPath,DialectFile> dialectFilePaths = [:]

	@Lazy(soft = true)
	private ArrayList<AttributeProcessor> attributeProcessors = { ->
		return dialectFilePaths.values().inject([]) { acc, dialectFile ->
			return acc + dialectFile.attributeProcessors
		}
	}()

	@Lazy(soft = true)
	private ArrayList<ElementProcessor> elementProcessors = { ->
		return dialectFilePaths.values().inject([]) { acc, dialectFile ->
			return acc + dialectFile.elementProcessors
		}
	}()

	@Lazy(soft = true)
	private ArrayList<ExpressionObjectMethod> expressionObjectMethods = { ->
		return dialectFilePaths.values().inject([]) { acc, dialectFile ->
			return acc + dialectFile.expressionObjectMethods
		}
	}()

	/**
	 * Adds a dialect to this project.  If the path already exists for a dialect
	 * in this project, then this method will ovewrite that dialect.
	 * 
	 * @param dialectFilepath
	 *   The resource path to the dialect.
	 * @param dialectItems
	 *   A list of the items in the dialect, but already processed to include all
	 *   the information they need for content assist queries.
	 */
	void addDialect(IPath dialectFilePath, List<DialectItem> dialectItems) {

		dialectFilePaths[dialectFilePath] = new DialectFile(dialectItems)
		attributeProcessors     = null
		elementProcessors       = null
		expressionObjectMethods = null
	}

	/**
	 * Return whether or not this project makes use of a dialect with the given
	 * resource path.
	 * 
	 * @param dialectFilePath
	 * @return <tt>true</tt> if a dialect in this project originates from the
	 * 		   given path.
	 */
	boolean hasDialect(IPath dialectFilePath) {

		return dialectFilePaths[dialectFilePath]
	}

	/**
	 * Removes a dialect from this project.
	 * 
	 * @param dialectfilepath The resource path to the dialect.
	 */
	void removeDialect(IPath dialectfilepath) {

		dialectFilePaths.remove(dialectfilepath)
		attributeProcessors     = null
		elementProcessors       = null
		expressionObjectMethods = null
	}
}
