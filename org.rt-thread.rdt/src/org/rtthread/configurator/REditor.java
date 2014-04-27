package org.rtthread.configurator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.osgi.framework.Bundle;
import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.uitl.ParseUtil;

/**
 * RT-Thread Configurator Editor
 * 
 * @author RDT Team
 * @date 2011-9-1
 */
public class REditor extends MultiPageEditorPart implements IResourceChangeListener {

	/** The text editor used in page 0. */
	private CEditor editor;

	private TreeViewer treeViewer;

	private List<Config> configArray = new ArrayList<Config>();

	/**
	 * Tells whether this editor has been activated at least once.
	 */
	private boolean fHasBeenActivated = false;

	/**
	 * Cached modification stamp of the editor's input.
	 * 
	 * @since 2.0
	 */
	private long fModificationStamp = -1;

	/**
	 * Indicates whether sanity checking in enabled.
	 */
	private boolean fIsSanityCheckEnabled = true;

	/**
	 * The editor's activation listener.
	 */
	private ActivationListener fActivationListener;

	public REditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public List<Config> getConfigArray() {
		return configArray;
	}

	public void updateConfig(Config config) {
		for (Config item : configArray) {
			if (item.getName().equals(config.getName())){
				item.setValue(config.getValue());
				return;
			}
		}
	}

	/**
	 * Creates page 0 of the multi-page editor, which contains a text editor.
	 */
	void createPage0() {
		try {
			editor = new CEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, "Text Editor");
			String editorText = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
			ParseUtil parse = new ParseUtil();
			parse.parseConfig(editorText);
			configArray = parse.getConList();
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * Creates page 1 of the multi-page editor, which shows the sorted text.
	 */
	void createPage1() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);

		createTreeViewer(composite);

