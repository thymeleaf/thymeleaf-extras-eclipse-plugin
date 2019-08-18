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

package org.thymeleaf.extras.eclipse

import org.eclipse.core.runtime.Plugin
import org.eclipse.core.runtime.Status
import org.osgi.framework.BundleContext
import org.thymeleaf.extras.eclipse.dialect.cache.DialectCache
import org.thymeleaf.extras.eclipse.template.cache.TemplateCache

/**
 * Plugin activator class for the Thymeleaf core module.
 * 
 * @author Emanuel Rabina
 */
class CorePlugin extends Plugin {

	static final String PLUGIN_ID = "org.thymeleaf.extras.eclipse"

	private static CorePlugin plugin

	private DialectCache dialectCache
	private TemplateCache templateCache

	/**
	 * Returns the shared instance of this plugin.
	 *
	 * @return This plugin instance.
	 */
	static CorePlugin getDefault() {

		return plugin
	}

	/**
	 * Logs an error message to the Eclipse logger.
	 *
	 * @param message
	 * @param throwable
	 */
	static void logError(String message, Throwable throwable) {

		plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, message, throwable));
	}

	/**
	 * Logs an information message to the Eclipse logger.
	 *
	 * @param message
	 */
	static void logInfo(String message) {

		plugin.getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(BundleContext context) throws Exception {

		super.start(context)
		plugin = this
		dialectCache = new DialectCache()
		templateCache = new TemplateCache()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		dialectCache.shutdown()
		templateCache.shutdown()
		plugin = null
		super.stop(context)
	}
}
