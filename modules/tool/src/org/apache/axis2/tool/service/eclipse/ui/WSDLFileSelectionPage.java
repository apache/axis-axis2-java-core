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
package org.apache.axis2.tool.service.eclipse.ui;

import org.apache.axis2.tool.service.bean.WSDLFileLocationBean;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WSDLFileSelectionPage extends AbstractServiceWizardPage {
    
    private boolean skipNextPage = true;
    
    private Text wsdlTextBox;
    private Label selectionLabel;
    private Button browseButton;
    private Button autoGenerateWSDLCheckButton;
    private Button skipWSDLCheckButton;
    
    public WSDLFileSelectionPage(){
        super("page5");
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_WSDL_FILE_NAME,"");
        settings.put(PREF_CHECK_WSDL_GENERATE,false);
        settings.put(PREF_CHECK_SKIP_WSDL,false);

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        container.setLayout(layout);
               
        selectionLabel= new Label(container,SWT.NULL);
        selectionLabel.setText(ServiceArchiver.getResourceString("page5.selectwsdl.caption"));
		
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        wsdlTextBox = new Text(container,SWT.BORDER);
        wsdlTextBox.setLayoutData(gd);
        wsdlTextBox.setText(settings.get(PREF_WSDL_FILE_NAME));
        wsdlTextBox.addModifyListener(new ModifyListener(){
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
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		autoGenerateWSDLCheckButton = new Button(container,SWT.CHECK);
		autoGenerateWSDLCheckButton.setLayoutData(gd);
		autoGenerateWSDLCheckButton.setText(ServiceArchiver.getResourceString("page5.generateauto.caption"));
		autoGenerateWSDLCheckButton.setSelection(settings.getBoolean(PREF_CHECK_WSDL_GENERATE));
		autoGenerateWSDLCheckButton.addSelectionListener(new SelectionListener(){
		    public void widgetSelected(SelectionEvent e){
		        handleSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e){}
		});
		autoGenerateWSDLCheckButton.setToolTipText(ServiceArchiver.getResourceString("page5.autogen.tooltip"));
		////////////////////////////////////////
		// Disable the automatic generation box
		autoGenerateWSDLCheckButton.setEnabled(false);
		///////////////////////////////////////
		
		//add an empty lable
		new Label(container,SWT.NONE);
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		skipWSDLCheckButton = new Button(container,SWT.CHECK);
		skipWSDLCheckButton.setText(ServiceArchiver.getResourceString("page5.skipWSDL.caption"));
		skipWSDLCheckButton.setLayoutData(gd);
		skipWSDLCheckButton.setSelection(settings.getBoolean(PREF_CHECK_SKIP_WSDL));
		skipWSDLCheckButton.addSelectionListener(new SelectionListener(){
		    public void widgetSelected(SelectionEvent e){
		        handleSkip();
		    }
		    public void widgetDefaultSelected(SelectionEvent e){} 
		    
		});
				
		setControl(container);
		
		if (restoredFromPreviousSettings){
		    if (!skipWSDLCheckButton.getSelection()){
		    handleSelection();
		    }
		}
    }

    private void handleSkip(){
        if (skipWSDLCheckButton.getSelection()){
           //disable other widgtets
           setStatus(false);
           //enable next
           this.updateStatus(null);
           settings.put(PREF_CHECK_SKIP_WSDL,true);
           
        }else{
            setStatus(true);
            //call this to update the status
            handleModify();
            settings.put(PREF_CHECK_SKIP_WSDL,false);
        }
    }
    
    private void setStatus(boolean b){
        this.selectionLabel.setEnabled(b);
        this.browseButton.setEnabled(b);
        this.wsdlTextBox.setEnabled(b);
    }
    private void handleBrowse(){
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[]{"*.wsdl"});
        String returnFileName = fileDialog.open() ;
        if (returnFileName!=null){
            this.wsdlTextBox.setText(returnFileName);
        }
    }
    
    private void handleSelection(){
        boolean selection = this.autoGenerateWSDLCheckButton.getSelection();
        settings.put(PREF_CHECK_WSDL_GENERATE,selection);
        if (selection){
            changeManualSelectionStatus(false); 
            this.skipNextPage = false;
            updateStatus(null);
            updateRecommendation(ServiceArchiver.getResourceString("page5.recommendation"));
        }else{
            changeManualSelectionStatus(true);
            this.skipNextPage = true;
            handleModify();
            updateRecommendation("");
        }
    }
    
    private void handleModify(){
        String text = wsdlTextBox.getText();
        settings.put(PREF_WSDL_FILE_NAME,text);
        //validate
        if ("".equals(text)){
            this.updateStatus(ServiceArchiver.getResourceString("page5.error.wsdlnameempty")); 
         }else if(!text.endsWith(".wsdl")){
             this.updateStatus(ServiceArchiver.getResourceString("page5.error.wsdlnamewrong"));  
         }else{
             this.updateStatus(null);
         }
        
    }
    private void updateRecommendation(String message){
        ServiceArchiveWizard wizard = (ServiceArchiveWizard)this.getWizard();
        wizard.updateWsdlFileGenerationStatus(message);
       
    }
    private void changeManualSelectionStatus(boolean state){
        this.wsdlTextBox.setEnabled(state);
        this.browseButton.setEnabled(state);
        this.selectionLabel.setEnabled(state);
    }
    
    public boolean isAutoGenerate(){
        return autoGenerateWSDLCheckButton.getSelection();
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#isSkipNext()
     */
    public boolean isSkipNext() {
        return this.skipNextPage;
    }
    
    public WSDLFileLocationBean getBean(){
        WSDLFileLocationBean locationBean = new WSDLFileLocationBean();
        locationBean.setManual(!autoGenerateWSDLCheckButton.getSelection());
        locationBean.setWSDLFileName(wsdlTextBox.getText());
        locationBean.setSkip(skipWSDLCheckButton.getSelection());
        return locationBean;
    }
}
