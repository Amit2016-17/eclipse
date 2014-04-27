package org.rt_thread.realtouch.dt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.rt_thread.realtouch.dt.RTUtil;
import org.rt_thread.realtouch.dt.dialogs.DownloadConfigureDialog;

public class DownloadConfigureHandler extends AbstractHandler {

    public DownloadConfigureHandler() {

    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IProject project = RTUtil.getActiveProject(window);

        String ipAddress = RTUtil.getIpAddress(project);
        DownloadConfigureDialog dialog = new DownloadConfigureDialog(window.getShell(), ipAddress);
        if (dialog.open() == Dialog.OK) {
            String newIpAddress = dialog.getIpAddress();
            if (ipAddress != null && ipAddress != newIpAddress) {
                // save IP address
                try {
                    RTUtil.updateIpAddress(project, newIpAddress);
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
