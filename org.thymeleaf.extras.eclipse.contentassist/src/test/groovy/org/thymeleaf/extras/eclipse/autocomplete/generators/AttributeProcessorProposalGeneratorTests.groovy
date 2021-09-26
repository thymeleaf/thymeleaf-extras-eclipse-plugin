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

package org.thymeleaf.extras.eclipse.autocomplete.generators

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.thymeleaf.extras.eclipse.TestContentAssistConfig
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.*

import groovy.xml.DOMBuilder
import javax.inject.Inject

/**
 * Tests for the {@link AttributeProcessorProposalGenerator}, which comes up
 * with autocomplete entries for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
@SpringJUnitConfig(classes = [TestContentAssistConfig, AttributeProcessorProposalGeneratorConfig.class])
class AttributeProcessorProposalGeneratorTests {

	@Configuration
	static class AttributeProcessorProposalGeneratorConfig {

		@Bean
		AttributeProcessorProposalGenerator attributeProcessorProposalGenerator() {
			return new AttributeProcessorProposalGenerator()
		}

		@Bean
		DialectCache dialectCache() {
			def dialectCache = mock(DialectCache)
			when(dialectCache.getAttributeProcessors(any(), any(), any())).thenReturn([])
			return dialectCache
		}
	}

	@Inject
	private final AttributeProcessorProposalGenerator attributeProcessorProposalGenerator
	@Inject
	private final IWorkbench workbench

	/**
	 * Create an {@link IDocument} mock that operates over the given text.
	 * 
	 * @param documentText
	 * @return
	 */
	private static IDocument createDocument(String documentText) {

		def document = mock(IDocument)
		when(document.getChar(anyInt())).thenAnswer({ invocation ->
			def index = invocation.getArgument(0)
			return documentText.charAt(index)
		})
		when(document.get(anyInt(), anyInt())).thenAnswer({ invocation ->
			def (position, length) = invocation.arguments
			return documentText.substring(position, position + length)
		})
		return document
	}

	@Test
	void mock() {

		def results = attributeProcessorProposalGenerator.generate(
			mock(IDOMNode), mock(ITextRegion), mock(IStructuredDocumentRegion),
			mock(IStructuredDocument), 0)
		assertEquals(results, [])
	}

	@Test
	void returnsProposalsAtElementWhitespace() {

		// TODO: See the note in IWorkbenchExtensions
		def project = mock(IProject)
		def file = mock(IFile)
		when(file.getProject()).thenReturn(project)
		def editorInput = mock(FileEditorInput)
		when(editorInput.getFile()).thenReturn(file)
		def activeEditor = mock(IEditorPart)
		when(activeEditor.getEditorInput()).thenReturn(editorInput)
		def workbenchPage = mock(IWorkbenchPage)
		when(workbenchPage.getActiveEditor()).thenReturn(activeEditor)
		def workbenchWindow = mock(IWorkbenchWindow)
		when(workbenchWindow.getActivePage()).thenReturn(workbenchPage)
		when(workbench.getWorkbenchWindows()).thenReturn(new IWorkbenchWindow[]{ workbenchWindow })

		def html = '<p xmlns:th="http://www.thymeleaf.org" >Hi!</p>'
		def document = DOMBuilder.newInstance().parseText(html)

		def results = attributeProcessorProposalGenerator.generate(document.firstChild, mock(ITextRegion),
			mock(IStructuredDocumentRegion), createDocument(html), 39)
		assertEquals(results, [])
	}
}
