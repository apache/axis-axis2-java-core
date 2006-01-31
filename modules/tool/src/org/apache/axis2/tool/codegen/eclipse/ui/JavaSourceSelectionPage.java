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
package org.apache.axis2.tool.codegen.eclipse.ui;



import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis2.tool.codegen.eclipse.util.ClassFileReader;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class JavaSourceSelectionPage extends AbstractWizardPage{

   
    private Text javaClassNameBox;
    private List javaClasspathList;
    private Label statusLabel;

    public JavaSourceSelectionPage() {  
        super("page4");
    }

    protected void initializeDefaultSettings() {
        settings.put(JAVA_CLASS_NAME, "");
        settings.put(JAVA_CLASS_PATH_ENTRIES, new String[]{});
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.tool.codegen.eclipse.ui.CodegenPage#getPageType()
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
        
        //class name  entry
        Label label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                .getResourceString("page4.classname.label"));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        javaClassNameBox = new Text(container,SWT.BORDER);
        javaClassNameBox.setLayoutData(gd);
        javaClassNameBox.setText(settings.get(JAVA_CLASS_NAME));
        javaClassNameBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
                handleClassNameTextChange();
             }
         });
        
        //class path entry
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=3;
        label = new Label(container, SWT.NULL);
        label.setLayoutData(gd);
        label.setText(CodegenWizardPlugin
                .getResourceString("page4.classpath.label"));
        
        
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button addDirButton = new Button(container,SWT.PUSH);
        addDirButton.setLayoutData(gd);
        addDirButton.setText(CodegenWizardPlugin
                .getResourceString("page4.addDir.label"));
        addDirButton.addMouseListener(new MouseAdapter(){
        	public void mouseUp(MouseEvent e) {
        		handleDirectoryBrowse();
        	}
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button addJarButton = new Button(container,SWT.PUSH);
        addJarButton.setLayoutData(gd);
        addJarButton.setText(CodegenWizardPlugin
                .getResourceString("page4.addJar.label"));
        addJarButton.addMouseListener(new MouseAdapter(){
        	public void mouseUp(MouseEvent e) {
        		handleFileBrowse();
        	}
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button removeEntryButton = new Button(container,SWT.PUSH);
        removeEntryButton.setLayoutData(gd);
        removeEntryButton.setText(CodegenWizardPlugin
                .getResourceString("page4.removeEntry.label"));
        removeEntryButton.addMouseListener(new MouseAdapter(){
        	public void mouseUp(MouseEvent e) {
        		handleRemove();
        	}
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        gd.verticalSpan = 7;
        javaClasspathList = new List(container,SWT.READ_ONLY | SWT.BORDER);
        javaClasspathList.setLayoutData(gd);
        javaClasspathList.setItems(settings.getArray(JAVA_CLASS_PATH_ENTRIES));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button tryLoadButton = new Button(container,SWT.PUSH);
        tryLoadButton.setLayoutData(gd);
        tryLoadButton.setText(CodegenWizardPlugin
                .getResourceString("page4.tryLoad.label"));
        tryLoadButton.addMouseListener(new MouseAdapter(){
        	public void mouseUp(MouseEvent e) {
        		java.util.List errorListener = new ArrayList();
        		if (!ClassFileReader.tryLoadingClass(getClassName(),
        				getClassPathList(),
        				errorListener)){
        			Iterator it = errorListener.iterator();
        			while(it.hasNext()){
        				Object nextObject = it.next();
        				String errorMessage = nextObject==null?CodegenWizardPlugin
				                .getResourceString("page4.unknownError.label"):nextObject.toString();
						updateStatus(errorMessage);
						updateStatusTextField(false,errorMessage);
        			}
        			
        		}else{
        			updateStatus(null);
        			updateStatusTextField(true,CodegenWizardPlugin
			                .getResourceString("page4.successLoading.label"));
        		}
        	}
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        statusLabel = new Label(container,SWT.NULL);
        statusLabel.setLayoutData(gd);
        
        setPageComplete(false);
        
        if (restoredFromPreviousSettings){
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
        	javaClasspathList.add(dirName);
        	updateListEntries();
        }
        updateStatusTextField(false,"");
    }
    
    
    /**
     * Pops up the file browse dialog box
     *  
     */
    private void handleRemove() {
        int[] selectionIndices = javaClasspathList.getSelectionIndices();
        for (int i=0;i<selectionIndices.length;i++){
        	javaClasspathList.remove(selectionIndices[i]);
        }
        updateListEntries();
        updateStatusTextField(false,"");
    }
    
   
    /**
     * Pops up the file browse dialog box
     *  
     */
    private void handleFileBrowse() {
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[]{"*.jar"});
        String fileName = fileDialog.open();
        if (fileName != null) {
        	javaClasspathList.add(fileName);
        	updateListEntries();
        }
        updateStatusTextField(false,"");

    }
    
    private void updateStatusTextField(boolean success,String text){
    	if (success){
    		getCodegenWizard().setDefaultNamespaces(javaClassNameBox.getText());
    	}
    	statusLabel.setText(text);
    }
    private void updateListEntries(){
    	settings.put(JAVA_CLASS_PATH_ENTRIES,javaClasspathList.getItems());
    }
    /**
     * 
     *
     */
    private void handleClassNameTextChange(){
        String className = javaClassNameBox.getText();
        settings.put(JAVA_CLASS_NAME,className);
        if (className==null || "".equals(className.trim())){
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page4.error.invalidClassName"));
        }else if(className.endsWith(".")){
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page4.error.ClassNameNotTerminated"));
        }else{
        	//just leave it
            //updateStatus(null);
        }
    }
    
    /**
     * 
     * @return
     */
    public String getClassName(){
        return javaClassNameBox.getText();
    }
    
    /**
     * 
     * @return
     */
    public String[] getClassPathList(){
        return javaClasspathList.getItems();
    }
    
   
}
