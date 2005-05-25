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


import org.apache.axis.tool.eclipse.plugin.ServiceArchiver;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * @author Ajith
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ServiceArchiveWizard extends Wizard implements INewWizard {

    private WizardPane1 wizardPane1;
    private WizardPane2 wizardPane2;
    private WizardPane3 wizardPane3;
    private WizardPane4 wizardPane4;
    
    
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
    public IWizardPage getNextPage(IWizardPage page) {
       IWizardPage pageout = super.getNextPage(page);
       if (page.equals(wizardPane2)){
           if (((WizardPane2)page).isSkipNextPage()){
               pageout = super.getNextPage(pageout);
           }
       }
       return pageout;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
         return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        wizardPane1 = new WizardPane1();
        this.addPage(wizardPane1);
        wizardPane2 = new WizardPane2();
        this.addPage(wizardPane2);
        wizardPane3 = new WizardPane3();
        this.addPage(wizardPane3);
        wizardPane4 = new WizardPane4();
        this.addPage(wizardPane4);
    }
    /* (non-Javadobc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }
}
