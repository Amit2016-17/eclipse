package org.rt_thread.realtouch.dt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.rt_thread.realtouch.dt.dialogs.PreferenceDialog;

public class PreferenceHandler extends AbstractHandler {

    public PreferenceHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        PreferenceDialog dialog = new PreferenceDialog(window.getShell());
        dialog.open();
        return null;
    }

}