		int index = addPage(composite);
		setPageText(index, "Configuration Wizard");
	}

	/**
	 * @param composite
	 */
	private void createTreeViewer(Composite composite) {
		treeViewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(composite, treeViewer);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setInput(configArray);

		// Make the selection available
		getSite().setSelectionProvider(treeViewer);

		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(gridData);
	}

	/**
	 * @param composite
	 * @param treeViewer
	 */
	private void createColumns(Composite composite, TreeViewer treeViewer) {
		String[] titles = { "name", "value" };
		int[] bounds = { 400, 100 };
		for (int i = 0; i < titles.length; i++) {
			final TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
			final TreeColumn column = viewerColumn.getColumn();
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
			viewerColumn.setEditingSupport(new ConfigEditingSupport(this, treeViewer, i));
		}
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
	 */
	// Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			String editorText = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
			ParseUtil parse = new ParseUtil();
			parse.parseConfig(editorText);
			configArray = parse.getConList();
			treeViewer.getContentProvider().inputChanged(treeViewer, treeViewer.getInput(), configArray);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
	 * .eclipse.core.resources.IResourceChangeEvent)
	 */
	// Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	// Override
	protected void createPages() {
		createPage0();
		createPage1();
		
		this.setPartName(String.format("Configurator[%s]", 
				editor.getEditorInput().getName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	// Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	// Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	// Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (fActivationListener != null) {
			fActivationListener.dispose();
			fActivationListener = null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	// Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
		fActivationListener = new ActivationListener(site.getWorkbenchWindow().getPartService());
	}

	public TextEditor getEditor() {
		return (TextEditor)editor;
	}

	/**
	 * Internal part and shell activation listener for triggering state
	 * validation.
	 * 
	 * @since 2.0
	 */
	class ActivationListener implements IPartListener, IWindowListener {

		/** Cache of the active workbench part. */
		private IWorkbenchPart fActivePart;
		/** Indicates whether activation handling is currently be done. */
		private boolean fIsHandlingActivation = false;
		/**
		 * The part service.
		 * 
		 * @since 3.1
		 */
		private IPartService fPartService;

		/**
		 * Creates this activation listener.
		 * 
		 * @param partService
		 *            the part service on which to add the part listener
		 * @since 3.1
		 */
		public ActivationListener(IPartService partService) {
			fPartService = partService;
			fPartService.addPartListener(this);
			PlatformUI.getWorkbench().addWindowListener(this);
		}

		/**
		 * Disposes this activation listener.
		 * 
		 * @since 3.1
		 */
		public void dispose() {
			fPartService.removePartListener(this);
			PlatformUI.getWorkbench().removeWindowListener(this);
			fPartService = null;
		}

		/*
		 * @see IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			fActivePart = part;
			handleActivation();
		}

		/*
		 * @see IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
			fActivePart = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.
		 * IWorkbenchWindow)
		 */
		// Override
		public void windowActivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow()) {
				/*
				 * Workaround for problem described in
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11731 Will be
				 * removed when SWT has solved the problem.
				 */
				window.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						handleActivation();
					}
				});
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.
		 * IWorkbenchWindow)
		 */
		// Override
		public void windowDeactivated(IWorkbenchWindow window) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.
		 * IWorkbenchWindow)
		 */
		// Override
		public void windowClosed(IWorkbenchWindow window) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.
		 * IWorkbenchWindow)
		 */
		// Override
		public void windowOpened(IWorkbenchWindow window) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart
		 * )
		 */
		// Override
		public void partOpened(IWorkbenchPart part) {

		}

		/**
		 * Handles the activation triggering a element state check in the
		 * editor.
		 */
		private void handleActivation() {
			if (fIsHandlingActivation)
				return;

			if (fActivePart == REditor.this) {
				fIsHandlingActivation = true;
				try {
					safelySanityCheckState(getEditorInput());
				} finally {
					fIsHandlingActivation = false;
					fHasBeenActivated = true;
				}
			}
		}

		/**
		 * Checks the state of the given editor input if sanity checking is
		 * enabled.
		 * 
		 * @param input
		 *            the editor input whose state is to be checked
		 * @since 2.0
		 */
		private void safelySanityCheckState(IEditorInput input) {
			boolean enabled = false;

			synchronized (this) {
				enabled = fIsSanityCheckEnabled;
			}

			if (enabled)
				sanityCheckState(input);
		}

		/**
		 * Checks the state of the given editor input.
		 * 
		 * @param input
		 *            the editor input whose state is to be checked
		 * @since 2.0
		 */
		private void sanityCheckState(IEditorInput input) {
			IDocumentProvider p = editor.getDocumentProvider();
			if (p == null)
				return;

			if (p instanceof IDocumentProviderExtension3) {

				IDocumentProviderExtension3 p3 = (IDocumentProviderExtension3) p;

				long stamp = p.getModificationStamp(input);
				if (stamp != fModificationStamp) {
					fModificationStamp = stamp;
					if (!p3.isSynchronized(input))
						handleEditorInputChanged();
				}

			} else {

				if (fModificationStamp == -1)
					fModificationStamp = p.getSynchronizationStamp(input);

				long stamp = p.getModificationStamp(input);
				if (stamp != fModificationStamp) {
					fModificationStamp = stamp;
					if (stamp != p.getSynchronizationStamp(input))
						handleEditorInputChanged();
				}
			}

			updateState(getEditorInput());
		}
	}

	private void updateState(IEditorInput input) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension = (IDocumentProviderExtension) provider;
			try {
				extension.updateStateCache(input);
			} catch (CoreException e) {
				Bundle bundle = Platform.getBundle(REditorPlugin.PLUGIN_ID);
				ILog log = Platform.getLog(bundle);
				log.log(e.getStatus());
			}
		}
	}

	/*
	 * @see ITextEditor#close
	 */
	public void close(final boolean save) {
		getSite().getPage().closeEditor(REditor.this, save);
	}

	/**
	 * Handles an external change of the editor's input element. Subclasses may
	 * extend.
	 */
	protected void handleEditorInputChanged() {
		String title;
		String msg;
		Shell shell = getSite().getShell();

		final IDocumentProvider provider = editor.getDocumentProvider();
		if (provider == null) {
			// fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=15066
			close(false);
			return;
		}

		final IEditorInput input = getEditorInput();
		final String inputName = input.getToolTipText();

		if (provider.isDeleted(input)) {

			if (isSaveAsAllowed()) {

				title = "File Not Accessible";
				msg = "The file " + getPartName() + " has been deleted or is not accessible. Do you want to save your changes or close  the editor without saving";

				String[] buttons = { "Save", "Close", };

				MessageDialog dialog = new MessageDialog(shell, title, null, msg, MessageDialog.QUESTION, buttons, 0);

				if (dialog.open() == 0) {
					IProgressMonitor pm = getProgressMonitor();
					performSaveAs(pm);
					if (pm.isCanceled())
						handleEditorInputChanged();
				} else {
					close(false);
				}

			} else {

				title = "File Not Accessible";
				msg = "The file " + inputName + " has been deleted or is not accessible. This editor will be closed.";
				if (MessageDialog.openConfirm(shell, title, msg))
					close(false);
			}

		} else {

			title = "File Changed";
			msg = "The file " + inputName + " has been changed on the file system. Do you want to replace the editor contents with these changes?";

			if (fHasBeenActivated && MessageDialog.openQuestion(shell, title, msg)) {

				try {
					if (provider instanceof IDocumentProviderExtension) {
						IDocumentProviderExtension extension = (IDocumentProviderExtension) provider;
						extension.synchronize(input);
					} else {
						doSetInput(input);
					}
				} catch (CoreException x) {
					IStatus status = x.getStatus();
					if (status == null || status.getSeverity() != IStatus.CANCEL) {
						title = "Problems loading File";
						msg = "The file" + inputName + "could not be loaded from the file system.";
						ErrorDialog.openError(shell, title, msg, x.getStatus());
					}
				}
			}
		}
	}

	private void doSetInput(IEditorInput input) throws CoreException {
		editor.updatePartControl(input);
	}

	/**
	 * Returns the progress monitor related to this editor. It should not be
	 * necessary to extend this method.
	 * 
	 * @return the progress monitor related to this editor
	 * @since 2.1
	 */
	private IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm = null;

		IStatusLineManager manager = getStatusLineManager();
		if (manager != null)
			pm = manager.getProgressMonitor();

		return pm != null ? pm : new NullProgressMonitor();
	}

	/**
	 * Returns the status line manager of this editor.
	 * 
	 * @return the status line manager of this editor
	 * @since 2.0, protected since 3.3
	 */
	protected IStatusLineManager getStatusLineManager() {
		return getEditorSite().getActionBars().getStatusLineManager();
	}

	/**
	 * This implementation asks the user for the workspace path of a file
	 * resource and saves the document there.
	 * 
	 * @param progressMonitor
	 *            the progress monitor to be used
	 * @since 3.2
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		// Shell shell= getSite().getShell();
		// final IEditorInput input= getEditorInput();
		//
		// IDocumentProvider provider= editor.getDocumentProvider();
		// final IEditorInput newInput;

		// if (input instanceof IURIEditorInput && !(input instanceof
		// IFileEditorInput)) {
		// FileDialog dialog= new FileDialog(shell, SWT.SAVE);
		// IPath oldPath= URIUtil.toPath(((IURIEditorInput)input).getURI());
		// if (oldPath != null) {
		// dialog.setFileName(oldPath.lastSegment());
		// dialog.setFilterPath(oldPath.toOSString());
		// }
		//
		// String path= dialog.open();
		// if (path == null) {
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(true);
		// return;
		// }
		//
		// // Check whether file exists and if so, confirm overwrite
		// final File localFile= new File(path);
		// if (localFile.exists()) {
		// MessageDialog overwriteDialog= new MessageDialog(
		// shell,
		// "Problems During Save As...",
		// null,
		// NLSUtility.format(editor.getPartName(), path),
		// MessageDialog.WARNING,
		// new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL
		// },
		// 1); // 'No' is the default
		// if (overwriteDialog.open() != Window.OK) {
		// if (progressMonitor != null) {
		// progressMonitor.setCanceled(true);
		// return;
		// }
		// }
		// }
		//
		// IFileStore fileStore;
		// try {
		// fileStore= EFS.getStore(localFile.toURI());
		// } catch (CoreException ex) {
		// EditorsPlugin.log(ex.getStatus());
		// String title=
		// TextEditorMessages.AbstractDecoratedTextEditor_error_saveAs_title;
		// String msg=
		// NLSUtility.format(TextEditorMessages.AbstractDecoratedTextEditor_error_saveAs_message,
		// ex.getMessage());
		// MessageDialog.openError(shell, title, msg);
		// return;
		// }
		//
		// IFile file= getWorkspaceFile(fileStore);
		// if (file != null)
		// newInput= new FileEditorInput(file);
		// else
		// newInput= new FileStoreEditorInput(fileStore);

		// } else {
		// SaveAsDialog dialog= new SaveAsDialog(shell);
		//
		// IFile original= (input instanceof IFileEditorInput) ?
		// ((IFileEditorInput) input).getFile() : null;
		// if (original != null)
		// dialog.setOriginalFile(original);
		//
		// dialog.create();
		//
		// if (provider.isDeleted(input) && original != null) {
		// String message=
		// NLSUtility.format(TextEditorMessages.AbstractDecoratedTextEditor_warning_saveAs_deleted,
		// original.getName());
		// dialog.setErrorMessage(null);
		// dialog.setMessage(message, IMessageProvider.WARNING);
		// }
		//
		// if (dialog.open() == Window.CANCEL) {
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(true);
		// return;
		// }
		//
		// IPath filePath= dialog.getResult();
		// if (filePath == null) {
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(true);
		// return;
		// }
		//
		// IWorkspace workspace= ResourcesPlugin.getWorkspace();
		// IFile file= workspace.getRoot().getFile(filePath);
		// newInput= new FileEditorInput(file);
		//
		// }

		// if (provider == null) {
		// // editor has programmatically been closed while the dialog was open
		// return;
		// }
		//
		// boolean success= false;
		// try {
		//
		// provider.aboutToChange(newInput);
		// provider.saveDocument(progressMonitor, newInput,
		// provider.getDocument(input), true);
		// success= true;
		//
		// } catch (CoreException x) {
		// final IStatus status= x.getStatus();
		// if (status == null || status.getSeverity() != IStatus.CANCEL) {
		// String title= "Problems During Save As...";
		// String msg= "Save could not be completed. " + x.getMessage();
		// MessageDialog.openError(shell, title, msg);
		// }
		// } finally {
		// // provider.changed(newInput);
		// // if (success)
		// // setInput(newInput);
		// }
		//
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(!success);
	}

}
