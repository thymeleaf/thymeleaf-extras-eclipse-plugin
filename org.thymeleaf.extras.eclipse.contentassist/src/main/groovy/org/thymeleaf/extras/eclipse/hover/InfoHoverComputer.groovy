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

package org.thymeleaf.extras.eclipse.hover

import org.eclipse.jface.text.DefaultInformationControl
import org.eclipse.jface.text.IInformationControl
import org.eclipse.jface.text.IInformationControlCreator
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextHover
import org.eclipse.jface.text.ITextHoverExtension
import org.eclipse.jface.text.ITextViewer
import org.eclipse.swt.widgets.Shell
import org.eclipse.wst.sse.ui.internal.derived.HTMLTextPresenter
import org.thymeleaf.extras.eclipse.ContentAssistContainer
import org.thymeleaf.extras.eclipse.ContentAssistPlugin
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache

/**
 * Documentation-on-hover creator for Thymeleaf processors.
 * 
 * @author Emanuel Rabina
 */
class InfoHoverComputer implements ITextHover, ITextHoverExtension {

	private final DialectCache dialectCache

	/**
	 * Constructor, used by Eclipse to create an instance of this class so
	 * defaults to using the dialect cache via direct access to the Spring
	 * container instance.
	 */
	InfoHoverComputer() {

		this(ContentAssistContainer.instance.getBean(DialectCache))
	}

	/**
	 * Constructor, create a new hover computer with the specified dialect cache.
	 * 
	 * @param dialectCache
	 */
	InfoHoverComputer(DialectCache dialectCache) {

		this.dialectCache = dialectCache
	}

	@Override
	IInformationControlCreator getHoverControlCreator() {

		return new IInformationControlCreator() {
			@Override
			IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, new HTMLTextPresenter(true))
			}
		}
	}

	@Override
	String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		String hoverInfo = null

		def cursorPosition = hoverRegion.offset
		def node = textViewer.getNodeAt(cursorPosition)

		// Retrieve documentation on attribute or element nodes
		if (node.elementNode) {
			def surroundingWord = textViewer.document.get(cursorPosition, hoverRegion.length)

			if (surroundingWord ==~ /[\w:-]*/) {
				def processor = dialectCache.getProcessor(ContentAssistPlugin.findCurrentJavaProject(), node.knownNamespaces, surroundingWord)
				return processor?.documentation?.value
			}

			// NOTE: The HTML editor currently doesn't give a precise enough offset
			//       to determine the _exact_ point being hovered over, making it
			//       difficult to pick out expression object methods and grab its
			//       help text.
//			else if (isUtilityMethodPattern(surroundingword)) {
//				UtilityMethod utilitymethod = ProcessorCache.getUtilityMethod(
//						findCurrentProject(), findNodeNamespaces(node), surroundingword)
//				if (utilitymethod != null && utilitymethod.isSetDocumentation()) {
//					return utilitymethod.getDocumentation().getValue()
//				}
//			}
		}
		return hoverInfo
	}

	/**
	 * Override so as to use the default hover region.
	 * 
	 * @param textViewer
	 * @param offset
	 * @return {@code null}
	 */
	@Override
	IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		return null
	}
}
