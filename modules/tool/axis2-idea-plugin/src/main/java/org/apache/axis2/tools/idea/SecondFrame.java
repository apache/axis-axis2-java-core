package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import javax.swing.border.BevelBorder;
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

    JLabel lblServiceName;
    JComboBox cmbServiceName;

    JLabel lblportName;
    JComboBox cmbPortName;

    JLabel lblpackgeName;
    JTextField txtPacakgeName;

    JLabel lbldbtype;
    JComboBox cmbdbtype;

    JCheckBox chkTestCase;

    JLabel lblClient;

    JRadioButton rdBoth;
    JRadioButton rdsyn;
    JRadioButton rdasync;

    JLabel lblServer;

    JCheckBox serverside;
    JCheckBox serviceXML;

    ButtonGroup buttonGroup;

    private CodegenBean codegenBean;
    java.util.List serviceNameList;

    public SecondFrame() {
        SecondFrameLayout customLayout = new SecondFrameLayout();

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        BevelBorder b = new BevelBorder(BevelBorder.LOWERED);
        setBorder(b);

        lblLangauge = new JLabel("Select the output language");
        add(lblLangauge);

        cmbLan = new JComboBox();
        cmbLan.addItem("java");
        cmbLan.addItem("C#");
        cmbLan.setToolTipText("Select the language of the generated code");
        add(cmbLan);

        lblServiceName = new JLabel("Select ServiceName");
        add(lblServiceName);

        cmbServiceName = new JComboBox();
        add(cmbServiceName);
        cmbServiceName.setToolTipText("Select the name of the service that the code should be generated for");
        cmbServiceName.addActionListener(this);

        lblportName = new JLabel("Select Port Name");
        add(lblportName);

        cmbPortName = new JComboBox();
        cmbPortName.setToolTipText("Select the port name that the code should be generated for");
        add(cmbPortName);

        lblpackgeName = new JLabel("Select the package name");
        add(lblpackgeName);

        txtPacakgeName = new JTextField("org.axis2");
        txtPacakgeName.setToolTipText("Set the package name of the generated code");
        add(txtPacakgeName);

        lbldbtype = new JLabel("Select Databinding type");
        add(lbldbtype);

        cmbdbtype = new JComboBox();
        cmbdbtype.addItem("adb");
        cmbdbtype.addItem("xmlbeans");
        cmbdbtype.addItem("none");
        cmbdbtype.setToolTipText("Select the databinding framework to be used in the generation process");
        add(cmbdbtype);

        chkTestCase = new JCheckBox("Generate Test Case", true);
        chkTestCase.setToolTipText("A test case will be generated if this is checked");
        add(chkTestCase);

        lblClient = new JLabel("Client Side Options");
        add(lblClient);

        buttonGroup = new ButtonGroup();

        rdBoth = new JRadioButton("Generate both sync and async", true);
        buttonGroup.add(rdBoth);
        add(rdBoth);

        rdsyn = new JRadioButton("Generate sync only", false);
        buttonGroup.add(rdsyn);
        add(rdsyn);

        rdasync = new JRadioButton("Generate async only", false);
        buttonGroup.add(rdasync);
        add(rdasync);

        lblServer = new JLabel("Server Side Options");
        add(lblServer);

        serverside = new JCheckBox("Generate server side ", true);
        add(serverside);

        serviceXML = new JCheckBox("Generate default service.xml", true);
        add(serviceXML);

        Dimension dim = new Dimension(450, 300);
        setSize(dim);
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
        if (chkTestCase.isSelected()) {
            codegenBean.setTestCase(true);
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
        dim.height = 450 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;

        // Language selection
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 10, 192, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 10, 176, 24);
        }

        // Service Name selection
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 40, 192, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 40, 176, 24);
        }

        // Port Name Selection
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 70, 192, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 70, 176, 24);
        }

        //Package NAme Selection
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 100, 192, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 100, 176, 24);
        }

        // Data Binding Selection
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 130, 192, 24);
        }
        c = parent.getComponent(9);
        if (c.isVisible()) {
            c.setBounds(insets.left + 272, insets.top + 130, 176, 24);
        }

        // Test Case Selection
        c = parent.getComponent(10);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 160, 208, 24);
        }

        // Client side options
        c = parent.getComponent(11);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 190, 168, 24);
        }

        // Service invocation both,sync,async
        c = parent.getComponent(12);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 220, 220, 24);
        }
        c = parent.getComponent(13);
        if (c.isVisible()) {
            c.setBounds(insets.left + 230, insets.top + 220, 140, 24);
        }
        c = parent.getComponent(14);
        if (c.isVisible()) {
            c.setBounds(insets.left + 380, insets.top + 220, 160, 24);
        }

        // Server side options
        c = parent.getComponent(15);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 250, 168, 24);
        }

        // Generate serverside, generate service XML
        c = parent.getComponent(16);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 280, 200, 24);
        }
        c = parent.getComponent(17);
        if (c.isVisible()) {
            c.setBounds(insets.left + 228, insets.top + 280, 200, 24);
        }


    }
}

