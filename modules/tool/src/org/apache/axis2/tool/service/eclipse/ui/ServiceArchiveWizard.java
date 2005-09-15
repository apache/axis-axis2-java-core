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


import org.apache.axis2.tool.service.bean.WizardBean;
import org.apache.axis2.tool.service.control.Controller;
import org.apache.axis2.tool.service.control.ProcessException;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class ServiceArchiveWizard extends Wizard implements INewWizard {

    private ClassFileLocationPage classFileLocationPage;
    private WSDLFileSelectionPage wsdlFileSelectionPage;
    private ServiceXMLFileSelectionPage serviceXMLFileSelectionPage;
    private ServiceXMLGenerationPage serviceXMLGenerationPage;
    private ServiceArchiveOutputLocationPage serviceArchiveOutputLocationPage;

    private boolean updateServiceGenerationStatus;
    private String classFileLocation;
    private String wsdlFileGenerationStatus;
    
    
    /**
     * @return Returns the wsdlFileGenerationStatus.
     */
    public String getWsdlFileGenerationStatus() {
        return wsdlFileGenerationStatus;
    }
    /**
     * @param message The wsdlFileGenerationStatus to set.
     */
    public void updateWsdlFileGenerationStatus(String message) {
        this.wsdlFileGenerationStatus = message;
    }
    public  String getClassFileLocation(){
        return classFileLocation;
    }
    
    public  void setClassFileLocation(String location){
        this.classFileLocation = location;
    }
    
    public void updateServiceGeneration(boolean status){
        updateServiceGenerationStatus = status;
    }
    /**
     * 
     */
    public ServiceArchiveWizard() {
        super();
        setWindowTitle(ServiceArchiver.getResourceString("main.title"));
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
//    public IWizardPage getNextPage(IWizardPage page) {
//       
//        AbstractServiceWizardPage thisPage = (AbstractServiceWizardPage)page.getNextPage();
//        while (thisPage!=null && thisPage.isSkipNext()) {
//            if (thisPage.getNextPage()!=null) {
//                thisPage = (AbstractServiceWizardPage)thisPage.getNextPage();
//            }else{
//                break;
//            }
//        }
//        return thisPage;
//    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        classFileLocationPage = new ClassFileLocationPage();
        this.addPage(classFileLocationPage);
        wsdlFileSelectionPage = new WSDLFileSelectionPage();
        this.addPage(wsdlFileSelectionPage);
        serviceXMLFileSelectionPage = new ServiceXMLFileSelectionPage();
        this.addPage(serviceXMLFileSelectionPage);
//        serviceXMLGenerationPage = new ServiceXMLGenerationPage();
//        this.addPage(serviceXMLGenerationPage);
        serviceArchiveOutputLocationPage = new ServiceArchiveOutputLocationPage();
        this.addPage(serviceArchiveOutputLocationPage);
    }

    /* (non-Javadobc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        //create a wizard bean
        WizardBean wizBean = new WizardBean();
        wizBean.setPage1bean(classFileLocationPage.getBean());
        wizBean.setWsdlBean(wsdlFileSelectionPage.getBean());
        wizBean.setPage2bean(serviceXMLFileSelectionPage.getBean());
        wizBean.setPage3bean(serviceArchiveOutputLocationPage.getBean());
        
        
        try {
            new Controller().process(wizBean);
            showSuccessMessage(" jar file creation successful! ");
            return true;
        } catch (ProcessException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Unknown Error! " + e.getMessage());
            return false;
        }


    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    private void showErrorMessage(String message) {
        MessageDialog.openError(this.getShell(), "Error", message);
    }

    private void showSuccessMessage(String message) {
        MessageDialog.openInformation(this.getShell(), "Success", message);
    }
}
