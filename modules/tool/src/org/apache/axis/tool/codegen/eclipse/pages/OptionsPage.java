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
import org.apache.axis.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis.wsdl.util.URLProcessor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OptionsPage extends WizardPage implements UIConstants {

    private Combo languageSelectionComboBox;

    private Button syncAndAsyncRadioButton;

    private Button syncOnlyRadioButton;

    private Button asyncOnlyRadioButton;

    private Text packageText;

    private Button serverSideCheckBoxButton;
    
    private Button testCaseCheckBoxButton;

    private Button serverXMLCheckBoxButton;

    /**
     * @param pageName
     */
    public OptionsPage() {
        super(CodegenWizardPlugin.getResourceString("page2.name"));
        setTitle(CodegenWizardPlugin.getResourceString("page2.title"));
        setDescription(CodegenWizardPlugin.getResourceString("page2.desc"));
        setImageDescriptor(CodegenWizardPlugin.getWizardImageDescriptor());

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
        gd.horizontalSpan = 2;

        Label label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page2.language.caption"));

        languageSelectionComboBox = new Combo(container, SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        //fill the combo
        this.fillLanguageCombo();
        languageSelectionComboBox.setLayoutData(gd);

        syncAndAsyncRadioButton = new Button(container, SWT.RADIO);
        syncAndAsyncRadioButton.setText(CodegenWizardPlugin
                .getResourceString("page2.syncAsync.caption"));
        syncAndAsyncRadioButton.setSelection(true);

        syncOnlyRadioButton = new Button(container, SWT.RADIO);
        syncOnlyRadioButton.setText(CodegenWizardPlugin
                .getResourceString("page2.sync.caption"));

        asyncOnlyRadioButton = new Button(container, SWT.RADIO);
        asyncOnlyRadioButton.setText(CodegenWizardPlugin
                .getResourceString("page2.async.caption"));

        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page2.package.caption"));

        packageText = new Text(container, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;

        packageText.setLayoutData(gd);
        packageText.setText(URLProcessor.getNameSpaceFromURL(""));//get this text from the URLProcessor
        
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        testCaseCheckBoxButton = new Button(container, SWT.CHECK);
        testCaseCheckBoxButton.setLayoutData(gd);
        testCaseCheckBoxButton.setText(CodegenWizardPlugin
                .getResourceString("page2.testcase.caption"));
        
        
        
        
        serverSideCheckBoxButton = new Button(container, SWT.CHECK);
        serverSideCheckBoxButton.setText(CodegenWizardPlugin
                .getResourceString("page2.serverside.caption"));
        serverSideCheckBoxButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                handleServersideSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        serverXMLCheckBoxButton = new Button(container, SWT.CHECK);
        serverXMLCheckBoxButton.setText(CodegenWizardPlugin
                .getResourceString("page2.serviceXML.caption"));
        serverXMLCheckBoxButton.setEnabled(false);

        setControl(container);
        setPageComplete(true);

    }

    /**
     * Fill the combo with proper language names
     *  
     */
    private void fillLanguageCombo() {

        languageSelectionComboBox.add(JAVA);
        languageSelectionComboBox.add(C_SHARP);
        languageSelectionComboBox.add(C_PLUS_PLUS);

        languageSelectionComboBox.setText(languageSelectionComboBox.getItem(0));
    }

    private void handleServersideSelection() {
        if (this.serverSideCheckBoxButton.getSelection()) {
            this.serverXMLCheckBoxButton.setEnabled(true);
        } else {
            this.serverXMLCheckBoxButton.setEnabled(false);
        }
    }

    /**
     * Get the selected language
     * 
     * @return
     */
    public String getSelectedLanguage() {
        return languageSelectionComboBox.getItem(languageSelectionComboBox
                .getSelectionIndex());
    }

    /**
     * the async only status
     * 
     * @return
     */
    public boolean isAsyncOnlyOn() {
        return asyncOnlyRadioButton.getSelection();
    }

    /**
     * the sync only status
     * 
     * @return
     */
    public boolean isSyncOnlyOn() {
        return syncOnlyRadioButton.getSelection();
    }

    /**
     * return the package name
     * 
     * @return
     */
    public String getPackageName() {
        return this.packageText.getText();
    }

    /**
     * The serverside status
     * @return
     */
    public boolean isServerside() {
        return this.serverSideCheckBoxButton.getSelection();
    }

    public boolean isServerXML() {
        if (this.serverXMLCheckBoxButton.isEnabled())
            return this.serverXMLCheckBoxButton.getSelection();
        else
            return false;
    }
    public boolean isGenerateTestCase(){
        return this.testCaseCheckBoxButton.getSelection();
    }
}
