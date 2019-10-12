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

package org.thymeleaf.extras.eclipse.contentassist

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.Status
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames
import org.osgi.framework.BundleContext

/**
 * Plugin activator class for the Thymeleaf content assist module.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings('restriction')
class ContentAssistPlugin extends AbstractUIPlugin {

	static final String PLUGIN_ID = 'org.thymeleaf.extras.eclipse.contentassist'

	static final String IMAGE_THYMELEAF                   = 'thymeleaf'
	static final String IMAGE_ATTRIBUTE_PROCESSOR         = 'attribute-processor'
	static final String IMAGE_ATTRIBUTE_RESTRICTION_VALUE = 'attribute-restriction-value'
	static final String IMAGE_ELEMENT_PROCESSOR           = 'element-processor'
	static final String IMAGE_EXPRESSION_OBJECT_METHOD    = 'expression-object-method'

	private static ContentAssistPlugin plugin

	/**
	 * Find the Eclipse project for the file the user is working on.
	 * 
	 * @return The project owning the file the user has open.
	 */
	static IJavaProject findCurrentJavaProject() {

		def editor = plugin.workbench.workbenchWindows[0].activePage.activeEditor
		def file = editor.editorInput.file
		def project = file.project
		if (project.isNatureEnabled(JavaCore.NATURE_ID)) {
			return JavaCore.create(project)
		}
		return null
	}

	/**
	 * Returns the shared instance of this plugin.
	 * 
	 * @return This plugin instance.
	 */
	static ContentAssistPlugin getDefault() {

		return plugin
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		super.initializeImageRegistry(reg)

		reg.put(IMAGE_THYMELEAF,                   imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Thymeleaf.png'))
		reg.put(IMAGE_ATTRIBUTE_PROCESSOR,         imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Attribute-Processor.png'))
		reg.put(IMAGE_ATTRIBUTE_RESTRICTION_VALUE, imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Attribute-Restriction-Value.png'))
		reg.put(IMAGE_ELEMENT_PROCESSOR,           imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Element-Processor.png'))
		reg.put(IMAGE_EXPRESSION_OBJECT_METHOD,    imageDescriptorFromPlugin(PLUGIN_ID, 'icons/Expression-Object-Method.png'))
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void start(BundleContext context) {

		super.start(context)
		plugin = this

		// Add the # character to the list of activation characters, then track if
		// it is ever removed by the user so that we know not to put it back again
		// automatically.
		if (plugin.preferenceStore.getBoolean(AUTO_PROPOSE_PREF)) {
			def htmlUiPrefs = HTMLUIPlugin.getDefault().preferenceStore

			htmlUiPrefs.setValue(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE,
					htmlUiPrefs.getString(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE) + '#')

			htmlUiPrefs.addPropertyChangeListener({ event ->
				if (event.property == HTMLUIPreferenceNames.AUTO_PROPOSE_CODE) {
					if (((String)event.getOldValue()).contains('#') &&
					   !((String)event.getNewValue()).contains('#')) {
						plugin.preferenceStore.setValue(AUTO_PROPOSE_PREF, false)
					}
				}
			})
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void stop(BundleContext context) {

		plugin = null
		super.stop(context)
	}
}
