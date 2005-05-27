/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.axis.tool.codegen.eclipse.pages;

import org.apache.axis.tool.codegen.eclipse.CodegenWizardPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class OutputPage extends WizardPage {
    private Text outputLocation;
    private Button browseButton;
    private Button locationSelectCheckBox;

    /**
     * @param pageName
     */
    public OutputPage() {
        super(CodegenWizardPlugin.getResourceString("page3.name"));
        setTitle(CodegenWizardPlugin.getResourceString("page3.title"));
        setDescription(CodegenWizardPlugin.getResourceString("page3.desc"));
        setImageDescriptor(CodegenWizardPlugin.getWizardImageDescriptor());
        //set the page complete status to false at initilaization
        setPageComplete(false);

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
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
                .getResourceString("page3.output.caption"));

        outputLocation = new Text(container, SWT.BORDER);
        outputLocation.setLayoutData(gd);
        outputLocation.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleModifyEvent();
            }
        });

        browseButton = new Button(container, SWT.PUSH);
        browseButton.setText(CodegenWizardPlugin
                .getResourceString("page3.outselection.browse"));
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });

//        locationSelectCheckBox = new Button(container, SWT.CHECK);
//        locationSelectCheckBox.setText("Workspace projects only");

        setControl(container);
        

    }
    
    /**
     * get the output location
     * @return
     */
    public String getOutputLocation() {
        return outputLocation.getText();
    }

    /**
     * Worker method for handling modifications to the 
     * textbox
     *
     */
    private void handleModifyEvent() {
        String text = this.outputLocation.getText();
        if ((text == null) || (text.trim().equals(""))) {
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page3.error.nolocation"));
            return;
        }
        updateStatus(null);
    }

    /**
     * Updates the wizard page error messages
     * @param message
     */
    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    /**
     * Handle the browse button events
     *
     */
    private void handleBrowse() {
        boolean location = false;//locationSelectCheckBox.getSelection();
        if (!location) {
            DirectoryDialog dialog = new DirectoryDialog(this.getShell());
            String returnString = dialog.open();
            if (returnString != null) {
                outputLocation.setText(returnString);
            }
        } else {
            ContainerSelectionDialog dialog =
    			new ContainerSelectionDialog(
    				getShell(),
    				ResourcesPlugin.getWorkspace().getRoot(),
    				false,
    				CodegenWizardPlugin.getResourceString("page3.containerbox.title"));
    		if (dialog.open() == ContainerSelectionDialog.OK) {
    			Object[] result = dialog.getResult();
    			if (result.length == 1) {
    				outputLocation.setText(((Path)result[0]).toOSString());
    			}
    		}
        }
    }
}
