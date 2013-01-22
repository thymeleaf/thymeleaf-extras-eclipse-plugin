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

package org.thymeleaf.extras.eclipse.contentassist;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin activator class for the Thymeleaf content assist module.
 * 
 * @author Emanuel Rabina
 */
public class ContentAssistPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.thymeleaf.extras.eclipse.contentassist";

	private static final String PROCESSOR_IMAGE_KEY = "thymeleaf-processor-image";

	private static ContentAssistPlugin plugin;

	/**
	 * Returns the shared instance of this plugin.
	 *
	 * @return This plugin instance.
	 */
	public static ContentAssistPlugin getDefault() {

		return plugin;
	}

	/**
	 * Return the icon used for Thymeleaf processors.
	 * 
	 * @return Thymeleaf processor icon.
	 */
	public static Image getAttributeImage() {

		return plugin.getImageRegistry().get(PROCESSOR_IMAGE_KEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		super.initializeImageRegistry(reg);
		reg.put(PROCESSOR_IMAGE_KEY, imageDescriptorFromPlugin(PLUGIN_ID, "icons/Thymeleaf-Leaf.png"));
	}

	/**
	 * Logs an information message to the Eclipse logger.
	 * 
	 * @param message
	 */
	public static void logInfo(String message) {

		plugin.getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(BundleContext context) throws Exception {

		super.start(context);
		plugin = this;

		// Initialize the processor cache
		ProcessorCache.initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		super.stop(context);
	}
}
