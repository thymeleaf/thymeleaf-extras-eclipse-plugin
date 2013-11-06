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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.osgi.framework.BundleContext;

/**
 * Plugin activator class for the Thymeleaf content assist module.
 * 
 * @author Emanuel Rabina
 */
@SuppressWarnings("restriction")
public class ContentAssistPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.thymeleaf.extras.eclipse.contentassist";

	public static final String IMAGE_THYMELEAF                   = "thymeleaf";
	public static final String IMAGE_ATTRIBUTE_PROCESSOR         = "attribute-processor";
	public static final String IMAGE_ATTRIBUTE_RESTRICTION_VALUE = "attribute-restriction-value";
	public static final String IMAGE_ELEMENT_PROCESSOR           = "element-processor";
	public static final String IMAGE_EXPRESSION_OBJECT_METHOD    = "expression-object-method";

	static final String AUTO_PROPOSE_PREF = "autoProposeOn";

	private static ContentAssistPlugin plugin;

	/**
	 * Find the Eclipse project for the file the user is working on.
	 * 
	 * @return The project owning the file the user has open.
	 */
	public static IJavaProject findCurrentJavaProject() {

		IEditorPart editor = getDefault().getWorkbench().getWorkbenchWindows()[0]
				.getActivePage().getActiveEditor();
		IFile file = ((IFileEditorInput)editor.getEditorInput()).getFile();
		IProject project = file.getProject();
		if (isJavaProject(project)) {
			return JavaCore.create(project);
		}
		return null;
	}

	/**
	 * Returns the shared instance of this plugin.
	 *
	 * @return This plugin instance.
	 */
	public static ContentAssistPlugin getDefault() {

		return plugin;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		super.initializeImageRegistry(reg);

		reg.put(IMAGE_THYMELEAF, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/Thymeleaf.png"));
		reg.put(IMAGE_ATTRIBUTE_PROCESSOR, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/Attribute-Processor.png"));
		reg.put(IMAGE_ATTRIBUTE_RESTRICTION_VALUE, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/Attribute-Restriction-Value.png"));
		reg.put(IMAGE_ELEMENT_PROCESSOR, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/Element-Processor.png"));
		reg.put(IMAGE_EXPRESSION_OBJECT_METHOD, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/Expression-Object-Method.png"));
	}

	/**
	 * Check if the given project is a Java project.
	 * 
	 * @param project
	 * @return <tt>true</tt> if the project is a Java project.
	 */
	public static boolean isJavaProject(IProject project) {

		try {
			return project.isNatureEnabled(JavaCore.NATURE_ID);
		}
		catch (CoreException ex) {
			logError("Project not open, or doesn't exist", ex);
		}
		return false;
	}

	/**
	 * Logs an error message to the Eclipse logger.
	 * 
	 * @param message
	 * @param throwable
	 */
	public static void logError(String message, Throwable throwable) {

		plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, message, throwable));
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

		// Add the # character to the list of activation characters, then track if it
		// is ever removed by the user so that we know not to put it back again automatically.
		if (getPreferenceStore().getBoolean(AUTO_PROPOSE_PREF)) {
			IPreferenceStore htmluiprefs = HTMLUIPlugin.getDefault().getPreferenceStore();

			htmluiprefs.setValue(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE,
					htmluiprefs.getString(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE) + "#");

			htmluiprefs.addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE)) {
						if (((String)event.getOldValue()).contains("#") &&
						   !((String)event.getNewValue()).contains("#")) {
							getPreferenceStore().setValue(AUTO_PROPOSE_PREF, false);
						}
					}
				}
			});
		}
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
