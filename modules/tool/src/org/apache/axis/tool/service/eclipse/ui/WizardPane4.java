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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WizardPane4 extends WizardPage {

    private Text outputFileNameTextBox;
    private Button browseButton;
    private Text outputFileName;
    
    public WizardPane4(){
        super("Page4");
        this.setTitle(ServiceArchiver.getResourceString("page3.title"));
        this.setDescription(ServiceArchiver.getResourceString("page3.welcometext"));
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
        gd.grabExcessHorizontalSpace = true;
        
		Label lable = new Label(container,SWT.NULL);
		lable.setText(ServiceArchiver.getResourceString("page3.outputlocation.label"));
		
		outputFileNameTextBox = new Text(container,SWT.BORDER);
		outputFileNameTextBox.setLayoutData(gd);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
				
		browseButton = new Button(container,SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(gd);
		browseButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleBrowse();
		    } 
		});
		
		lable = new Label(container,SWT.NULL);
		lable.setText("Output file name");
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		
		outputFileName = new Text(container,SWT.BORDER);
		outputFileName.setLayoutData(gd);
		
		setControl(container);


    }
    
    private void handleBrowse(){
        DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
        dirDialog.setMessage("Browse for the output location");
        String returnText = dirDialog.open();
        if (returnText!=null){
            this.outputFileNameTextBox.setText(returnText);
            this.outputFileNameTextBox.setToolTipText(returnText);
        }
     }
}
