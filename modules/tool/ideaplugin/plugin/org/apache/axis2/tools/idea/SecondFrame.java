package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import javax.xml.namespace.QName;
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

public class SecondFrame extends JPanel implements ActionListener {

    JLabel lblLangauge;
    JComboBox cmbLan;
    JCheckBox serverside;
    JCheckBox rdsyn;
    JCheckBox rdasync;
    JLabel lbldbtype;
    JComboBox cmbdbtype;
    JLabel lblServiceName;
    JComboBox cmbServiceName;
    JLabel lblportName;
    JComboBox cmbPortName;
    JLabel lblpackgeName;
    JTextField txtPacakgeName;
    private CodegenBean codegenBean;
    java.util.List serviceNameList;

    public SecondFrame() {
        SecondFrameLayout customLayout = new SecondFrameLayout();

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        lblLangauge = new JLabel("Select the output language");
        add(lblLangauge);

        cmbLan = new JComboBox();
        cmbLan.addItem("java");
        cmbLan.addItem("C#");
        add(cmbLan);

        serverside = new JCheckBox("Generate server side ", true);
        add(serverside);

        rdsyn = new JCheckBox("Generate sync", false);
        add(rdsyn);

        rdasync = new JCheckBox("Generate async", false);
        add(rdasync);

        lbldbtype = new JLabel("Select Databinding type");
        add(lbldbtype);

        cmbdbtype = new JComboBox();
        cmbdbtype.addItem("adb");
        cmbdbtype.addItem("xmlbeans");
        cmbdbtype.addItem("none");
        add(cmbdbtype);

        lblServiceName = new JLabel("Select ServiceName");
        add(lblServiceName);

        cmbServiceName = new JComboBox();
        add(cmbServiceName);
        cmbServiceName.addActionListener(this);

        lblportName = new JLabel("Select Port Name");
        add(lblportName);

        cmbPortName = new JComboBox();
        add(cmbPortName);

        lblpackgeName = new JLabel("Select the package name");
        add(lblpackgeName);

        txtPacakgeName = new JTextField("org.axis2");
        add(txtPacakgeName);

        setSize(getPreferredSize());
    }

    public void setCodeGenBean(CodegenBean codegenBean) {
        this.codegenBean = codegenBean;
        codegenBean.readWSDL();
        serviceNameList = codegenBean.getServiceList();
        for (int i = 0; i < serviceNameList.size(); i++) {
            QName name = (QName) serviceNameList.get(i);
            cmbServiceName.addItem(name.getLocalPart());
        }
    }

    public void fillBean() {
        int index = cmbLan.getSelectedIndex();
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

        index = cmbdbtype.getSelectedIndex();
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
        }
        if (rdsyn.isSelected()) {
            codegenBean.setSyncOnly(true);
        }
        if (serverside.isSelected()) {
            codegenBean.setServerSide(true);
        }
        codegenBean.setPackageName(txtPacakgeName.getText());
        codegenBean.setServiceName(cmbServiceName.getSelectedItem().toString());
        codegenBean.setServiceName(cmbPortName.getSelectedItem().toString());
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == cmbServiceName) {
            int selindex = cmbServiceName.getSelectedIndex();
            java.util.List ports = codegenBean.getPortNameList((QName) serviceNameList.get(selindex));
            cmbPortName.removeAllItems();
            for (int i = 0; i < ports.size(); i++) {
                String portName = (String) ports.get(i);
                cmbPortName.addItem(portName);
            }
        }
    }
}

class SecondFrameLayout implements LayoutManager {

    public SecondFrameLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 575 + insets.left + insets.right;
        dim.height = 268 + insets.top + insets.bottom;

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
            c.setBounds(insets.left + 8, insets.top + 8, 192, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 264, insets.top + 8, 184, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 32, 208, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 220, insets.top + 32, 168, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 385, insets.top + 32, 144, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 60, 192, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 60, 176, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 90, 192, 24);
        }
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 90, 176, 24);
        }
        c = parent.getComponent(9);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 120, 192, 24);
        }
        c = parent.getComponent(10);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 120, 176, 24);
        }
        c = parent.getComponent(11);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 154, 192, 24);
        }
        c = parent.getComponent(12);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 154, 176, 24);
        }
    }
}

