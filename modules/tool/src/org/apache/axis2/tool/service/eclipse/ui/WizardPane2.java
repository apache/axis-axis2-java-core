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
package org.apache.axis.tool.service.eclipse.ui;

import org.apache.axis.tool.service.bean.Page2Bean;
import org.apache.axis.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class WizardPane2 extends WizardPage {

    private Text serviceXMLText;
    private Label manualSelectionLabel;
    private Button browseButton;
    private Button selectAutoFileGenerationCheckBox;

    private boolean skipNextPage = true;
    private boolean pageComplete;

    public WizardPane2() {
        super("page2");
        this.setTitle(ServiceArchiver.getResourceString("page2.title"));
        this.setDescription(
                ServiceArchiver.getResourceString("page2.welcometext"));
        this.setImageDescriptor(ServiceArchiver.getWizardImageDescriptor());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        container.setLayout(layout);

        manualSelectionLabel = new Label(container, SWT.NULL);
        manualSelectionLabel.setText(
                ServiceArchiver.getResourceString(
                        "page2.selectservicexml.caption"));

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        serviceXMLText = new Text(container, SWT.BORDER);
        serviceXMLText.setLayoutData(gd);
        serviceXMLText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleModify();
            }
        });

        browseButton = new Button(container, SWT.PUSH);
        browseButton.setText(
                ServiceArchiver.getResourceString("general.browse"));
        browseButton.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                handleBrowse();
            }
        });

        gd = new GridData();
        gd.horizontalSpan = 2;
        selectAutoFileGenerationCheckBox = new Button(container, SWT.CHECK);
        selectAutoFileGenerationCheckBox.setLayoutData(gd);
        selectAutoFileGenerationCheckBox.setText(
                ServiceArchiver.getResourceString("page2.generateauto.caption"));
        selectAutoFileGenerationCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                handleSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        /////////////////////////////////////////
        //disable the selection combo for now
        selectAutoFileGenerationCheckBox.setEnabled(false);
        ////////////////////////////////////////////
        setControl(container);
        setPageComplete(false);
    }

    private void handleBrowse() {
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[]{"service.xml"});
        String returnFileName = fileDialog.open();
        if (returnFileName != null) {
            this.serviceXMLText.setText(returnFileName);
        }
    }

    private void handleSelection() {
        if (this.selectAutoFileGenerationCheckBox.getSelection()) {
            changeManualSelectionStatus(false);
            this.skipNextPage = false;
            updateMessage(null);
        } else {
            changeManualSelectionStatus(true);
            this.skipNextPage = true;
            handleModify();

        }
    }

    private void changeManualSelectionStatus(boolean state) {
        this.serviceXMLText.setEnabled(state);
        this.browseButton.setEnabled(state);
        this.manualSelectionLabel.setEnabled(state);
    }

    private void handleModify() {
        String serviceXMLString = serviceXMLText.getText().trim().toLowerCase();
        if (serviceXMLString.equals("")) {
            this.updateMessage(
                    ServiceArchiver.getResourceString(
                            "page2.error.servicenameempty"));
        } else if (!serviceXMLString.endsWith("service.xml")) {
            this.updateMessage(
                    ServiceArchiver.getResourceString(
                            "page2.error.servicenamewrong"));
        } else {
            this.updateMessage(null);
        }
    }

    private void updateMessage(String str) {
        this.setErrorMessage(str);
        setPageComplete(str == null);
    }

    /**
     * @return Returns the skipNextPage.
     */
    public boolean isSkipNextPage() {
        return skipNextPage;
    }

    public Page2Bean getBean() {
        Page2Bean pageBean = new Page2Bean();
        pageBean.setManual(true);
        pageBean.setManualFileName(this.serviceXMLText.getText());
        return pageBean;
    }
}
