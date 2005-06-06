/**
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


import org.apache.axis.tool.service.bean.Page1Bean;
import org.apache.axis.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class WizardPane1 extends WizardPage {

    private Text classFileLocationText;
    private Button browseButton;
    
    private boolean pageComplete;
    
    public WizardPane1(){
        super("page1");
        this.setTitle(ServiceArchiver.getResourceString("page1.title"));
        this.setDescription(ServiceArchiver.getResourceString("page1.welcometext"));
        this.setImageDescriptor(ServiceArchiver.getWizardImageDescriptor());
    }
   

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label lable = new Label(container,SWT.NULL);
		lable.setText(ServiceArchiver.getResourceString("page1.fileLocationLabel"));
		
		classFileLocationText = new Text(container,SWT.BORDER);
		classFileLocationText.setLayoutData(gd);
		classFileLocationText.addModifyListener(new ModifyListener(){
		    public void modifyText(ModifyEvent e){
		        handleModify();
		    }
		});
		
		browseButton = new Button(container,SWT.PUSH);
		browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
		browseButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleBrowse();
		    } 
		});
		
		setControl(container);
		setPageComplete(false);
    }
    
    
    private void handleBrowse(){
       DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
       dirDialog.setMessage(ServiceArchiver.getResourceString("page1.filedialogTitle"));
       String returnText = dirDialog.open();
       if (returnText!=null){
           this.classFileLocationText.setText(returnText);
       }
    }
    
    private void handleModify(){
        String classLocationText = this.classFileLocationText.getText().trim();
        if (classLocationText.equals("")){
            updateMessage("Filename should not be empty");
            return;
        }else{    
            updateMessage(null);
        }
    }
    
    private void updateMessage(String str){
        this.setErrorMessage(str);
        setPageComplete(str==null);
    }
    
    public Page1Bean getBean(){
        Page1Bean pageBean = new Page1Bean();
        pageBean.setFileLocation(this.classFileLocationText.getText());
        return pageBean;
    }
}
