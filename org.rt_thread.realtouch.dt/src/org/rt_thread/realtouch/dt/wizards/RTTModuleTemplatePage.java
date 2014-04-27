package org.rt_thread.realtouch.dt.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.rt_thread.realtouch.dt.RTDTPlugin;

public class RTTModuleTemplatePage extends WizardPage {
    private Button btnCheckButton;
    List list;
    private Text text;

    private String template;

    /**
     * Create the wizard.
     */
    public RTTModuleTemplatePage() {
        super("RTTModuleTemplatePage");
        setTitle("Templates");
        setDescription("Select one of the available templates to generate a fully-functioning RT-Thread module.");

        template = null;
    }

    /**
     * Create contents of the wizard.
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        btnCheckButton = new Button(container, SWT.CHECK);
        btnCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnCheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = false;
                list.setEnabled(enabled);
                text.setEnabled(enabled);
            }
        });
        btnCheckButton.setText("&Create a RT-Thread module using of the templates");
        btnCheckButton.setSelection(true);

        Label lblavailableTemplates = new Label(container, SWT.NONE);
        lblavailableTemplates.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblavailableTemplates.setText("&Available Templates:");

        list = new List(container, SWT.BORDER | SWT.SINGLE);
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        String templateLocation = RTDTPlugin.getConfigPrefs().getString(RTDTPlugin.SDK_LOCATION)
                + RTDTPlugin.DEFAULT_TEMPLATES_RELATIVE_FOLDER;
        
        File templateFolder = new File(templateLocation);
        if (templateFolder.exists()) {
            for (File file : templateFolder.listFiles()) {
                if (file.isDirectory()) {
                    list.add(file.getName());
                }
            }
        }

        list.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String[] items = list.getSelection();
                if (items.length > 0) {
                    template = items[0];
                } else {
                    template = null;
                }
            }
        });

        // text = new Text(container, SWT.BORDER | SWT.MULTI);
        // text.setEditable(false);
        // text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
        // 1));
    }

    public String getTemplate() {
        return template;
    }

}
