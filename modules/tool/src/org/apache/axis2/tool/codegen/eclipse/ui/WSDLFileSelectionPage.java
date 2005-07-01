package org.apache.axis.tool.codegen.eclipse.ui;

import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The first page of the code generator wizrad. Asks for the WSDL file Name
 */

public class WSDLFileSelectionPage extends AbstractWizardPage {

    private Text fileText;

    private ISelection selection;

   
    /**
     * 
     * @param pageName
     */
    public WSDLFileSelectionPage() {
        super("page1");
       

    }

    /**
     * Creates a default value for the settings on this page. For
     * WSDLFileSelection, this is not very much.
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_WSDL_LOCATION, "");
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        Label label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page1.fileselection.label"));

        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        fileText.setLayoutData(gd);
        fileText.setText(settings.get(PREF_WSDL_LOCATION));
        fileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_WSDL_LOCATION, fileText.getText());
                dialogChanged();
            }
        });

        Button button = new Button(container, SWT.PUSH);
        button.setText(CodegenWizardPlugin
                .getResourceString("page1.fileselection.browse"));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        
        setPageComplete(false);
        setControl(container);

        /*
         * Validate this dialog, because we could have got valid values from the
         * settings already.
         */
        if (restoredFromPreviousSettings){
            dialogChanged();
        }
    }

    /**
     * Handle the dialog change event. Basically evaluates the file name and
     * sets the error message accordingly
     */
    private void dialogChanged() {
        String fileName = getFileName();

        if (fileName.length() == 0) {
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page1.error.filemissingerror"));
            return;
        }

        if (!fileName.matches(".*\\.wsdl")) {
            updateStatus(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                    .getResourceString("page1.error.wrongextension"));
            return;
        }

        updateStatus(null);

    }

    /**
     * Pops up the file browse dialog box
     *  
     */
    private void handleBrowse() {
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[] { "*.wsdl" });
        String fileName = fileDialog.open();
        if (fileName != null) {
            fileText.setText(fileName);
        }

    }

    
    /**
     * Get the file name
     * 
     * @return
     */
    public String getFileName() {
        return fileText.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.tool.codegen.eclipse.ui.CodegenPage#getPageType()
     */
    public int getPageType() {
        return WSDL_2_JAVA_TYPE;
    }
}