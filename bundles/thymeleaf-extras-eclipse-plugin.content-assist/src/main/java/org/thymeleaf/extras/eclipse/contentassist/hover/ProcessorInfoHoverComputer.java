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

package org.thymeleaf.extras.eclipse.contentassist.hover;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.sse.ui.internal.derived.HTMLTextPresenter;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.thymeleaf.extras.eclipse.contentassist.AbstractProcessorComputer;
import org.thymeleaf.extras.eclipse.contentassist.ProcessorCache;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import org.w3c.dom.Node;

/**
 * Documentation-on-hover creator for Thymeleaf processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class ProcessorInfoHoverComputer extends AbstractProcessorComputer
	implements ITextHover, ITextHoverExtension {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {

		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, new HTMLTextPresenter(true));
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		String hoverinfo = null;

		try {
			int cursorposition = hoverRegion.getOffset();
			Node node = (Node)ContentAssistUtils.getNodeAt(textViewer, cursorposition);

			// Retrieve processor documentation on attribute or element nodes
			if (node instanceof IDOMElement) {
				String surroundingword = getSurroundingWord(textViewer.getDocument(), hoverRegion.getOffset());
				Processor processor = ProcessorCache.getProcessor(
						findCurrentProject(), findNodeNamespaces(node), surroundingword);
				if (processor != null && processor.isSetDocumentation()) {
					return processor.getDocumentation().getValue();
				}
			}
		}
		catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		return hoverinfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		// Don't think we need to specify a special region, so use the default
		return null;
	}

	/**
	 * Get whatever word is in the document around the cursor position.
	 * 
	 * @param document
	 * @param cursorposition
	 * @return The word at the document position.
	 * @throws BadLocationException
	 */
	private static String getSurroundingWord(IDocument document, int cursorposition)
		throws BadLocationException {

		int backtrace = cursorposition;
		while (backtrace > 0) {
			if (isProcessorChar(document.getChar(backtrace - 1))) {
				backtrace--;
			}
			else {
				break;
			}
		}

		int forwardtrace = cursorposition;
		while (forwardtrace < document.getLength() - 1) {
			if (isProcessorChar(document.getChar(forwardtrace + 1))) {
				forwardtrace++;
			}
			else {
				break;
			}
		}

		return document.get(backtrace, forwardtrace - backtrace + 1);
	}
}
