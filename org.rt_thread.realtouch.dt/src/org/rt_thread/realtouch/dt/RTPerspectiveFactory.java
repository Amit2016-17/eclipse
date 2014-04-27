package org.rt_thread.realtouch.dt;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class RTPerspectiveFactory implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.2f, editorArea);
        IFolderLayout bottomFolder = layout.createFolder("folder.bottom", IPageLayout.BOTTOM, 0.8f,
                editorArea);
        bottomFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.8f, editorArea);
    }

}
