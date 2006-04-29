package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;

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
 * Time: 10:52:36 PM
 */
public class SecondPanel extends JPanel {
    JLabel lblol;
    JComboBox comlanguage;
    ButtonGroup cbg;
    JRadioButton rdsynasyn;
    JRadioButton rdsyn;
    JRadioButton rdasync;
    JLabel lblpakage;
    JTextField txtpackage;
    private CodegenBean codegenBean;

    JLabel databiding;
    JComboBox databindingType;
    JComboBox servicenames;
    JLabel serviceNames ;
    JComboBox portNames;

    public SecondPanel(CodegenBean codegenBean) {
        this.codegenBean = codegenBean;
        SecondPanelLayout customLayout = new SecondPanelLayout();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        lblol = new JLabel("   Select the output language");
        add(lblol);

        comlanguage = new JComboBox();
        comlanguage.addItem("java");
        comlanguage.addItem("C#");
        add(comlanguage);

        cbg = new ButtonGroup();
        rdsynasyn = new JRadioButton(" Generate both sync and async", true);
        cbg.add(rdsynasyn);
        add(rdsynasyn);

        rdsyn = new JRadioButton(" Generate sync only", false);
        cbg.add(rdsyn);
        add(rdsyn);

        rdasync = new JRadioButton(" Generate async only", false);
        cbg.add(rdasync);
        add(rdasync);

        databiding = new JLabel("Select Databinding type");
        add(databiding);
        databindingType = new JComboBox();
        databindingType.addItem("adb");
        databindingType.addItem("xmlbeans");
        databindingType.addItem("none");
        databindingType.setEnabled(true);
        add(databindingType);

        lblpakage = new JLabel("Set the package name");
        add(lblpakage);

        txtpackage = new JTextField("org.axis2");
        add(txtpackage);

        servicenames = new JComboBox();
        add(servicenames);

        portNames = new JComboBox();
        add(portNames);


        setSize(getPreferredSize());

    }

    public void fillBean() {
        int index = comlanguage.getSelectedIndex();
        switch (index) {
            case 0: {
                codegenBean.setLanguage("java");
                break;
            }
            case 1: {
                codegenBean.setLanguage("c-sharp");
                break;
            }
        }

        index = databindingType.getSelectedIndex();
        switch (index) {
            case 0: {
                codegenBean.setDatabindingName("adb");
                break;
            }
            case 1: {
                codegenBean.setDatabindingName("xmlbeans");
                break;
            }
            case 2: {
                codegenBean.setDatabindingName("none");
                break;
            }
        }

        if (rdasync.isSelected()) {
            codegenBean.setAsyncOnly(true);
        } else if (rdsyn.isSelected()) {
            codegenBean.setSyncOnly(true);
        } else {
            codegenBean.setSyncOnly(false);
            codegenBean.setAsyncOnly(false);
        }
        codegenBean.setPackageName(txtpackage.getText());
    }
}

class SecondPanelLayout implements LayoutManager {

    public SecondPanelLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 565 + insets.left + insets.right;
        dim.height = 235 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top + 5, 200, 20);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 5, 160, 20);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 30, 250, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 280, insets.top + 30, 150, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 55, 150, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 80, 200, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 80, 160, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 110, 200, 24);
        }
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 110, 200, 24);
        }
    }
}

