package org.rt_thread.realtouch.dt;

import java.io.ByteArrayInputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class RTUtil {
    public final static String CONSOLE_NAME = "RealTouch Console";

    public static IProject getActiveProject() {
        IWorkbenchWindow window = RTDTPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        return getActiveProject(window);
    }

    public static IProject getActiveProject(IWorkbenchWindow window) {
        IProject activeProject = null;

        ISelectionService selectionService = window.getSelectionService();
        ISelection selection = selectionService.getSelection(IPageLayout.ID_PROJECT_EXPLORER);
        if (selection instanceof StructuredSelection) {
            Object obj = ((StructuredSelection) selection).getFirstElement();

            IResource resource = null;
            if (obj instanceof IResource) {
                resource = (IResource) obj;
            } else if (obj instanceof ICElement) {
                resource = ((ICElement) obj).getResource();
            } else if (obj instanceof IncludeRefContainer) {
                resource = ((IncludeRefContainer) obj).getCProject().getResource();
            }

            if (resource != null) {
                activeProject = resource.getProject();
                if (activeProject != null) {
                    return activeProject;
                }
            }
        }

        IEditorPart editorPart = window.getActivePage().getActiveEditor();
        if (editorPart != null) {
            IEditorInput input = (IEditorInput) editorPart.getEditorInput();
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) input).getFile();
                activeProject = file.getProject();
            }
        }

        return activeProject;
    }

    public static MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++)
            if (name.equals(existing[i].getName()))
                return (MessageConsole) existing[i];
        // no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(name, null);
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static void updateIpAddress(IProject project, String ip) throws CoreException {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
        ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
        IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
        String delimiter = envManager.getDefaultDelimiter();
        IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
        contribEnv.addVariable("IP_ADDRESS", ip, IEnvironmentVariable.ENVVAR_REPLACE, delimiter, desc);

        CoreModel.getDefault().setProjectDescription(project, prjDesc);
    }

    public static String getIpAddress(IProject project) {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
        ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
        IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();

        IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
        IEnvironmentVariable var = contribEnv.getVariable("IP_ADDRESS", desc);

        return (var != null) ? var.getValue() : null;
    }

    public static IFile getXmlFile(IProject project) {
        String projectName = project.getName();
        String xmlFileName = projectName + ".xml";
        IFile xmlFile = project.getFile(xmlFileName);
        if (!xmlFile.exists()) {
            String fileContent = "<?xml version=\\\"1.0\\\"?>\n" + "<name>" + projectName + "</name>\n"
                    + "<image>" + projectName + "/" + "icon.bmp</image>\n";
            try {
                xmlFile.create(new ByteArrayInputStream(fileContent.getBytes()), true, null);
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return xmlFile;
    }

}
