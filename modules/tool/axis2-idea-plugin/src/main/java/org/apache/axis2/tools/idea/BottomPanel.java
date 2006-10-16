package org.apache.axis2.tools.idea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 20, 2005
 * Time: 3:38:12 PM
 */
public class BottomPanel extends JPanel implements ActionListener {
    public static JButton btnNext;
    public static JButton btnFinish;
    public static JButton btnCancel;

    private Java2CodeFrame java2CodeFrame;

    public BottomPanel(Java2CodeFrame java2CodeFrame) {
        this.java2CodeFrame = java2CodeFrame;
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        BottomLayout customLayout = new BottomLayout();

        setLayout(customLayout);

        btnNext = new JButton("Next");
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

    public static void setEnable(boolean next, boolean finish, boolean cancel) {
        btnNext.setEnabled(next);
        btnFinish.setEnabled(finish);
        btnCancel.setEnabled(cancel);
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnCancel) {
            java2CodeFrame.setVisible(false);
            Thread.currentThread().setContextClassLoader(java2CodeFrame.getClassLoader());
        } else if (obj == btnFinish) {

            String selected = java2CodeFrame.optionPane.buttonGroup.getSelection().getActionCommand();

            if (selected.equalsIgnoreCase("radDefaultClient")) {
                    File temp = java2CodeFrame.optionPane.setDefaultClientConfigurations();
                    try {
                        java2CodeFrame.generateDefaultClientCode(temp);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        StringWriter writer = new StringWriter();
                        e1.printStackTrace(new PrintWriter(writer));
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                        java2CodeFrame.setVisible(false);
                    }

                    JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration successful!",
                            "Axis2 codegeneration", JOptionPane.INFORMATION_MESSAGE);
                    java2CodeFrame.setVisible(false);
                    return;

                }

           else if (selected.equalsIgnoreCase("radCustom")) {

                try {
                    java2CodeFrame.generatecode();

                } catch (Exception e1) {
                    e1.printStackTrace();
                    StringWriter writer = new StringWriter();
                    e1.printStackTrace(new PrintWriter(writer));
                    JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                            "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                    java2CodeFrame.setVisible(false);
                }
            } else if (selected.equalsIgnoreCase("radDefaultServer")) {

                File temp = java2CodeFrame.optionPane.setDefaultServerConfigurations();
                String output = java2CodeFrame.outputpane.buttonGroup.getSelection().getActionCommand();

                if (output.equalsIgnoreCase("radCurrentProject")) {

                    try {

                        java2CodeFrame.generateDefaultServerCode(temp, (String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        StringWriter writer = new StringWriter();
                        e1.printStackTrace(new PrintWriter(writer));
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                        java2CodeFrame.setVisible(false);
                    }


                } else {
                    String path = java2CodeFrame.outputpane.txtoutput.getText();
                    File outputPath = new File(path);
                    if (outputPath.exists()) {
                        try {
                            java2CodeFrame.generateDefaultServerCode(temp, outputPath.getAbsolutePath());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            StringWriter writer = new StringWriter();
                            e1.printStackTrace(new PrintWriter(writer));
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.setVisible(false);
                        }

                    } else {
                        JOptionPane.showMessageDialog(java2CodeFrame, "Invalid file path!",
                                "Error!!!", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }


                }
            } else if (selected.equalsIgnoreCase("radDefaultBoth")) {

                File temp = java2CodeFrame.optionPane.setDefaultServerConfigurations();
                String output = java2CodeFrame.outputpane.buttonGroup.getSelection().getActionCommand();

                if (output.equalsIgnoreCase("radCurrentProject")) {
                    try {

                        java2CodeFrame.generateDefaultServerCode(temp, (String) java2CodeFrame.outputpane.cmbModuleSrc.getSelectedItem());
                        File temp2 = java2CodeFrame.optionPane.setDefaultClientConfigurations();
                        java2CodeFrame.generateDefaultClientCode(temp2);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                        StringWriter writer = new StringWriter();
                        e1.printStackTrace(new PrintWriter(writer));
                        JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                        java2CodeFrame.setVisible(false);
                    }

                } else {
                    String path = java2CodeFrame.outputpane.txtoutput.getText();
                    File outputPath = new File(path);
                    if (outputPath.exists()) {
                        try {
                            java2CodeFrame.generateDefaultServerCode(temp, outputPath.getAbsolutePath());
                            File temp2 = java2CodeFrame.optionPane.setDefaultClientConfigurations();
                            java2CodeFrame.generateDefaultClientCode(temp2);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            StringWriter writer = new StringWriter();
                            e1.printStackTrace(new PrintWriter(writer));
                            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration failed!" + writer.toString(),
                                    "Axis2 codegeneration", JOptionPane.ERROR_MESSAGE);
                            java2CodeFrame.setVisible(false);
                        }

                    } else {
                        JOptionPane.showMessageDialog(java2CodeFrame, "Invalid file path!",
                                "Error!!!", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }


                }
            }

            JOptionPane.showMessageDialog(java2CodeFrame, "Code genaration successful!",
                    "Axis2 codegeneration", JOptionPane.INFORMATION_MESSAGE);
            java2CodeFrame.setVisible(false);

        } else if (obj == btnNext) {

            if (java2CodeFrame.optionPane.isVisible()) {

                String selected = java2CodeFrame.optionPane.buttonGroup.getSelection().getActionCommand();


                if (selected.equalsIgnoreCase("radDefaultServer") || selected.equalsIgnoreCase("radDefaultBoth")) {

                    java2CodeFrame.increasePanelID();

                }

            }

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
            c.setBounds(insets.left + 232, insets.top + 10, 80, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 312, insets.top + 10, 80, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 392, insets.top + 10, 80, 24);
        }
    }
}

