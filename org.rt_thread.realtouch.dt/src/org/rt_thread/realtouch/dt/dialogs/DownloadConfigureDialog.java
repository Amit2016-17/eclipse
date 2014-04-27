package org.rt_thread.realtouch.dt.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DownloadConfigureDialog extends Dialog {
    private String ipAddress;
    private Text ipAddressText;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public DownloadConfigureDialog(Shell parentShell) {
        this(parentShell, null);
    }

    public DownloadConfigureDialog(Shell parentShell, String ip) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.TITLE);

        ipAddress = ip;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Download Configurations");
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gl_container = new GridLayout(1, false);
        gl_container.marginBottom = 10;
        gl_container.marginTop = 10;
        gl_container.marginRight = 10;
        gl_container.marginLeft = 10;
        container.setLayout(gl_container);

        Label lblRealtouchIp = new Label(container, SWT.NONE);
        lblRealtouchIp.setText("RealTouch IP:");

        ipAddressText = new Text(container, SWT.BORDER);
        ipAddressText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        if (ipAddress != null) {
            ipAddressText.setText(ipAddress);
        }
        return container;
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
        return new Point(450, 300);
    }

    @Override
    protected void okPressed() {
        ipAddress = ipAddressText.getText();

        setReturnCode(OK);
        close();
    }

}
