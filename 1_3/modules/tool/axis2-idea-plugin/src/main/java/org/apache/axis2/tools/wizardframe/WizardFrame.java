/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.tools.wizardframe;

import org.apache.axis2.tools.component.*;
import org.apache.axis2.tools.bean.WsdlgenBean;
import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.idea.ProgressBarPanel;
import org.apache.axis2.tools.idea.FirstPanel;
import org.apache.axis2.tools.idea.SecondPanel;
import org.apache.axis2.tools.idea.WSDL2JavaOutputPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TimerTask;
import java.io.*;

import com.intellij.openapi.project.Project;

/**
 * wizardFrame class
 */
public class WizardFrame extends JFrame {
     /**
     * variable
     */

    private JLabel panelImageLabel;
    private JLabel panelTopTitleLabel;
    private JLabel panelBottomTitleLabel;
    private JLabel errorLabel;
    private JPanel errorPanel;
    private WizardComponents wizardComponents;
    protected WsdlgenBean wsdlgenBean;
    protected CodegenBean codegenBean;
    protected Project project;
    private ProgressBarPanel progress;

    public WizardFrame() {
        init();
    }

    private void init() {

        wizardComponents = new DefaultWizardComponents();

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(createTitlePanel()
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL
                , new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(wizardComponents.getWizardPanelsContainer()
                , new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.BOTH
                , new Insets(10, 0, 0, 0), 0, 0));

        progress =new ProgressBarPanel();
        this.getContentPane().add(progress
                , new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL
                , new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL
                , new Insets(1, 1, 1, 1), 0, 0));

