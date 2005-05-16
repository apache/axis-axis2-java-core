package org.apache.axis.tool.codegen.eclipse.pages;

import org.apache.axis.tool.codegen.eclipse.CodegenWizardPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.viewers.*;

/**
 * The first page of the code generator wizrad. 
 * Asks for the WSDL file Name
 */

public class WSDLFileSelectionPage extends WizardPage {
	
	private Text fileText;
	private ISelection selection;

	/**
	 * 
	 * @param pageName
	 */
	public WSDLFileSelectionPage(ISelection selection) {
		super(CodegenWizardPlugin.getResourceString("page1.name"));
		setTitle(CodegenWizardPlugin.getResourceString("page1.title"));
		setDescription(CodegenWizardPlugin.getResourceString("page1.desc"));
		this.selection = selection;
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
		label.setText(CodegenWizardPlugin.getResourceString("page1.fileselection.label"));

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		Button button = new Button(container, SWT.PUSH);
		button.setText(CodegenWizardPlugin.getResourceString("page1.fileselection.browse"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		setControl(container);
	}
	
	/**
	 * Handle the dialog change event. Basically evaluates the file name
	 * and sets the error message accordingly 
	 */
	private void dialogChanged() {
	    
	    String fileName = getFileName();
	    
	    if (fileName.length() == 0) {
			updateStatus(CodegenWizardPlugin.getResourceString("page1.error.filemissingerror"));
			return;
		}
	    
		int dotLoc = fileName.lastIndexOf('.');
		
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("wsdl") == false) {
				updateStatus(CodegenWizardPlugin.getResourceString("File extension must be wsdl"));
				return;
			}
		}  
	    updateStatus(null);
	}
	
	/**
	 * Pops up the file browse dialog box
	 *
	 */
	private void handleBrowse(){
	    FileDialog fileDialog = new FileDialog(this.getShell());
	    fileDialog.setFilterExtensions(new String[]{"*.wsdl"});
	    String fileName = fileDialog.open();
	    if (fileName!=null){
	        fileText.setText(fileName);
	    }
	   
	}
	
	/**
	 * Updates the wizard page messages
	 * @param message
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getFileName() {
		return fileText.getText();
	}
}