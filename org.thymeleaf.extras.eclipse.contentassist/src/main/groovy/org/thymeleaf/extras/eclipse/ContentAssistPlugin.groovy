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

import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames
import org.osgi.framework.BundleContext
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * Plugin activator class for the Thymeleaf content assist module.
 * 
 * @author Emanuel Rabina
 */
class ContentAssistPlugin extends AbstractUIPlugin {

	static final String PLUGIN_ID = 'org.thymeleaf.extras.eclipse.contentassist'

	static final String IMAGE_THYMELEAF                   = 'thymeleaf'
	static final String IMAGE_ATTRIBUTE_PROCESSOR         = 'attribute-processor'
	static final String IMAGE_ATTRIBUTE_RESTRICTION_VALUE = 'attribute-restriction-value'
	static final String IMAGE_ELEMENT_PROCESSOR           = 'element-processor'
	static final String IMAGE_EXPRESSION_OBJECT_METHOD    = 'expression-object-method'

	private static ContentAssistPlugin plugin

	ApplicationContext applicationContext

	/**
	 * Shortcut to the {@code getBean} method of the underlying application
	 * context.
	 */
	public <T> T getBean(Class<T> requiredType) {

		return plugin.applicationContext.getBean(requiredType)
	}

	/**
	 * Returns the shared instance of this plugin.
	 * 
	 * @return This plugin instance.
	 */
	static ContentAssistPlugin getDefault() {

		return plugin
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		super.initializeImageRegistry(reg)

		reg.put(IMAGE_THYMELEAF,                   imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Thymeleaf.png'))
		reg.put(IMAGE_ATTRIBUTE_PROCESSOR,         imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Attribute-Processor.png'))
		reg.put(IMAGE_ATTRIBUTE_RESTRICTION_VALUE, imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Attribute-Restriction-Value.png'))
		reg.put(IMAGE_ELEMENT_PROCESSOR,           imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Element-Processor.png'))
		reg.put(IMAGE_EXPRESSION_OBJECT_METHOD,    imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Expression-Object-Method.png'))
	}

	@Override
	void start(BundleContext context) {

		super.start(context)
		plugin = this

		// Add the # character to the list of activation characters, then track if
		// it is ever removed by the user so that we know not to put it back again
		// automatically.
		if (plugin.preferenceStore.getBoolean(ContentAssistPreferenceInitializer.AUTO_PROPOSE_PREF)) {
			def htmlUiPrefs = HTMLUIPlugin.getDefault().preferenceStore

			htmlUiPrefs.setValue(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE,
					htmlUiPrefs.getString(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE) + '#')

			htmlUiPrefs.addPropertyChangeListener({ event ->
				if (event.property == HTMLUIPreferenceNames.AUTO_PROPOSE_CODE) {
					if (((String)event.getOldValue()).contains('#') &&
					   !((String)event.getNewValue()).contains('#')) {
						plugin.preferenceStore.setValue(ContentAssistPreferenceInitializer.AUTO_PROPOSE_PREF, false)
					}
				}
			})
		}

		applicationContext = new AnnotationConfigApplicationContext(ContentAssistConfig)
	}

	@Override
	void stop(BundleContext context) {

		plugin = null
		super.stop(context)
	}
}
