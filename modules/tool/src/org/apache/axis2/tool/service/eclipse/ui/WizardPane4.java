/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.tool.service.eclipse.ui;

import org.apache.axis.tool.service.bean.Page3Bean;
import org.apache.axis.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class WizardPane4 extends WizardPage {

    private Text outputFileLocationTextBox;
    private Button browseButton;
    private Text outputFileNameTextbox;

    public WizardPane4() {
        super("Page4");
        this.setTitle(ServiceArchiver.getResourceString("page4.title"));
        this.setDescription(ServiceArchiver.getResourceString("page4.welcometext"));
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

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;

        Label lable = new Label(container, SWT.NULL);
        lable.setText(ServiceArchiver.getResourceString("page4.outputlocation.label"));

        outputFileLocationTextBox = new Text(container, SWT.BORDER);
        outputFileLocationTextBox.setLayoutData(gd);
        outputFileLocationTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleModify();
            }
        });

        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);

        browseButton = new Button(container, SWT.PUSH);
        browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
        browseButton.setLayoutData(gd);
        browseButton.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                handleBrowse();
            }
        });

        lable = new Label(container, SWT.NULL);
        lable.setText(ServiceArchiver.getResourceString("page4.outputname.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;

        outputFileNameTextbox = new Text(container, SWT.BORDER);
        outputFileNameTextbox.setLayoutData(gd);
        outputFileNameTextbox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleModify();
            }
        });

        //Fill the name with the default name
        outputFileNameTextbox.setText("my_service.jar");
        setControl(container);


    }

    private void handleBrowse() {
        DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
        dirDialog.setMessage(ServiceArchiver.getResourceString("page4.dirdialog.caption"));
        String returnText = dirDialog.open();
        if (returnText != null) {
            this.outputFileLocationTextBox.setText(returnText);
            this.outputFileLocationTextBox.setToolTipText(returnText);
        }
    }

    private void handleModify() {
        String outputLocationText = outputFileLocationTextBox.getText();
        String outputFilenameText = outputFileNameTextbox.getText();
        if (outputLocationText == null || outputLocationText.trim().equals("")) {
            this.updateMessage("");
        } else if (outputLocationText == null || outputLocationText.trim().equals("")) {
            this.updateMessage("");
        } else {
            updateMessage(null);
        }
    }

    private void updateMessage(String str) {
        this.setErrorMessage(str);
        setPageComplete(str == null);
    }

    public Page3Bean getBean() {
        Page3Bean pageBean = new Page3Bean();
        pageBean.setOutputFolderName(this.outputFileLocationTextBox.getText().trim());
        pageBean.setOutputFileName(this.outputFileNameTextbox.getText().trim());
        return pageBean;
    }
}