        this.getContentPane().add(createButtonPanel(),
                new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0
                        ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(10, 10, 10, 10), 0, 0));

        ImageIcon  img=new ImageIcon("icons/icon.png");
        this.setIconImage(img.getImage());
        this.setFont(new Font("Helvetica", Font.PLAIN, 8));

        wizardComponents.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                setPanelTopTitle(((WizardPanel)event.getNewValue()).getPanelTopTitle());
                setPanelTitleImage(((WizardPanel)event.getNewValue()).getPanelImage());
                setPanelBottomTitle(((WizardPanel)event.getNewValue()).getPanelBottomTitle());
                setErrorVisible(false
                        ,((WizardPanel)event.getNewValue()).getError());
            }
        });
        wizardComponents.setFinishAction(createFinishAction());
        wizardComponents.setCancelAction(createCancelAction());
        handleWindowClosing();
    }

    public WizardComponents getWizardComponents(){
        return wizardComponents;
    }

    public void setWizardComponents(WizardComponents aWizardComponents){
        wizardComponents = aWizardComponents;
    }

    public void show() {
        wizardComponents.updateComponents();
        super.show();
    }
      // set error message
    protected void setErrorVisible(boolean flag ,String  error){
        errorLabel.setText(error);
        errorPanel.setVisible(flag);

    }
    protected void setBottomVisible(boolean flag){
        panelBottomTitleLabel.setVisible(flag);
    }
    //Set Title Panel
    protected void setPanelTopTitle(String title) {
        panelTopTitleLabel.setText(title);
    }

    protected void setPanelBottomTitle(String title) {
        panelBottomTitleLabel.setText(title);
    }
    // set title image
    protected void setPanelTitleImage(ImageIcon image){
        panelImageLabel.setIcon(image );
    }

    protected JPanel createTitlePanel() {

        JPanel panel = new JPanel(new GridBagLayout() );

        panel.setBackground(Color.white );

        panelTopTitleLabel = new JLabel();
        panelTopTitleLabel.setFont(new Font("Helvetica", Font.BOLD, 12));

        panelBottomTitleLabel=new JLabel();

        panelImageLabel= new JLabel();

        errorLabel = new JLabel();
        errorLabel.setVisible(false);
        errorLabel.setBackground(Color.blue);

        errorPanel =new JPanel();
        errorPanel .setLayout(new GridBagLayout());
        errorPanel .setBorder(BorderFactory.createLineBorder(Color.black));

        errorPanel.add(errorLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH
                , new Insets(10, 10, 0, 10), 0, 0));

        panel.add(panelTopTitleLabel
                , new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0
                , GridBagConstraints.LINE_START , GridBagConstraints.BOTH
                , new Insets(10, 20, 0, 0), 0, 0));

        panel.add(panelBottomTitleLabel
                , new GridBagConstraints(0, 1, 1, 1, 0.5, 0.0
                , GridBagConstraints.LINE_START, GridBagConstraints.BOTH
                , new Insets(10,20, 0, 0), 0, 0));

        panel.add(panelImageLabel
                , new GridBagConstraints(1, 0, 1,2, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH
                , new Insets(0, 0, 0, 0), 0, 0));


        panel.add(errorPanel
                , new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.BOTH
                , new Insets(20, 0, 0, 0), 0, 0));

        return panel;

    }
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(wizardComponents.getBackButton());
        panel.add(wizardComponents.getNextButton());
        panel.add(wizardComponents.getFinishButton());
        panel.add(wizardComponents.getCancelButton());
        return panel;
    }

    protected FinishAction createFinishAction() {
        return new FinishAction(wizardComponents) {
            public void performAction() {
                System.out.println("FinishAction performed");
                try {
                    wizardComponents.getCurrentPanel().update();
                    switch (wizardComponents.getCurrentPanel().getPageType()) {
                        case WizardPanel .WSDL_2_JAVA_TYPE:
                            doFinishWSDL2Java();
                            System.out.println("FinishAction  WSDL2Java performed");
                            break;
                        case WizardPanel.JAVA_2_WSDL_TYPE:
                            doFinishJava2WSDL();
                            System.out.println("FinishAction Java2WSDL performed");
                            break;
                        case WizardPanel.UNSPECIFIED_TYPE:
                            break; //Do nothing
                        default:
                            throw new RuntimeException("Invalid state!");
                    }
                } catch (Exception e) {
                     dispose();
                }

            }
        };
    }

    protected CancelAction createCancelAction() {
        return new CancelAction(wizardComponents) {
            public void performAction() {
                System.out.println("CancelAction performed");
                dispose();
            }
        };
    }

    protected void handleWindowClosing() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                wizardComponents.getCancelAction().performAction();
            }
        });
    }

    protected void handlePragress(){
        wizardComponents.getFinishButton().setEnabled(false);
        progress.setVisible(true);
        progress.aboutToDisplayPanel();
        progress.displayingPanel();
        new java.util.Timer(true).schedule(new TimerTask() {
            public void run() {
                progress.requestStop();
            }
        }, 1000);
    }

    protected void handleSuccess(){
        StringWriter writer = new StringWriter();
        JOptionPane.showMessageDialog(this ,
                "Code genaration Successful !" + writer.toString(),
                "Axis2 code generation",
                JOptionPane.INFORMATION_MESSAGE );
        dispose();
    }

    protected void handleError(){
        wizardComponents.getFinishButton().setEnabled(true);
        StringWriter writer = new StringWriter();
        JOptionPane.showMessageDialog(this ,
                "Code genaration failed! !" + writer.toString(),
                "Axis2 code generation",
                JOptionPane.ERROR_MESSAGE );
    }

    private void addLibsToProjectLib(String libDirectory, String outputLocation){
        String newOutputLocation = outputLocation+File.separator+"lib";
        //Create a lib directory; all ancestor directories must exist
        boolean success = (new File(newOutputLocation)).mkdir();
        if (!success) {
            // Directory creation failed
        }
        try {
            copyDirectory(new File(libDirectory),new File(newOutputLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.

    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                        new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    // Copies src file to dst file.
    // If the dst file does not exist, it is created
    private void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    protected void doFinishWSDL2Java(){
        handlePragress();
        new java.util.Timer(true).schedule(new TimerTask() {
            public void run() {
                try {
                    FirstPanel  first=(FirstPanel)wizardComponents.getWizardPanel(1);
                    SecondPanel option=(SecondPanel)wizardComponents.getWizardPanel(3);
                    WSDL2JavaOutputPanel output=(WSDL2JavaOutputPanel)wizardComponents.getWizardPanel(5);
                    boolean isServerside,isServiceXML,isGenerateServerSideInterface  = false;
                    if (option.getGenerateAll()){
                        isServerside = true;
                        isServiceXML = true;
                        isGenerateServerSideInterface = true;
                    }else{
                        isServerside = option.isServerside();
                        isServiceXML =option.isServerXML();
                        isGenerateServerSideInterface = option.getGenerateServerSideInterface();
                    }
                    codegenBean.setPackageName(option.getPackageName());
                    codegenBean.setLanguage(option.getSelectedLanguage());
                    codegenBean.setPortName(option.getPortName());
                    codegenBean.setServiceName(option.getServiceName());
                    codegenBean.setDatabindingName(option.getDatabinderName());
                    codegenBean.setAsyncOnly(option.isAsyncOnlyOn());
                    codegenBean.setSyncOnly(option.isSyncOnlyOn());
                    codegenBean.setTestCase(option.isGenerateTestCase());
                    codegenBean.setGenerateAll(option.getGenerateAll());
                    codegenBean.setServerXML(isServiceXML);
                    codegenBean.setServerSide(isServerside);
                    codegenBean.setServerSideInterface(isGenerateServerSideInterface);
                    codegenBean.setOutput(output.getOutputLocation());
                    codegenBean.setNamespace2packageList(option.getNs2PkgMapping());
                    codegenBean.setWSDLFileName(first.getWSDLFileName());
                    codegenBean.generate();
                    progress.setVisible(false);
                    handleSuccess();
                }catch (Exception e1) {
                    progress.setVisible(false);
                    handleError();
                }
            }
        }, 1000);

    }

    protected void doFinishJava2WSDL(){
        handlePragress();
        new java.util.Timer(true).schedule(new TimerTask() {
            public void run() {
                try {
                    wsdlgenBean.generate();
                    progress.setVisible(false);
                    handleSuccess();
                } catch (Exception e1) {
                    progress.setVisible(false);
                    handleError();
                }
            }
        }, 3100);
    }
    public void setProject(Project project){
        this.project=project;
    }
    public JComponent getRootComponent() {
        return this.getRootPane();
    }
    public void showUI() {
        pack();
        this.setVisible(true);
        show();
    }

}
