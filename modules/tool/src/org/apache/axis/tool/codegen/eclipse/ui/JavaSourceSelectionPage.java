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

import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
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

public class JavaSourceSelectionPage extends AbstractWizardPage{

   
    private Text javaClassFileLocationBox;
    private Text javaClassNameBox;


    public JavaSourceSelectionPage() {  
        super("page4");
    }

    protected void initializeDefaultSettings() {
        settings.put(JAVA_CLASS_NAME, "");
        settings.put(JAVA_CLASS_LOCATION_NAME, "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.tool.codegen.eclipse.ui.CodegenPage#getPageType()
     */
    public int getPageType() {
        return JAVA_2_WSDL_TYPE;
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
        label.setText(CodegenWizardPlugin
                .getResourceString("page4.javafilelocation.label"));

        javaClassFileLocationBox = new Text(container, SWT.BORDER);
        javaClassFileLocationBox.setLayoutData(gd);
        javaClassFileLocationBox.setText(settings.get(JAVA_CLASS_LOCATION_NAME));
        javaClassFileLocationBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
               handleLocationTextChange(); 
            }
        });
        

        Button browseButton = new Button(container, SWT.PUSH);
        browseButton.setText(CodegenWizardPlugin
                .getResourceString("general.browse"));
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleDirectoryBrowse();
            }
        });
        
        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page4.classname.label"));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        javaClassNameBox = new Text(container,SWT.BORDER);
        javaClassNameBox.setLayoutData(gd);
        javaClassNameBox.setText(settings.get(JAVA_CLASS_NAME));
        javaClassNameBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
                handleClassNameTextChange();
             }
         });
        
        Button searchButton = new Button(container, SWT.PUSH);
        searchButton.setText(CodegenWizardPlugin
                .getResourceString("general.search"));
        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                //handleDirectoryBrowse();
            }
        });
        searchButton.setEnabled(false);
        
        setPageComplete(false);
        
        if (restoredFromPreviousSettings){
            handleLocationTextChange();
            handleClassNameTextChange();
        }
        
        setControl(container);

    }

    /**
     * Pops up the file browse dialog box
     *  
     */
    private void handleDirectoryBrowse() {
        DirectoryDialog fileDialog = new DirectoryDialog(this.getShell());
        String dirName = fileDialog.open();
        if (dirName != null) {
            javaClassFileLocationBox.setText(dirName);
        }

    }
    
    private void handleLocationTextChange(){
        String locationText = javaClassFileLocationBox.getText();
        settings.put(JAVA_CLASS_LOCATION_NAME,locationText);
        if (locationText==null || "".equals(locationText.trim())){
            updateStatus("Class file location needs to be specified!");
        }else{
            updateStatus(null);
        }
    }
    
    private void handleClassNameTextChange(){
        String className = javaClassNameBox.getText();
        settings.put(JAVA_CLASS_NAME,className);
        if (className==null || "".equals(className.trim())){
            updateStatus("Fully qualified class name needs to be specified!");
        }else if(className.endsWith(".")){
            updateStatus("Class name is not properly terminated!");
        }else{
            updateStatus(null);
        }
    }
    
    public String getClassName(){
        return javaClassNameBox.getText();
    }
    
    public String getClassLocation(){
        return javaClassFileLocationBox.getText();
    }
}
