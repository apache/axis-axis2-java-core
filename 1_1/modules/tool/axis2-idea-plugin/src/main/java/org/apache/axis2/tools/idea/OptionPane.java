package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import com.intellij.openapi.module.Module;


public class OptionPane extends JPanel implements ActionListener {


    JLabel lblOption;
    JRadioButton radDefaultClient;
    JRadioButton radDefaultServer;
    JRadioButton radDefaultBoth;

    JRadioButton radCustom;
    ButtonGroup buttonGroup;

    CodegenBean codegenBean;

    QName serviceName;
    String portName;

    public OptionPane() {
        OptionPaneLayout customLayout = new OptionPaneLayout();


        setLayout(customLayout);

        lblOption = new JLabel("");
        add(lblOption);

        buttonGroup = new ButtonGroup();

        radDefaultBoth = new JRadioButton("Generate both client and server code with default configurations", true);
        buttonGroup.add(radDefaultBoth);
        radDefaultBoth.setActionCommand("radDefaultBoth");
        add(radDefaultBoth);
        radDefaultBoth.addActionListener(this);

        radDefaultClient = new JRadioButton("Generate client code with default configurations");
        radDefaultClient.setToolTipText("Generates the jar that contains the stub and places it in the lib folder under the project. The generated jar is added as a project dependancy ");
        buttonGroup.add(radDefaultClient);
        radDefaultClient.setActionCommand("radDefaultClient");
        add(radDefaultClient);
        radDefaultClient.addActionListener(this);

        radDefaultServer = new JRadioButton("Generate Server code with default configurations");
        buttonGroup.add(radDefaultServer);
        radDefaultServer.setActionCommand("radDefaultServer");
        radDefaultServer.setToolTipText("Generates the skeleton for the service and places it in a path specified by the user");
        add(radDefaultServer);
        radDefaultServer.addActionListener(this);

        radCustom = new JRadioButton("Custom");
        buttonGroup.add(radCustom);
        radCustom.setActionCommand("radCustom");
        radCustom.setToolTipText("Allows the user to choose custom settings for the generation process");
        add(radCustom);
        radCustom.addActionListener(this);


        setSize(getPreferredSize());

    }


    public void actionPerformed(ActionEvent e) {

        Object obj = e.getSource();

        if (obj == radDefaultBoth) {
            BottomPanel.setEnable(true,true, false, true);
        } else if (obj == radDefaultClient) {
            BottomPanel.setEnable(true,false, true, true);
        } else if (obj == radDefaultServer) {
            BottomPanel.setEnable(true,true, false, true);
        } else if (obj == radCustom) {
            BottomPanel.setEnable(true,true, false, true);
        }

    }

    public void setDefaultBothConfigurations() {

        setDefaultCommonConfigurations();
    }

    public void setDefaultCommonConfigurations() {

        codegenBean.setLanguage("java");

        codegenBean.setDatabindingName("adb");

        codegenBean.setTestCase(false);

        codegenBean.setServiceName(serviceName.getLocalPart());

        codegenBean.setPortName(portName);

        codegenBean.setPackageName("org.axis2");


    }

    public File setDefaultServerConfigurations() {
        setDefaultCommonConfigurations();
        codegenBean.setServerSide(true);
        codegenBean.setServerXML(true);
        File temp = codegenBean.getTemp();
        codegenBean.setOutput(temp.getAbsolutePath());
        return temp;

    }

    public File setDefaultClientConfigurations() {
        setDefaultCommonConfigurations();
        codegenBean.setServerSide(false);
        codegenBean.setServerXML(false);
        codegenBean.setPackageName(codegenBean.packageFromTargetNamespace());
        File temp = codegenBean.getTemp();
        codegenBean.setOutput(temp.getAbsolutePath());

        return temp;
    }

    public void setCodeGenBean(CodegenBean codegenBean) {
        this.codegenBean = codegenBean;
        java.util.List serviceList = new ArrayList();
        java.util.List portList = new ArrayList();

        serviceList = codegenBean.getServiceList();
        if (serviceList.size() > 0) {
            serviceName = (QName) serviceList.get(0);
            portList = codegenBean.getPortNameList(serviceName);
        }
        if (portList.size() > 0)
            portName = (String) portList.get(0);

        Module modules[] = codegenBean.getModules();
        if (modules == null){
            radDefaultBoth.setEnabled(false);
            radDefaultClient.setEnabled(false);
            radDefaultServer.setEnabled(false);
            radCustom.setSelected(true);
        }
    }


    class OptionPaneLayout implements LayoutManager {
        public void removeLayoutComponent(Component comp) {
        }

        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();

            Component c;
            c = parent.getComponent(0);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 8, 500, 24);
            }
            c = parent.getComponent(1);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 40, 500, 24);
            }
            c = parent.getComponent(2);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 70, 500, 24);
            }
            c = parent.getComponent(3);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 100, 500, 24);
            }
            c = parent.getComponent(4);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 130, 500, 24);
            }

        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public Dimension minimumLayoutSize(Container parent) {
            return null;
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = new Dimension(0, 0);

            Insets insets = parent.getInsets();
            dim.width = 565 + insets.left + insets.right;
            dim.height = 600 + insets.top + insets.bottom;

            return dim;
        }
    }
}
