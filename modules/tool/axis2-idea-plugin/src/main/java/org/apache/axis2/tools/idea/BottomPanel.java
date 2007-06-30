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
package org.apache.axis2.tools.idea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.util.TimerTask;

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 20, 2005
 * Time: 3:38:12 PM
 */
public class BottomPanel extends JPanel implements ActionListener {
    public static JButton btnBack;
    public static JButton btnNext;
    public static JButton btnFinish;
    public static JButton btnCancel;

    private Java2CodeFrame java2CodeFrame;

    public BottomPanel(Java2CodeFrame java2CodeFrame) {
        this.java2CodeFrame = java2CodeFrame;
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        BottomLayout customLayout = new BottomLayout();

        setLayout(customLayout);

        btnBack = new JButton("< Back");
        btnBack.setEnabled(true);
        btnBack.addActionListener(this);
        add(btnBack);

        btnNext = new JButton("Next >");
        btnNext.addActionListener(this);
        add(btnNext);

        btnFinish = new JButton("Finish");
        btnFinish.addActionListener(this);
        add(btnFinish);

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        add(btnCancel);

        setSize(getPreferredSize());

    }

    public static void setEnable(boolean back,boolean next, boolean finish, boolean cancel) {
        btnBack.setEnabled(back);
        btnNext.setEnabled(next);
        btnFinish.setEnabled(finish);
        btnCancel.setEnabled(cancel);
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnBack) {
            java2CodeFrame.backButtonImpl();
        }
        else if (obj == btnCancel) {
            java2CodeFrame.dispose();
            Thread.currentThread().setContextClassLoader(java2CodeFrame.getClassLoader());
            return;
        } else if (obj == btnFinish) {

            File outputDir = new File(java2CodeFrame.outputpane.txtoutput.getText().trim());
            if (java2CodeFrame.outputpane.radCustomLocation.isSelected() )
            {
                if (!outputDir.isDirectory())
                {
                    JOptionPane.showMessageDialog(java2CodeFrame, "The Output Directory specified is invalid. Please provide a valid directory",
                            "Error!", JOptionPane.ERROR_MESSAGE);
                    java2CodeFrame.repaint();
                    return;
                }
            }

            final String selected = java2CodeFrame.secondPanel.cmbCodeGenOption.getSelectedItem().toString() ;
            java2CodeFrame.outputpane.progressBar.setVisible(true);
            final BarThread stepper = new BarThread(java2CodeFrame.outputpane.progressBar);
            stepper.start();
            new java.util.Timer(true).schedule(new TimerTask() {
                public void run() {
                    stepper.requestStop();

                }
            }, 1000);

            new Thread(){public void run(){
                if (selected.equalsIgnoreCase("default")) {

                    String output = java2CodeFrame.outputpane.buttonGroup.getSelection().getActionCommand();
                    java2CodeFrame.secondPanel.setDefaultCommonConfigurations();

                    if (output.equalsIgnoreCase("radCurrentProject")) {

                        File temp = java2CodeFrame.secondPanel.codegenBean.getTemp();
                        java2CodeFrame.secondPanel.codegenBean.setOutput(temp.getAbsolutePath() );
                        try {
                            java2CodeFrame.generatecode() ;
                            java2CodeFrame.copyDirectory(new File(temp + File.separator + "src"), new File((String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem()));
                            File src = new File(temp + File.separator + "resources");
                            if (src.isDirectory())
                                java2CodeFrame.copyDirectory(src, new File((String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem() + File.separator + ".." + File.separator + "resources"));

                            java2CodeFrame.deleteDirectory(temp);
                        } catch (Exception e1) {
                            java2CodeFrame.outputpane.progressBar.setVisible(false);
                            StringWriter writer = new StringWriter();
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 code generation", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.dispose();
                            return;
                        }
                        java2CodeFrame.outputpane.progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code generation successful!",
                                "Axis2 code generation", JOptionPane.INFORMATION_MESSAGE);
                        java2CodeFrame.dispose();
                        return;
                    }else if(output.equalsIgnoreCase("radCustomLocation") ){
                        try{
                            java2CodeFrame.generatecode();
                        } catch (Exception e1) {
                            java2CodeFrame.outputpane.progressBar.setVisible(false);
                            StringWriter writer = new StringWriter();
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 code generation", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.dispose();
                            return;
                        }
                        java2CodeFrame.outputpane.progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code generation successful!",
                                "Axis2 code generation", JOptionPane.INFORMATION_MESSAGE);
                        java2CodeFrame.dispose();
                        return;
                    }
                }

                else if (selected.equalsIgnoreCase("custom")) {

                    String output = java2CodeFrame.outputpane.buttonGroup.getSelection().getActionCommand();
                    java2CodeFrame.secondPanel.fillBean();
                    if (output.equalsIgnoreCase("radCurrentProject")) {

                        File temp = java2CodeFrame.secondPanel.codegenBean.getTemp();
                        java2CodeFrame.secondPanel.codegenBean.setOutput(temp.getAbsolutePath());
                        //java2CodeFrame.secondPanel.codegenBean.setProject();
                        try {
                            java2CodeFrame.generatecode();
                            java2CodeFrame.copyDirectory(new File(temp + File.separator + "src"), new File((String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem()));
                            File src = new File(temp + File.separator + "resources");
                            if (src.isDirectory())
                                java2CodeFrame.copyDirectory(src, new File((String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem() + File.separator + ".." + File.separator + "resources"));

                            java2CodeFrame.deleteDirectory(temp);
                        } catch (Exception e1) {
                            StringWriter writer = new StringWriter();
                            java2CodeFrame.outputpane.progressBar.setVisible(false);
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 code generation", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.dispose();
                            return;
                        }
                        java2CodeFrame.outputpane.progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code generation successful!",
                                "Axis2 code generation", JOptionPane.INFORMATION_MESSAGE);
                        java2CodeFrame.dispose();
                        return;
                    }
                    else if(output.equalsIgnoreCase("radCustomLocation"))
                    {
                        try {
                            java2CodeFrame.generatecode();
                        } catch (Exception e1) {
                            java2CodeFrame.outputpane.progressBar.setVisible(false);
                            StringWriter writer = new StringWriter();
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 code generation", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.dispose();
                            return;
                        }
                        java2CodeFrame.outputpane.progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code generation successful!",
                                "Axis2 code generation", JOptionPane.INFORMATION_MESSAGE);
                        java2CodeFrame.dispose();
                        return;
                    }
                }
            }
            }.start();

        } else if (obj == btnNext) {

            java2CodeFrame.setPane();
        }
    }
}

class BottomLayout implements LayoutManager {

    public BottomLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 541 + insets.left + insets.right;
        dim.height = 60 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 152, insets.top + 10, 80, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 232, insets.top + 10, 80, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 312, insets.top + 10, 80, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 392, insets.top + 10, 80, 24);
        }
    }
}

