package org.rt_thread.realtouch.dt.dialogs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rt_thread.realtouch.dt.RTDTPlugin;
import org.rt_thread.realtouch.dt.RTUtil;

public class PreferenceDialog extends Dialog {
    private static boolean selectPythonPath = RTUtil.isWindows();
    private Text sdkPathText;
    private Text gccPathText;
    private Text pythonPathText;
    private Button sdkBrowseButton;
    private Button gccBrowseButton;
    private Button useDefaultButton;
    private Button pythonBrowseButton;
    private Label pythonPathLabel;
    private Label gccPathLabel;
    private IPreferenceStore preference;

    private SelectionAdapter buttonSelectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
            String selectedLocation;

            Object source = event.getSource();
            if ((Button) source == sdkBrowseButton) {
                selectedLocation = handleLocationBrowseButtonPressed(sdkPathText.getText());
                if (selectedLocation != null) {
                    sdkPathText.setText(selectedLocation);

                    if (useDefaultButton.getSelection()) {
                        /**
                         * Use tools within SDK
                         */
                        gccPathText.setText(selectedLocation + RTDTPlugin.DEFAULT_GCC_RELATIVE_FOLDER);
                        if (selectPythonPath) {
                            pythonPathText.setText(selectedLocation + RTDTPlugin.DEFAULT_PYTHON_RELATIVE_FOLDER);
                        }
                    }
                }
            } else if ((Button) source == gccBrowseButton) {
                selectedLocation = handleLocationBrowseButtonPressed(gccPathText.getText());
                if (selectedLocation != null) {
                    gccPathText.setText(selectedLocation);
                }
            } else if (selectPythonPath && ((Button) source == pythonBrowseButton)) {
                selectedLocation = handleLocationBrowseButtonPressed(pythonPathText.getText());
                if (selectedLocation != null) {
                    pythonPathText.setText(selectedLocation);
                }
            }
        }
    };

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public PreferenceDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.TITLE);

        preference = RTDTPlugin.getConfigPrefs();
    }

    public IPath getsdkPathText() {
        String path = sdkPathText.getText();
        return new Path(path);
    }

    public IPath getgccPathText() {
        String path = gccPathText.getText();
        return new Path(path);
    }

    /**
     * Configure the dialog
     * 
     * @param shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("RealTouch");
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginTop = 10;
        gl_composite.marginRight = 10;
        gl_composite.marginLeft = 10;
        composite.setLayout(gl_composite);

        Label lblRtthreadRoot = new Label(composite, SWT.NONE);
        lblRtthreadRoot.setText("SDK Path:");

        sdkPathText = new Text(composite, SWT.BORDER);
        sdkPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        sdkBrowseButton = new Button(composite, SWT.NONE);
        sdkBrowseButton.setText("Browse...");
        sdkBrowseButton.addSelectionListener(buttonSelectionListener);

        useDefaultButton = new Button(composite, SWT.CHECK);
        useDefaultButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        useDefaultButton.setText("Use tools of SDK");
        useDefaultButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                boolean selected = useDefaultButton.getSelection();
                setToolsSelection(!selected);
                if (selected) {
                    String sdkPath = sdkPathText.getText();
                    gccPathText.setText(sdkPath);
                    gccPathText.setText(sdkPath + RTDTPlugin.DEFAULT_GCC_RELATIVE_FOLDER);
                    if (selectPythonPath) {
                        pythonPathText.setText(sdkPath + RTDTPlugin.DEFAULT_PYTHON_RELATIVE_FOLDER);
                    }
                }
            }
        });

        gccPathLabel = new Label(composite, SWT.NONE);
        gccPathLabel.setText("GCC Path:");

        gccPathText = new Text(composite, SWT.BORDER);
        gccPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        gccBrowseButton = new Button(composite, SWT.NONE);
        gccBrowseButton.setText("Browse...");
        gccBrowseButton.addSelectionListener(buttonSelectionListener);

        if (selectPythonPath) {

            pythonPathLabel = new Label(composite, SWT.NONE);
            pythonPathLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            pythonPathLabel.setText("Python Path:");

            pythonPathText = new Text(composite, SWT.BORDER);
            pythonPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

            pythonBrowseButton = new Button(composite, SWT.NONE);
            pythonBrowseButton.setEnabled(false);
            pythonBrowseButton.setText("Browse...");
            pythonBrowseButton.addSelectionListener(buttonSelectionListener);

            pythonPathText.setText(preference.getString(RTDTPlugin.PYTHON_LOCATION));
        }

        // Initial
        sdkPathText.setText(preference.getString(RTDTPlugin.SDK_LOCATION));
        gccPathText.setText(preference.getString(RTDTPlugin.GCC_LOCATION));
        boolean useDefault = preference.getBoolean(RTDTPlugin.USE_TOOLS_WITHIN_SDK);
        useDefaultButton.setSelection(useDefault);
        setToolsSelection(!useDefault);

        return composite;
    }

    private void setToolsSelection(boolean enabled) {
        gccPathLabel.setEnabled(enabled);
        gccPathText.setEnabled(enabled);
        gccBrowseButton.setEnabled(enabled);

        if (selectPythonPath) {
            pythonPathLabel.setEnabled(enabled);
            pythonPathText.setEnabled(enabled);
            pythonBrowseButton.setEnabled(enabled);
        }
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(600, 320);
    }

    /**
     * Open an appropriate directory browser
     */
    private String handleLocationBrowseButtonPressed(String path) {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
        dialog.setMessage("Select the location of RealTouch SDK");

        dialog.setFilterPath(path);
        String selectedPath = dialog.open();

        return selectedPath;
    }

    @Override
    protected void okPressed() {
        preference.setValue(RTDTPlugin.SDK_LOCATION, sdkPathText.getText());
        
        preference.setValue(RTDTPlugin.USE_TOOLS_WITHIN_SDK, useDefaultButton.getSelection());

        preference.setValue(RTDTPlugin.GCC_LOCATION, gccPathText.getText());
        if (selectPythonPath) {
            preference.setValue(RTDTPlugin.PYTHON_LOCATION, pythonPathText.getText());
        }

        setReturnCode(OK);
        close();
    }
}