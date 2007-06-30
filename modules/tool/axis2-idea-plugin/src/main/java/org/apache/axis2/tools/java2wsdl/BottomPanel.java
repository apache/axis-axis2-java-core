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
package org.apache.axis2.tools.java2wsdl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;


public class BottomPanel extends JPanel implements ActionListener {
    public static JButton btnBack;
    public static JButton btnNext;
    public static JButton btnFinish;
    public static JButton btnCancel;
    // public static

    private Java2WSDLFrame java2WSDLFrame;
    final JFileChooser fc=new JFileChooser();

    public BottomPanel(Java2WSDLFrame java2WSDLFrame) {

        this.java2WSDLFrame = java2WSDLFrame;

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
        if (obj == btnNext) {
            if (java2WSDLFrame.plMiddle .isVisible() ) {

                if(java2WSDLFrame.plMiddle.testLoading()){

                    java2WSDLFrame.setPanel();
                }else
                    setEnable(false,false,false,true);

            }else if(java2WSDLFrame.opPanel.isVisible()){

                if( java2WSDLFrame.opPanel .txtService .getText().trim().equals("")){
                    JOptionPane.showMessageDialog(java2WSDLFrame,
                            " Service name should be specified. ",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    java2WSDLFrame.repaint();
                    setEnable(true,false,false,true);
                    return;
                }else{
                    setEnable(true,true,true,true);
                    java2WSDLFrame.setPanel();
                }

            }

        } else if(obj == btnBack){

            java2WSDLFrame.backButtonImpl();

        } else if (obj == btnCancel) {

            java2WSDLFrame.dispose();
            Thread.currentThread().setContextClassLoader(java2WSDLFrame.getClassLoader());

        }  else if(obj ==btnFinish ){

            java2WSDLFrame.outPanel.setOutput();
            java2WSDLFrame.opPanel .setNamespaceDefaults();

            File outputDir = new File(java2WSDLFrame.outPanel .txtLocation .getText().trim());
            String outputName=java2WSDLFrame.outPanel.txtFileName.getText().trim();

            if(java2WSDLFrame.opPanel.txtService .getText() .trim() .equals("") ){
                JOptionPane.showMessageDialog(java2WSDLFrame,
                        " Service name should be specified. ",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
                java2WSDLFrame.repaint();
                setEnable(true,false,false,true);
                return;
            }

            if (java2WSDLFrame.outPanel .rbtnSave .isSelected() )
            {
                if(!outputDir.isDirectory() && !new FileFilter() .accept(outputName )){
                    JOptionPane.showMessageDialog(java2WSDLFrame,
                            "Input a proper location for the output and name for WSDL.",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    java2WSDLFrame.repaint();
                    setEnable(true,false,false,true);
                    return;
                }
                if (!outputDir.isDirectory())
                {
                    JOptionPane.showMessageDialog(java2WSDLFrame,
                            "The Output Directory specified is invalid. Please provide a valid directory",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    java2WSDLFrame.repaint();
                    setEnable(true,false,false,true);
                    return;
                }
                if(!new FileFilter() .accept(outputName ) ){
                    JOptionPane.showMessageDialog(java2WSDLFrame,
                            "Input a valid file name , Example : services.wsdl or services.xml",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    java2WSDLFrame.repaint();
                    setEnable(true,false,false,true);
                    return;
                }
            }
            try {
                java2WSDLFrame.generatecode();
                StringWriter writer = new StringWriter();
                JOptionPane.showMessageDialog(java2WSDLFrame,
                        "Code genaration Successful !" + writer.toString(),
                        "Axis2 code generation",
                        JOptionPane.INFORMATION_MESSAGE );

                java2WSDLFrame.dispose();

            } catch (Exception e1) {

                StringWriter writer = new StringWriter();
                JOptionPane.showMessageDialog(java2WSDLFrame,
                        "Code genaration failed!" + writer.toString(),
                        "Axis2 code generation",
                        JOptionPane.ERROR_MESSAGE);
                java2WSDLFrame.dispose();

            }
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
        dim.width = 475 + insets.left + insets.right;
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
            c.setBounds(insets.left + 395, insets.top + 10, 80, 24);
        }
    }
}
