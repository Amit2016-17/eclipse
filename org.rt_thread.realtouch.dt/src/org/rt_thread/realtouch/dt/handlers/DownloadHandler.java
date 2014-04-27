package org.rt_thread.realtouch.dt.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.rt_thread.realtouch.dt.RTDTPlugin;
import org.rt_thread.realtouch.dt.RTUtil;
import org.rt_thread.realtouch.dt.dialogs.DownloadConfigureDialog;

public class DownloadHandler extends AbstractHandler {
    private final static String DESTINATION_PARENT_FOLDER = "/SD/programs/";
    
    private IProject project;
    private String projectName;
    private String moduleFile;
    private String ipAddress;
    private Path commandPath;
    private MessageConsole myConsole;
    MessageConsoleStream out;

    /**
     * The constructor.
     */
    public DownloadHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ipAddress = null;

        myConsole = RTUtil.findConsole(RTUtil.CONSOLE_NAME);
        myConsole.activate();
        out = myConsole.newMessageStream();

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        project = RTUtil.getActiveProject(window);
        if (project == null || !project.isOpen()) {
            out.println("Nothing to download.");
            return null;
        }

        projectName = project.getName();
        moduleFile = projectName + ".mo";
        if (!project.getFile(moduleFile).exists()) {
            out.println("Build project firstly.");
            return null;
        }

        ipAddress = RTUtil.getIpAddress(project);

        if (ipAddress == null) {
            DownloadConfigureDialog dialog = new DownloadConfigureDialog(window.getShell());
            if (dialog.open() == DownloadConfigureDialog.OK) {
                ipAddress = dialog.getIpAddress();

                // save IP address
                try {
                    RTUtil.updateIpAddress(project, ipAddress);
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // Cancel download
                return null;
            }
        }
        
        String commandName;
        if (RTUtil.isWindows()) {
            commandName = RTDTPlugin.WINDOWS_DOWNLOAD_UTIL;
        } else {
            commandName = RTDTPlugin.LINUX_DOWNLOAD_UTIL;
        }

        String command = RTDTPlugin.getConfigPrefs().getString(RTDTPlugin.SDK_LOCATION) + RTDTPlugin.DEFAULT_TOOLS_RELATIVE_FOLDER + "utils/" + commandName ;
        commandPath = new Path(command);
        if (!(new File(command)).exists()) {
            out.println("Error: " + command + " does not exist!");

            return null;
        }

        
        out.println("**** Download for project " + projectName + " ****");

        IRunnableWithProgress runnable = null;
        try {
            runnable = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    String xmlFile = projectName + ".xml";
                    RTUtil.getXmlFile(project);

                    String iconFile = "icon.bmp";
                    String dstFolder = DESTINATION_PARENT_FOLDER + projectName;
                    String[] params = new String[3];
                    params[2] = ipAddress;
                    params[1] = dstFolder;
                    String[] filesToDownload = { moduleFile, xmlFile, iconFile };
                    
                    monitor.beginTask("Download", 100);
                    
                    CommandLauncher launcher = new CommandLauncher();
                    for (int i = 0; i < 3; i++) {
                        try {
                            MessageConsoleStream output = myConsole.newMessageStream();
                            output.println("Download file - " + filesToDownload[i]);
                            params[0] = filesToDownload[i];
                            if (monitor.isCanceled()) {
                                break;
                            }
                            launcher.execute(commandPath, params, null, project.getLocation(), monitor);
                            monitor.worked(15);
                            launcher.waitAndRead(output, output, monitor);
                            monitor.worked(15);
                            
                        } catch (CoreException e) {
                            break;
                        }
                    }
                    
                    if (monitor.isCanceled()) {
                        out.println("**** Download Canceled ****");
                    } else {
                        monitor.done();
                        out.println("**** Download Finished ****");
                    }
                }
             };
            window.run(true, true, runnable);
        } catch (InvocationTargetException e) {
            
        } catch (InterruptedException e) {
        }

        return null;
    }
}