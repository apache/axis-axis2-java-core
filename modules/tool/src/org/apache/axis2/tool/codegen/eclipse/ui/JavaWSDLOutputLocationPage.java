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
package org.apache.axis2.tool.codegen.eclipse.ui;

import java.io.File;

import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Ajith
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaWSDLOutputLocationPage extends AbstractWizardPage {
    
    private Text outputFolderTextBox;
    private Text outputFileNameTextBox;

    /**
     * @param pageName
     */
    public JavaWSDLOutputLocationPage() {
        super("page6");
    }
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.codegen.eclipse.ui.AbstractWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_JAVA_OUTPUT_WSDL_LOCATION,System.getProperty("user.dir"));
        settings.put(JAVA_OUTPUT_WSDL_NAME,"service.wsdl");

    }

    /* (non-Javadoc)
     * @see org.apache.axis2.tool.codegen.eclipse.ui.AbstractWizardPage#getPageType()
     */
    public int getPageType() {
         return JAVA_2_WSDL_TYPE;
    }

    /* (non-Javadoc)
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
                .getResourceString("page6.output.label"));

        outputFolderTextBox = new Text(container,SWT.BORDER);
        outputFolderTextBox.setLayoutData(gd);
        outputFolderTextBox.setText(settings.get(PREF_JAVA_OUTPUT_WSDL_LOCATION));
        outputFolderTextBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
                handleFolderTextChange();
            }
        });
        
        Button  browseButton = new Button(container,SWT.PUSH);
        browseButton.setText(CodegenWizardPlugin
                .getResourceString("general.browse"));
        browseButton.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e){
               handleBrowse(); 
            }
            public void widgetDefaultSelected(SelectionEvent e){}
        });
        
        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page6.outputname.label"));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        outputFileNameTextBox = new Text(container,SWT.BORDER);
        outputFileNameTextBox.setLayoutData(gd);
        outputFileNameTextBox.setText(settings.get(JAVA_OUTPUT_WSDL_NAME));
        outputFileNameTextBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
               handleFileNameTextChange();
            }
        });
        
        if(restoredFromPreviousSettings){
            handleFolderTextChange();
            handleFolderTextChange();
        }
        
        setControl(container);

    }
    
    private void handleFolderTextChange(){
        String outputFolder = outputFolderTextBox.getText();
        settings.put(PREF_JAVA_OUTPUT_WSDL_LOCATION,outputFolder);
        if ("".equals(outputFolder.trim())){
            updateStatus("Input a proper location for the output");
        }else{
            updateStatus(null);
        }
    }
    
    private void handleFileNameTextChange(){
        String outFileName = outputFileNameTextBox .getText();
        settings.put(JAVA_OUTPUT_WSDL_NAME,outFileName);
        if ("".equals(outFileName.trim())){
            updateStatus("Input a file name");
        }else if (outFileName.matches("\\W")){
            updateStatus("Input a valid file name");
        }else{
            updateStatus(null); 
        }
    }
    
    private void handleBrowse(){
        DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
        String dirName = fileDialog.open();
        if (dirName != null) {
            outputFolderTextBox.setText(dirName);
        }

    }
    
    public String getFullFileName(){
        String folder =this.outputFolderTextBox.getText();
        String fileName = this.outputFileNameTextBox.getText();
        if (!fileName.endsWith(".wsdl")){
            fileName = fileName + ".wsdl";
        }
        return folder + File.separator +fileName;
    }
    
    public String getOutputWSDLName(){
        return this.outputFileNameTextBox.getText();
    }
    
    public String getOutputLocation(){
        return this.outputFolderTextBox.getText();
    }

}
