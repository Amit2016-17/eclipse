package org.rt_thread.realtouch.dt;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RTDTPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.rt_thread.realtouch.dt";
    public static final String REALTOUCH_LOCATION = PLUGIN_ID + ".location.realtouchsdk";
    public static final String RT_THREAD_LOCATION = PLUGIN_ID + ".locations.rt_thread";
    public static final String TOOLCHAIN_LOCATION = PLUGIN_ID + ".locations.toolchain";
    public static final String SDK_LOCATION = PLUGIN_ID + ".locations.sdk";
    public static final String GCC_LOCATION = PLUGIN_ID + ".locations.gcc";
    public static final String PYTHON_LOCATION = PLUGIN_ID + ".locations.python";
    public static final String USE_TOOLS_WITHIN_SDK = PLUGIN_ID + ".use_tools_within_sdk";

    public static final String DEFAULT_RT_THREAD_RELATIVE_FOLDER = "/rt-thread/";
    public static final String DEFAULT_BSP_RELATIVE_FOLDER = "/rt-thread/realtouch/";
    public static final String DEFAULT_BSP_CPULIB_RELATIVE_FOLDER = "/libcpu/arm/cortex-m4/";
    public static final String DEFAULT_TEMPLATES_RELATIVE_FOLDER = "/rt-thread/templates/";
    public static final String DEFAULT_TOOLS_RELATIVE_FOLDER = "/tools/";
    public static final String DEFAULT_GCC_RELATIVE_FOLDER = DEFAULT_TOOLS_RELATIVE_FOLDER + "gcc-arm-none-eabi/bin/";
    public static final String DEFAULT_PYTHON_RELATIVE_FOLDER = DEFAULT_TOOLS_RELATIVE_FOLDER + "python/";
    public static final String WINDOWS_DOWNLOAD_UTIL = "download_util.exe";
    public static final String LINUX_DOWNLOAD_UTIL = "download_util.py";
    public static final String WINDOWS_BUILDER = "scons.bat";
    public static final String LINUX_BUILDER = "scons";
    public static final String BUILD_TARGET = "-j2";
    public static final String CLEAN_TARGET = "-c";
    
    // The shared instance
    private static RTDTPlugin plugin;

    public static IPreferenceStore getConfigPrefs() {
        return getDefault().getPreferenceStore();
        // return PlatformUI.getPreferenceStore();
    }

    /**
     * The constructor
     */
    public RTDTPlugin() {
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        IPreferenceStore preference = getConfigPrefs();
        String sdkPath = preference.getString(SDK_LOCATION);
        if (sdkPath == null || sdkPath.equals("")) {
            String eclipsePath= System.getProperty("eclipse.home.location");
            if (RTUtil.isWindows()) {
                eclipsePath = eclipsePath.replace("file:/", "");
            } else {
                eclipsePath = eclipsePath.replace("file:", "");
            }
            String eclipseParentPath = new File(eclipsePath).getParent();
            sdkPath = eclipseParentPath;
            preference.setValue(USE_TOOLS_WITHIN_SDK, true);
            preference.setValue(SDK_LOCATION, sdkPath);
            preference.setValue(GCC_LOCATION, sdkPath + "/tools/gcc-arm-none-eabi/bin");
            preference.setValue(PYTHON_LOCATION, sdkPath + "/tools/python");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
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
    public static RTDTPlugin getDefault() {
        return plugin;
    }

    public File getConfigDir() {
        Location location = Platform.getConfigurationLocation();
        if (location != null) {
            URL configURL = location.getURL();
            if (configURL != null && configURL.getProtocol().startsWith("file")) {
                return new File(configURL.getFile(), PLUGIN_ID);
            }
        }

        // If the configuration location is read-only,
        // then return an alternate location
        // rather than null or throwing an Exception.
        return getStateLocation().toFile();
    }

}
