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

package org.thymeleaf.extras.eclipse

import org.eclipse.core.resources.IWorkspace
import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.ui.IWorkbench
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.thymeleaf.extras.eclipse.dialect.cache.DialectChangeListener
import org.thymeleaf.extras.eclipse.dialect.cache.DialectItemProcessor
import org.thymeleaf.extras.eclipse.dialect.cache.DialectTree
import static org.mockito.Mockito.mock

/**
 * Spring configuration for tests.
 * 
 * @author Emanuel Rabina
 */
@Configuration
class TestContentAssistConfig {

	@Bean
	DialectCache dialectCache() {
		return new DialectCache()
	}

	@Bean
	DialectChangeListener dialectChangeListener() {
		return mock(DialectChangeListener)
	}

	@Bean
	DialectItemProcessor dialectItemProcessor() {
		return new DialectItemProcessor()
	}

	@Bean
	DialectTree dialectTree() {
		return new DialectTree()
	}

	@Bean
	ImageRegistry imageRegistry() {
		return mock(ImageRegistry)
	}

	@Bean
	IWorkbench workbench() {
		return mock(IWorkbench)
	}

	@Bean
	IWorkspace workspace() {
		return mock(IWorkspace)
	}

	@Bean
	XmlDialectLoader xmlDialectLoader() {
		return new XmlDialectLoader()
	}
}
