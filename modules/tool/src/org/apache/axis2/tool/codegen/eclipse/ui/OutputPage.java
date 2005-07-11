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

package org.apache.axis.tool.codegen.eclipse.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class OutputPage extends AbstractWizardPage {

    private Text outputLocation;

    private Button browseButton;

    private Button locationSelectCheckBox;


    /**
     *  
     */
    public OutputPage() {
        super("page3");
    }

    /**
     * Creates some initial values for the settings. On the output page, this is
     * not very much.
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_OUTPUT_LOCATION, "");
    }

    /*
     * (non-Javadoc)
     * 
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
        label
                .setText(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                         .getResourceString("page3.output.caption"));

        outputLocation = new Text(container, SWT.BORDER);
        outputLocation.setLayoutData(gd);
        outputLocation.setText(settings.get(PREF_OUTPUT_LOCATION));
        outputLocation.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_OUTPUT_LOCATION, outputLocation.getText());
                handleModifyEvent();
            }
        });

        browseButton = new Button(container, SWT.PUSH);
        browseButton
                .setText(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                         .getResourceString("page3.outselection.browse"));
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });

        // locationSelectCheckBox = new Button(container, SWT.CHECK);
        // locationSelectCheckBox.setText("Workspace projects only");

        setControl(container);

        /*
         * Update the buttons, in case this was restored from an earlier setting
         */
        if (restoredFromPreviousSettings) {
            handleModifyEvent();
        }
    }

    /**
     * get the output location
     * 
     * @return a string containing the full pathname of the output location
     */
    public String getOutputLocation() {
        return outputLocation.getText();
    }

    /**
     * Worker method for handling modifications to the textbox
     */
    private void handleModifyEvent() {
        String text = this.outputLocation.getText();
        if ((text == null) || (text.trim().equals(""))) {
            updateStatus(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                         .getResourceString("page3.error.nolocation"));
            return;
        }
        updateStatus(null);
    }


    /**
     * Handle the browse button events: opens a dialog where the user can choose
     * an external directory location
     */
    private void handleBrowse() {
        boolean location = false;// locationSelectCheckBox.getSelection();
        if (!location) {
            DirectoryDialog dialog = new DirectoryDialog(this.getShell());
            String returnString = dialog.open();
            if (returnString != null) {
                outputLocation.setText(returnString);
            }
        } else {
            ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
                                                                           ResourcesPlugin.getWorkspace().getRoot(),
                                                                           false,
                                                                           org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                                                                           .getResourceString("page3.containerbox.title"));
            if (dialog.open() == ContainerSelectionDialog.OK) {
                Object[] result = dialog.getResult();
                if (result.length == 1) {
                    outputLocation.setText(((Path) result[0]).toOSString());
                }
            }
        }
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
