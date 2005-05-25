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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class WizardPane3 extends WizardPage{
    
    public WizardPane3(){
        super("page3");
        this.setTitle(ServiceArchiver.getResourceString("page2.title"));
        this.setDescription(ServiceArchiver.getResourceString("Generate the service XML file"));
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
               
        Label manualSelectionLabel = new Label(container,SWT.NULL);
        manualSelectionLabel.setText("Make the service XML file");
		
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		
		setControl(container);

    }
    
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
     */
    public boolean canFlipToNextPage() {
        // TODO Auto-generated method stub
        return super.canFlipToNextPage();
    }
}
