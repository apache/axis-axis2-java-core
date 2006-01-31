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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class JavaWSDLOutputLocationPage extends AbstractWizardPage {
    
    private Text outputFolderTextBox;
    private Text outputFileNameTextBox;
    private Button locationSelectCheckBox;

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
        settings.put(JAVA_OUTPUT_WSDL_NAME,"services.wsdl");
        settings.put(PREF_JAVA_OUTPUT_SELECTION,false);

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
        layout.numColumns = 3;
        //layout.verticalSpacing = 9;
        container.setLayout(layout);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        locationSelectCheckBox = new Button(container, SWT.CHECK);
        locationSelectCheckBox.setText(CodegenWizardPlugin
                .getResourceString("page6.selectOption.label"));
        locationSelectCheckBox.setLayoutData(gd);
        locationSelectCheckBox.addSelectionListener(new SelectionAdapter(){
        	public void widgetSelected(SelectionEvent e) {
        		 settings.put(PREF_JAVA_OUTPUT_SELECTION,
        				 locationSelectCheckBox.getSelection());
        	}
        });
        
        
        Label label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page6.output.label"));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
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
        gd.horizontalSpan = 2;
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
    	
    	boolean location = locationSelectCheckBox.getSelection();
		if (!location) {
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			String returnString = dialog.open();
			if (returnString != null) {
				outputFolderTextBox.setText(returnString);
			}
		} else {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(
					getShell(),
					root,
					false,
					CodegenWizardPlugin
							.getResourceString("page3.containerbox.title"));
			if (dialog.open() == ContainerSelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length == 1) {
					Path path = ((Path) result[0]);
					// append to the workspace path
					if (root.exists(path)) {
						outputFolderTextBox.setText(root.getLocation().append(path)
								.toFile().getAbsolutePath());
					}
				}
			}
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
