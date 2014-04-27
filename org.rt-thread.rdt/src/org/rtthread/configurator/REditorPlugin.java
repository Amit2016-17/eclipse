/*******************************************************************************
 * Copyright (c) 2005-2011, Chinese Eclipse Community(CEC) All rights reserved. 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.ceclipse.org
 *
 * Contributors:
 *   Ming.He <heming@ceclipse.com> - initial API and implementation 
 *******************************************************************************/
package org.rtthread.configurator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class REditorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.rt-thread.configurator"; //$NON-NLS-1$

	// The shared instance
	private static REditorPlugin plugin;
	
	/**
	 * The constructor
	 */
	public REditorPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static REditorPlugin getDefault() {
		return plugin;
	}
	
	/**  
     * Returns an image descriptor for the image file at the given  
     * plug-in relative path  
     *  
     * @param path the path  
     * @return the image descriptor  
     */  
    public static ImageDescriptor getImageDescriptor(String path) {   
        return imageDescriptorFromPlugin(PLUGIN_ID, path);   
    }

}
