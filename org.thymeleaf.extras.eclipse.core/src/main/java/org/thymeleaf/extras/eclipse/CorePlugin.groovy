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
import org.osgi.framework.BundleContext

/**
 * Plugin activator class for the Thymeleaf core module.
 * 
 * @author Emanuel Rabina
 */
class CorePlugin extends Plugin {

	static final String PLUGIN_ID = "org.thymeleaf.extras.eclipse"

	private static CorePlugin plugin

	private SpringContainer springContainer

	/**
	 * Returns the shared instance of this plugin.
	 * 
	 * @return This plugin instance.
	 */
	static CorePlugin getDefault() {

		return plugin
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void start(BundleContext context) {

		super.start(context)
		plugin = this
		springContainer = SpringContainer.instance
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void stop(BundleContext context) {

		springContainer.close()
		plugin = null
		super.stop(context)
	}
}
