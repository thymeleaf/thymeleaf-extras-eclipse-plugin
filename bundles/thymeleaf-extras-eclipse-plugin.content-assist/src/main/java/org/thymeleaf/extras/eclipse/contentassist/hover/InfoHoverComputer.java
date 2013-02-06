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
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.sse.ui.internal.derived.HTMLTextPresenter;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.contentassist.DialectCache;
import org.thymeleaf.extras.eclipse.dialect.xml.Processor;
import static org.thymeleaf.extras.eclipse.contentassist.ContentAssistPlugin.*;

import java.util.regex.Pattern;

/**
 * Documentation-on-hover creator for Thymeleaf processors.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class InfoHoverComputer extends AbstractComputer implements ITextHover, ITextHoverExtension {

	private static final Pattern PROCESSOR_NAME_PATTERN           = Pattern.compile("[\\w:-]*");
//	private static final Pattern EXPRESSION_OBJECT_METHOD_PATTERN = Pattern.compile("#\\w*(\\.\\w*)?");

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
			IDOMNode node = (IDOMNode)ContentAssistUtils.getNodeAt(textViewer, cursorposition);

			// Retrieve documentation on attribute or element nodes
			if (node.getNodeType() == IDOMNode.ELEMENT_NODE) {
				String surroundingword = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());

				if (isProcessorNamePattern(surroundingword)) {
					Processor processor = DialectCache.getProcessor(
							findCurrentJavaProject(), findNodeNamespaces(node), surroundingword);
					if (processor != null && processor.isSetDocumentation()) {
						return processor.getDocumentation().getValue();
					}
				}

				// NOTE: The HTML editor currently doesn't give a precise enough offset
				//       to determine the _exact_ point being hovered over, making it
				//       difficult to pick out expression object methods and grab its
				//       help text.
/*				else if (isUtilityMethodPattern(surroundingword)) {
					UtilityMethod utilitymethod = ProcessorCache.getUtilityMethod(
							findCurrentProject(), findNodeNamespaces(node), surroundingword);
					if (utilitymethod != null && utilitymethod.isSetDocumentation()) {
						return utilitymethod.getDocumentation().getValue();
					}
				}
*/			}
		}
		catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		return hoverinfo;
	}

	/**
	 * Does nothing so as to use the default hover region.
	 */
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		return null;
	}

	/**
	 * Returns whether or not the given pattern is an expression object method
	 * string.
	 * 
	 * @param pattern The autocomplete pattern to check against.
	 * @return <tt>true</tt> if the pattern matches an expression object method.
	 */
/*	private static boolean isExpressionObjectMethodPattern(String pattern) {

		return EXPRESSION_OBJECT_METHOD_PATTERN.matcher(pattern).matches();
	}
*/
	/**
	 * Returns whether or not the given pattern is a processor name string.
	 * 
	 * @param pattern The autocomplete pattern to check against.
	 * @return <tt>true</tt> if the pattern matches a processor name.
	 */
	private static boolean isProcessorNamePattern(String pattern) {

		return PROCESSOR_NAME_PATTERN.matcher(pattern).matches();
	}
}
