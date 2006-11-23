package org.apache.ideaplugin.frames;

import com.intellij.openapi.project.Project;
import org.apache.axis2.tools.idea.Java2CodeFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * Author: Deepal Jayasinghe
 * Date: Sep 24, 2005
 * Time: 10:41:41 AM
 */
public class Axi2PluginPage extends JFrame implements ActionListener {
    ButtonGroup cbg;
    JRadioButton service;
    JRadioButton javawsdl;
    JButton butOK;
    JButton butCancle;
    JPanel imglbl;
    Project project;
    Java2CodeFrame win;

    public Axi2PluginPage() {
        setBackground(Color.white);
        Dimension dim = getPreferredSize();
        setSize(dim);
        setBounds(200, 200, dim.width, dim.height);
        setBounds(200, 200, dim.width, dim.height);
        Axi2PluginPageLayout customLayout = new Axi2PluginPageLayout();

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(customLayout);
        setTitle("Axis2 Plugin");
        cbg = new ButtonGroup();
        service = new JRadioButton("Create a service archive", true);
        service.setToolTipText("Hepls package classes, libs and WSDLs to create a archive that can be deployed in Axis2");
        cbg.add(service);
        getContentPane().add(service);

        javawsdl = new JRadioButton("WSDL2Code code generation", false);
        javawsdl.setToolTipText("Helps generate skeletons and stubs for a given WSDL");
        cbg.add(javawsdl);
        getContentPane().add(javawsdl);

        butOK = new JButton("OK");
        butOK.addActionListener(this);
        getContentPane().add(butOK);

        butCancle = new JButton("Cancel");
        butCancle.addActionListener(this);
        getContentPane().add(butCancle);

        imglbl = new LogoPage();
        getContentPane().add(imglbl);

    }

    public void showUI() {
        pack();
        this.setVisible(true);
        show();
    }

    public void setProject(Project project) {

        this.project = project;
    }


    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == butCancle) {
            this.hide();
            setVisible(false);
        } else if (obj == butOK) {
            this.hide();
            setVisible(false);
            if (javawsdl.isSelected()) {

                win = new Java2CodeFrame();
                win.setResizable(false);
                win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                win.setProject(project);
                win.pack();
                win.show();
                
            } else {
                ServiceArciveFrame window = new ServiceArciveFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setResizable(false);
                window.setTitle("Service Archive creation");
                window.show();
            }
        }

    }

    public JComponent getRootComponent() {
        return this.getRootPane();
    }
}

class Axi2PluginPageLayout implements LayoutManager {

    public Axi2PluginPageLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 320 + insets.left + insets.right;
        dim.height = 240 + insets.top + insets.bottom;

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
            c.setBounds(insets.left + 24, insets.top + 104, 208, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 136, 208, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 130, insets.top + 200, 80, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 215, insets.top + 200, 80, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top, 320, 80);
        }
    }
}
