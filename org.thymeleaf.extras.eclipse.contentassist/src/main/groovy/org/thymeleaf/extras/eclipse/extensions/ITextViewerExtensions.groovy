/* 
 * Copyright 2021, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.extensions

import org.eclipse.jface.text.ITextViewer
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils
import org.w3c.dom.Node

/**
 * Extensions to Eclipse's {@link ITextViewer} class.
 * 
 * @author Emanuel Rabina
 */
class ITextViewerExtensions {

	/**
	 * Wrapper around the {@link ContentAssistUtils#getNodeAt} method.  Note that
	 * while the signature of that method is to return an {@code IndexedRegion},
	 * WTP always seems to return an {@code IDOMNode} which in turn implements
	 * {@link Node}, which is much easier to work with.
	 * 
	 * @param self
	 * @param offset
	 * @return
	 */
	static Node getNodeAt(ITextViewer self, int offset) {

		return ContentAssistUtils.getNodeAt(self, offset) as Node
	}

	/**
	 * Wrapper around the {@link ContentAssistUtils#getStructuredDocumentRegion}
	 * method.
	 * 
	 * @param self
	 * @param offset
	 * @return
	 */
	static IStructuredDocumentRegion getStructuredDocumentRegion(ITextViewer self, int offset) {

		return ContentAssistUtils.getStructuredDocumentRegion(self, offset)
	}
}
