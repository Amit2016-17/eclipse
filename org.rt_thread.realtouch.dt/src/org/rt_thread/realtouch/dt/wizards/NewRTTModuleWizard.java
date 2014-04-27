package org.rt_thread.realtouch.dt.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.rt_thread.realtouch.dt.CfgHolder;
import org.rt_thread.realtouch.dt.RTDTPlugin;
import org.rt_thread.realtouch.dt.RTUtil;

@SuppressWarnings("restriction")
public class NewRTTModuleWizard extends BasicNewResourceWizard implements IExecutableExtension,
        IWizardWithMemory, ICDTCommonProjectWizard {

    public static final String EMPTY_STR = "";
    private static final String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";
    private static final String ID = "org.eclipse.cdt.build.core.buildArtefactType.exe";
    private static final String PROPERTY = "org.eclipse.cdt.build.core.buildType";
    private static final String PROP_VAL = PROPERTY + ".debug";
    private IConfigurationElement fConfigElement;
    private WizardNewProjectCreationPage fMainPage;
    private RTTModuleTemplatePage fTemplatePage;
    protected IProject newProject;
    private boolean existingPath = false;
    private String lastProjectName = null;
    private URI lastProjectLocation = null;
    protected IProgressMonitor continueCreationMonitor;
    private static final String[] EMPTY_ARR = new String[0];
    private CfgHolder[] cfgs = null;
    private String sdkPath;
    private IPreferenceStore preference;

    public NewRTTModuleWizard() {
        super();
        setDialogSettings(RTDTPlugin.getDefault().getDialogSettings());
        setNeedsProgressMonitor(true);
        setForcePreviousAndNextButtons(true);
        setWindowTitle("New RT-Thread Project");

        preference = RTDTPlugin.getConfigPrefs();
        sdkPath = preference.getString(RTDTPlugin.SDK_LOCATION);
    }

    @Override
    public void addPages() {
        fMainPage = new WizardNewProjectCreationPage("RTProject");
        fMainPage.setTitle("RT-Thread Module Project");
        fMainPage.setDescription("Create a RT-Thread project");
        addPage(fMainPage);

        fTemplatePage = new RTTModuleTemplatePage();
        addPage(fTemplatePage);
    }

    @Override
    public boolean performFinish() {
        if (getProject(true, true) == null)
            return false;

        // TODO include files
        includeFiles(newProject);

        BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
        selectAndReveal(newProject);

        return true;
    }

    @Override
    public boolean performCancel() {
        clearProject();
        return true;
    }

    /**
     * Remove created project either after error or if user returned back from
     * config page.
     */
    private void clearProject() {
        if (lastProjectName == null)
            return;
        try {
            ResourcesPlugin.getWorkspace().getRoot().getProject(lastProjectName)
                    .delete(!existingPath, true, null);
        } catch (CoreException ignore) {
        }
        newProject = null;
        lastProjectName = null;
        lastProjectLocation = null;
    }

    /**
     * @return true if user has changed settings since project creation
     */
    private boolean isChanged() {
        if (!fMainPage.getProjectName().equals(lastProjectName))
            return true;

        URI projectLocation = getProjectLocation();
        if (projectLocation == null) {
            if (lastProjectLocation != null)
                return true;
        } else if (!projectLocation.equals(lastProjectLocation))
            return true;

        // return savedHandler.isChanged();
        return true;
    }

    @Override
    public IProject createIProject(String name, URI location) throws CoreException {
        return createIProject(name, location, new NullProgressMonitor());
    }

    @Override
    public IProject createIProject(String name, URI location, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask("Create RT-Thread Project", 100);

        if (newProject != null)
            return newProject;

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        final IProject newProjectHandle = root.getProject(name);

        if (!newProjectHandle.exists()) {
            // IWorkspaceDescription workspaceDesc = workspace.getDescription();
            // workspaceDesc.setAutoBuilding(false);
            // workspace.setDescription(workspaceDesc);
            IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
            if (location != null)
                description.setLocationURI(location);
            newProject = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle,
                    new SubProgressMonitor(monitor, 25));
        } else {
            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                }
            };
            workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, new SubProgressMonitor(monitor, 25));
            newProject = newProjectHandle;
        }

        // Open the project if we have to
        if (!newProject.isOpen()) {
            newProject.open(new SubProgressMonitor(monitor, 25));
        }

        continueCreationMonitor = new SubProgressMonitor(monitor, 25);
        IProject proj = continueCreation(newProject);

        monitor.done();

        return proj;
    }

    protected IProject continueCreation(IProject prj) {
        if (continueCreationMonitor == null) {
            continueCreationMonitor = new NullProgressMonitor();
        }
        try {
            copyFiles(newProject);

            this.continueCreationMonitor.beginTask("Add RT-Thread Project Nature", 1);
            CProjectNature.addCNature(prj, new SubProgressMonitor(this.continueCreationMonitor, 1));

            try {
                linkFiles(newProject);
            } catch (CoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (CoreException e) {
        } finally {
            continueCreationMonitor.done();
        }
        return prj;
    }

    private void addAllFiles(IContainer container, File file) throws CoreException, FileNotFoundException {
        if (file.isDirectory()) {
            IFolder folder = container.getFolder(new Path(file.getName()));
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            for (File subFile : file.listFiles()) {
                addAllFiles(folder, subFile);
            }
        } else {
            IFile newFile = container.getFile(new Path(file.getName()));
            if (!newFile.exists()) {
                newFile.create(new FileInputStream(file), true, null);
            }
        }
    }

    private void copyFiles(IProject project) {
        String selectedTemplate = fTemplatePage.getTemplate();

        try {
            // TODO include this header file instead of copy it?
            addAllFiles(project, new File(sdkPath + RTDTPlugin.DEFAULT_BSP_RELATIVE_FOLDER + "rtconfig.h"));
            
            String templatesPath = sdkPath + RTDTPlugin.DEFAULT_TEMPLATES_RELATIVE_FOLDER;
            if (selectedTemplate != null) {
                String templatePath = templatesPath + selectedTemplate;
                File templateFolder = new File(templatePath);
                for (File file : templateFolder.listFiles()) {
                    addAllFiles(project, file);
                }
            }
            addAllFiles(project, new File(templatesPath + "rtconfig.py"));
            addAllFiles(project, new File(templatesPath + "SConstruct"));
            addAllFiles(project, new File(templatesPath + "SConscript"));
            
            if (!project.getFile("icon.bmp").exists()) {
                // Add default icon
                addAllFiles(project, new File(templatesPath + "icon.bmp"));
            }
        } catch (CoreException e) {

        } catch (FileNotFoundException e) {

        }
    }

    private void linkFiles(IProject project) throws CoreException {
        // IWorkspace workspace = ResourcesPlugin.getWorkspace();

        String rtthreadPath = sdkPath + RTDTPlugin.DEFAULT_RT_THREAD_RELATIVE_FOLDER;
        String linkFolder[] = new String[] { "components", "include", "src" };

        IFolder rtthreadFolder = project.getFolder("RT-Thread");
        rtthreadFolder.create(true, false, null);

        for (String folder : linkFolder) {
            String f = rtthreadPath + folder;
            IPath folderPath = new Path(f);
            IFolder containerFolder = rtthreadFolder.getFolder(folder);
            containerFolder.createLink(folderPath, IResource.ALLOW_MISSING_LOCAL, null);
        }

        // Link folder which is relative with CPU
        String libcpu = RTDTPlugin.DEFAULT_BSP_CPULIB_RELATIVE_FOLDER;
        IFolder cpuFolder = rtthreadFolder.getFolder("cpu");
        cpuFolder.createLink(new Path(rtthreadPath + libcpu), IResource.ALLOW_MISSING_LOCAL, null);

    }

    private void includeFiles(IProject project) {
        String rtthreadPath = sdkPath + RTDTPlugin.DEFAULT_RT_THREAD_RELATIVE_FOLDER;
        String includePath =  rtthreadPath + "include";
        ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project,
                true);
        ICConfigurationDescription configDecriptions[] = projectDescription.getConfigurations();
        for (ICConfigurationDescription configDescription : configDecriptions) {
            ICFolderDescription projectRoot = configDescription.getRootFolderDescription();
            ICLanguageSetting[] settings = projectRoot.getLanguageSettings();
            for (ICLanguageSetting setting : settings) {
                List<ICLanguageSettingEntry> includes = new ArrayList<ICLanguageSettingEntry>();
                includes.addAll(setting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH));

                includes.add(new CIncludePathEntry(includePath, ICSettingEntry.BUILTIN));
                includes.add(CDataUtil.createCIncludePathEntry(rtthreadPath + "components/rtgui/include/rtgui", ICSettingEntry.LOCAL));
                setting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, includes);
            }
        }

        try {
            CoreModel.getDefault().setProjectDescription(project, projectDescription);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String[] getContentTypeIDs() {
        return new String[] { CCorePlugin.CONTENT_TYPE_CSOURCE, CCorePlugin.CONTENT_TYPE_CHEADER };
    }

    @Override
    public String[] getExtensions() {
        String[] contentTypeIds = getContentTypeIDs();
        if (contentTypeIds.length > 0) {
            IContentTypeManager manager = Platform.getContentTypeManager();
            List<String> extensions = new ArrayList<String>();
            for (int i = 0; i < contentTypeIds.length; ++i) {
                IContentType contentType = manager.getContentType(contentTypeIds[i]);
                if (null != contentType) {
                    String[] thisTypeExtensions = contentType
                            .getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
                    extensions.addAll(Arrays.asList(thisTypeExtensions));
                }
            }
            return extensions.toArray(new String[extensions.size()]);
        }
        return EMPTY_ARR;
    }

    @Override
    public String[] getLanguageIDs() {
        String[] contentTypeIds = getContentTypeIDs();
        if (contentTypeIds.length > 0) {
            IContentTypeManager manager = Platform.getContentTypeManager();
            List<String> languageIDs = new ArrayList<String>();
            for (int i = 0; i < contentTypeIds.length; ++i) {
                IContentType contentType = manager.getContentType(contentTypeIds[i]);
                if (null != contentType) {
                    ILanguage language = LanguageManager.getInstance().getLanguage(contentType);
                    if (!languageIDs.contains(language.getId())) {
                        languageIDs.add(language.getId());
                    }
                }
            }
            return languageIDs.toArray(new String[languageIDs.size()]);
        }
        return EMPTY_ARR;
    }

    @Override
    public IProject getLastProject() {
        return newProject;
    }

    @Override
    public String[] getNatures() {
        return new String[] { CProjectNature.C_NATURE_ID };
    }

    @Override
    public IProject getProject(boolean defaults) {
        return getProject(defaults, true);
    }

    @Override
    public IProject getProject(boolean defaults, boolean onFinish) {
        if (newProject != null && isChanged())
            clearProject();
        if (newProject == null) {
            existingPath = false;
            try {
                IFileStore fs;
                URI p = getProjectLocation();
                if (p == null) {
                    fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
                    fs = fs.getChild(fMainPage.getProjectName());
                } else
                    fs = EFS.getStore(p);
                IFileInfo f = fs.fetchInfo();
                if (f.exists() && f.isDirectory()) {
                    if (fs.getChild(".project").fetchInfo().exists()) { //$NON-NLS-1$
                        if (!MessageDialog
                                .openConfirm(getShell(),
                                        "Old project will be overridden", //$NON-NLS-1$
                                        "Existing project settings will be overridden.\nImport feature can be used instead to preserve old settings.\nOK to override ?") //$NON-NLS-1$
                        )
                            return null;
                    }
                    existingPath = true;
                }
            } catch (CoreException e) {
                CUIPlugin.log(e.getStatus());
            }
            lastProjectName = fMainPage.getProjectName();
            lastProjectLocation = getProjectLocation();
            // start creation process
            invokeRunnable(getRunnable(defaults, onFinish));
        }
        return newProject;
    }

    private IRunnableWithProgress getRunnable(boolean _defaults, final boolean onFinish) {
        final boolean defaults = _defaults;
        return new IRunnableWithProgress() {
            public void run(IProgressMonitor imonitor) throws InvocationTargetException,
                    InterruptedException {
                final Exception except[] = new Exception[1];
                getShell().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
                                new IRunnableWithProgress() {
                                    public void run(IProgressMonitor monitor)
                                            throws InvocationTargetException, InterruptedException {
                                        final IProgressMonitor fMonitor;
                                        if (monitor == null) {
                                            fMonitor = new NullProgressMonitor();
                                        } else {
                                            fMonitor = monitor;
                                        }
                                        fMonitor.beginTask(CUIPlugin
                                                .getResourceString("CProjectWizard.op_description"), 100); //$NON-NLS-1$
                                        fMonitor.worked(10);
                                        try {
                                            newProject = createIProject(lastProjectName,
                                                    lastProjectLocation, new SubProgressMonitor(fMonitor,
                                                            40));
                                            if (newProject != null)
                                                createProject(newProject, defaults, onFinish,
                                                        new SubProgressMonitor(fMonitor, 40));

                                            ICProjectDescriptionManager mngr = CoreModel.getDefault()
                                                    .getProjectDescriptionManager();
                                            ICProjectDescription des = mngr.createProjectDescription(
                                                    newProject, false);

                                            ManagedProject mProj = new ManagedProject(des);
                                            Configuration cfg = new Configuration(mProj, null,
                                                    "org.rt_thread.realtouch.dt.configuration", "scons");

                                            IBuilder builder = cfg.getEditableBuilder();

                                            String buildCommand;
                                            if (RTUtil.isWindows()) {
                                                buildCommand = RTDTPlugin.WINDOWS_BUILDER;
                                            } else {
                                                buildCommand = RTDTPlugin.LINUX_BUILDER;
                                            }
                                            builder.setCommand(buildCommand);
                                            builder.setIncrementalBuildTarget(RTDTPlugin.BUILD_TARGET);
                                            builder.setCleanBuildTarget(RTDTPlugin.CLEAN_TARGET);
                                            builder.setAutoBuildEnable(false);
//                                            builder.setAutoBuildTarget(RTDTPlugin.BUILD_TARGET);

                                            CConfigurationData data = cfg.getConfigurationData();
                                            des.createConfiguration(
                                                    ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);

                                            // Persist the project description
                                            mngr.setProjectDescription(newProject, des);

                                            /* set Environment Variables */
                                            setEnvironmentVariables(newProject);

                                            fMonitor.worked(10);
                                        } catch (Exception e) {
                                            CUIPlugin.log(e);
                                        } finally {
                                            fMonitor.done();
                                        }
                                    }
                                });
                        try {
                            getContainer().run(false, true, op);
                        } catch (InvocationTargetException e) {
                            except[0] = e;
                        } catch (InterruptedException e) {
                            except[0] = e;
                        }
                    }
                });
                if (except[0] != null) {
                    if (except[0] instanceof InvocationTargetException) {
                        throw (InvocationTargetException) except[0];
                    }
                    if (except[0] instanceof InterruptedException) {
                        throw (InterruptedException) except[0];
                    }
                    throw new InvocationTargetException(except[0]);
                }
            }
        };
    }

    public void createProject(IProject project, boolean defaults, boolean onFinish,
            IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("", 100); //$NON-NLS-1$
            setProjectDescription(project, defaults, onFinish, monitor);
            monitor.worked(30);
        } finally {
            monitor.done();
        }
    }

    private void setProjectDescription(IProject project, boolean defaults, boolean onFinish,
            IProgressMonitor monitor) throws CoreException {
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
        ICProjectDescription des = mngr.createProjectDescription(project, false, !onFinish);
        ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
        monitor.worked(10);
        cfgs = getDefaultCfgs();
        if (cfgs == null || cfgs.length == 0 || cfgs[0].getConfiguration() == null) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            "Convenience method which returns the unique identifier of this plugin", "Cannot create managed project with NULL configuration")); //$NON-NLS-1$
        }
        Configuration cf = (Configuration) cfgs[0].getConfiguration();
        ManagedProject mProj = new ManagedProject(project, cf.getProjectType());
        info.setManagedProject(mProj);
        monitor.worked(10);
        cfgs = CfgHolder.unique(cfgs);
        cfgs = CfgHolder.reorder(cfgs);

        ICConfigurationDescription cfgDebug = null;
        ICConfigurationDescription cfgFirst = null;

        int work = 50 / cfgs.length;

        for (CfgHolder cfg : cfgs) {
            cf = (Configuration) cfg.getConfiguration();
            String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
            Configuration config = new Configuration(mProj, cf, id, false, true);
            CConfigurationData data = config.getConfigurationData();
            ICConfigurationDescription cfgDes = des.createConfiguration(
                    ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
            config.setConfigurationDescription(cfgDes);
            config.exportArtifactInfo();

            IBuilder bld = config.getEditableBuilder();
            if (bld != null) {
                bld.setManagedBuildOn(true);
            }

            config.setName(cfg.getName());
            config.setArtifactName(mProj.getDefaultArtifactName());

            IBuildProperty b = config.getBuildProperties().getProperty(PROPERTY);
            if (cfgDebug == null && b != null && b.getValue() != null
                    && PROP_VAL.equals(b.getValue().getId()))
                cfgDebug = cfgDes;
            if (cfgFirst == null) // select at least first configuration
                cfgFirst = cfgDes;
            monitor.worked(work);
        }

        mngr.setProjectDescription(project, des);
    }

    public static void setEnvironmentVariables(IProject project) throws CoreException {
        String rttRootPath = RTDTPlugin.getConfigPrefs().getString(RTDTPlugin.SDK_LOCATION) + RTDTPlugin.DEFAULT_RT_THREAD_RELATIVE_FOLDER;

        String rttCC = "gcc";

        // String eclipse_path = System.getProperty("eclipse.home.location");
        // eclipse_path = eclipse_path.replace("file:/", "");

        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
        ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
        IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
        String delimiter = envManager.getDefaultDelimiter();
        IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

        /* add or update variables */
        contribEnv.addVariable("RTT_ROOT", rttRootPath, IEnvironmentVariable.ENVVAR_REPLACE, delimiter,
                desc);
        contribEnv.addVariable("RTT_CC", rttCC, IEnvironmentVariable.ENVVAR_REPLACE, delimiter, desc);

        String rttExecPath = RTDTPlugin.getDefault().getPreferenceStore()
                .getString(RTDTPlugin.GCC_LOCATION);
        contribEnv.addVariable("RTT_EXEC_PATH", rttExecPath, IEnvironmentVariable.ENVVAR_REPLACE,
                delimiter, desc);

        String path = System.getenv("PATH");
        path += ";" + rttExecPath;
        if (RTUtil.isWindows()) {
            String pythonPath = RTDTPlugin.getConfigPrefs().getString(RTDTPlugin.PYTHON_LOCATION);
            path += ";" + pythonPath;
            path += ";" + pythonPath + "/Scripts";
        }
        contribEnv.addVariable("PATH", path, IEnvironmentVariable.ENVVAR_REPLACE, delimiter, desc);

        CoreModel.getDefault().setProjectDescription(project, prjDesc);
    }

    private CfgHolder[] getDefaultCfgs() {
        ArrayList<CfgHolder> out = new ArrayList<CfgHolder>();
        CfgHolder[] cfgs = null;
        IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
        IBuildPropertyType bpt = bpm.getPropertyType(ARTIFACT);
        IBuildPropertyValue[] vs = bpt.getSupportedValues();
        Arrays.sort(vs, BuildListComparator.getInstance());
        IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(ARTIFACT, vs[0].getId(), false);
        SortedMap<String, IToolChain> full_tcs = new TreeMap<String, IToolChain>();
        for (int i = 0; i < tcs.length; i++) {
            if (isValid(tcs[i], true, this)) {
                full_tcs.put(tcs[i].getUniqueRealName(), tcs[i]);
            }
        }

        cfgs = CfgHolder.cfgs2items(ManagedBuildManager.getExtensionConfigurations(tcs[0], ARTIFACT, ID));
        for (int j = 0; j < cfgs.length; j++) {
            if (cfgs[j].isSystem() && !cfgs[j].isSupported())
                continue;
            out.add(cfgs[j]);
        }
        return out.toArray(new CfgHolder[out.size()]);
    }

    private boolean invokeRunnable(IRunnableWithProgress runnable) {
        IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
        try {
            getContainer().run(true, true, op);
        } catch (InvocationTargetException e) {
            CUIPlugin.errorDialog(getShell(), "RT Project", "Create RT project of selected type",
                    e.getTargetException(), true);
            clearProject();
            return false;
        } catch (InterruptedException e) {
            clearProject();
            return false;
        }
        return true;
    }

    // Override
    public void dispose() {
        fMainPage.dispose();
        fTemplatePage.dispose();
    }

    // Override
    public boolean canFinish() {
        for (int i = 0; i < getPages().length; i++) {
            if (!getPages()[i].isPageComplete())
                return false;
        }
        return super.canFinish();
    }

    @Override
    public String getLastProjectName() {
        return lastProjectName;
    }

    @Override
    public URI getLastProjectLocation() {
        return lastProjectLocation;
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
            throws CoreException {
        fConfigElement = config;
    }

    /**
     * Checks whether toolchain can be displayed
     * 
     * @param tc
     * @return
     */
    protected boolean isValid(IToolChain tc, boolean supportedOnly, IWizard w) {
        // Check for langiuage compatibility first in any case
        if (!isLanguageCompatible(tc, w))
            return false;

        // Do not do further check if all toolchains are permitted
        if (!supportedOnly)
            return true;

        // Filter off unsupported and system toolchains
        if (tc == null || !tc.isSupported() || tc.isAbstract() || tc.isSystemObject())
            return false;

        // Check for platform compatibility
        return ManagedBuildManager.isPlatformOk(tc);
    }

    /**
     * Checks toolchain for Language ID, Content type ID and Extensions, if they
     * are required by wizard.
     * 
     * @param tc
     *            - toolchain to check
     * @param w
     *            - wizard which provides selection criteria
     * @return
     */
    protected boolean isLanguageCompatible(IToolChain tc, IWizard w) {
        if (w == null)
            return true;
        if (!(w instanceof ICDTCommonProjectWizard))
            return true;

        ITool[] tools = tc.getTools();
        ICDTCommonProjectWizard wz = (ICDTCommonProjectWizard) w;
        String[] langIDs = wz.getLanguageIDs();
        String[] ctypeIDs = wz.getContentTypeIDs();
        String[] exts = wz.getExtensions();

        // nothing requied ?
        if (empty(langIDs) && empty(ctypeIDs) && empty(exts))
            return true;

        for (int i = 0; i < tools.length; i++) {
            IInputType[] its = tools[i].getInputTypes();

            // no input types - check only extensions
            if (empty(its)) {
                if (!empty(exts)) {
                    String[] s = tools[i].getAllInputExtensions();
                    if (contains(exts, s))
                        return true; // extension fits
                }
                continue;
            }
            // normal tool with existing input type(s)
            for (int j = 0; j < its.length; j++) {
                // Check language IDs
                if (!empty(langIDs)) {
                    String lang = its[j].getLanguageId(tools[i]);
                    if (contains(langIDs, new String[] { lang })) {
                        return true; // Language ID fits
                    }
                }
                // Check content types
                if (!empty(ctypeIDs)) {
                    String[] ct1 = its[j].getSourceContentTypeIds();
                    String[] ct2 = its[j].getHeaderContentTypeIds();
                    if (contains(ctypeIDs, ct1) || contains(ctypeIDs, ct2)) {
                        return true; // content type fits
                    }
                }
                // Check extensions
                if (!empty(exts)) {
                    String[] ex1 = its[j].getHeaderExtensions(tools[i]);
                    String[] ex2 = its[j].getSourceExtensions(tools[i]);
                    if (contains(exts, ex1) || contains(exts, ex2)) {
                        return true; // extension fits fits
                    }
                }
            }
        }
        return false; // no one value fits to required
    }

    private boolean empty(Object[] s) {
        return (s == null || s.length == 0);
    }

    private boolean contains(String[] s1, String[] s2) {
        for (int i = 0; i < s1.length; i++)
            for (int j = 0; j < s2.length; j++)
                if (s1[i].equals(s2[j]))
                    return true;
        return false;
    }

    private URI getProjectLocation() {
        return fMainPage.useDefaults() ? null : fMainPage.getLocationURI();
    }
}