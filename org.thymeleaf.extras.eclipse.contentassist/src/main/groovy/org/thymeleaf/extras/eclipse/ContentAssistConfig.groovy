/* 
 * Copyright 2019, The Thymeleaf Project (http://www.thymeleaf.org/)
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
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.IWorkbench
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.thymeleaf.extras.eclipse.dialect.XmlDialectLoader
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache

/**
 * Spring configuration for the core part of the plugin.
 * 
 * @author Emanuel Rabina
 */
@Configuration
@ComponentScan
class ContentAssistConfig {

	@Bean
	ImageRegistry imageRegistry() {
		return ContentAssistPlugin.default.imageRegistry
	}

	@Bean
	IWorkbench workbench() {
		return PlatformUI.workbench
	}

	@Bean
	IWorkspace workspace() {
		return ResourcesPlugin.workspace
	}
}
