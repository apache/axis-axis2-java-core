package org.apache.axis2.tools.idea;

import com.intellij.openapi.module.Module;
import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
 * Date: Jul 22, 2005
 * Time: 12:52:31 PM
 */
public class OutPutPane extends JPanel implements ActionListener {
    JTextField txtoutput;
    JButton btwBrowse;

    JRadioButton radCurrentProject;
    JLabel lblCurrentProject;
    JComboBox cmbCurrentProject;
    JRadioButton radCustomLocation;

    ButtonGroup buttonGroup;

    JLabel lblModuleSrc;
    JComboBox cmbModuleSrc;
    boolean flag = true;
    private CodegenBean cogenbean;
    final JFileChooser fc = new JFileChooser();

    public OutPutPane(CodegenBean bean) {
        this.cogenbean = bean;
        OutPutPaneLayout customLayout = new OutPutPaneLayout();

        setLayout(customLayout);

        buttonGroup = new ButtonGroup();

        txtoutput = new JTextField("");
        txtoutput.setEnabled(false);
        add(txtoutput);

        btwBrowse = new JButton("Browse..");
        add(btwBrowse);
        btwBrowse.setEnabled(false);
        btwBrowse.addActionListener(this);


        radCurrentProject = new JRadioButton("Add sources to current project", true);
        buttonGroup.add(radCurrentProject);
        radCurrentProject.setActionCommand("radCurrentProject");
        add(radCurrentProject);
        radCurrentProject.addActionListener(this);

        lblCurrentProject = new JLabel("Select Module");
        lblCurrentProject.setEnabled(true);
        add(lblCurrentProject);

        radCustomLocation = new JRadioButton("Select custom output location");
        buttonGroup.add(radCustomLocation);
        radCustomLocation.setActionCommand("radCustomLocation");
        add(radCustomLocation);
        radCustomLocation.addActionListener(this);

        cmbCurrentProject = new JComboBox();
        cmbCurrentProject.setEnabled(true);
        add(cmbCurrentProject);
        cmbCurrentProject.addActionListener(this);

        lblModuleSrc = new JLabel("Select Source Directory");
        lblModuleSrc.setEnabled(true);
        add(lblModuleSrc);

        cmbModuleSrc = new JComboBox();
        cmbModuleSrc.setEnabled(true);
        add(cmbModuleSrc);


//        loadCmbCurrentProject();
//        loadcmbModuleSrcProject();


        setSize(getPreferredSize());
    }

    public void loadCmbCurrentProject() {
        Module modules[] = cogenbean.getModules();

        if (modules != null) {
            for (int count = 0; count < modules.length; count++) {
                cmbCurrentProject.addItem(modules[count].getName());
            }
        }

    }

    public void loadcmbModuleSrcProject() {
        String module = null;
        module = (String) cmbCurrentProject.getSelectedItem();
        cmbModuleSrc.removeAllItems();
        int count = 0;
        if (module != null) {
            String src[] = cogenbean.getModuleSrc(module);
            for ( count = 0; count < src.length; count++) {
                cmbModuleSrc.addItem(src[count]);
            }

            count = src.length;
        }

        if (flag)
        {
            flag = false;

            if (count == 0) {
                radCurrentProject.setEnabled(false);
                cmbCurrentProject.setEnabled(false);
                cmbModuleSrc.setEnabled(false);
                lblCurrentProject.setEnabled(false);
                lblModuleSrc.setEnabled(false);
                radCustomLocation.setSelected(true);
                txtoutput.setEnabled(true);
                btwBrowse.setEnabled(true);
            }
            else{
                radCurrentProject.setEnabled(true);
                cmbCurrentProject.setEnabled(true);
                cmbModuleSrc.setEnabled(true);
                lblCurrentProject.setEnabled(true);
                lblModuleSrc.setEnabled(true);
                radCurrentProject.setSelected(true);
                txtoutput.setEnabled(false);
                btwBrowse.setEnabled(false);
            }
        }
    }


    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btwBrowse) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                cogenbean.setOutput(file.getAbsolutePath());
                txtoutput.setText(file.getAbsolutePath());
            } else {
                System.out.println("no file");
            }
        } else if (obj == radCurrentProject) {
            lblCurrentProject.setEnabled(true);
            cmbCurrentProject.setEnabled(true);
            lblModuleSrc.setEnabled(true);
            cmbModuleSrc.setEnabled(true);
            txtoutput.setEnabled(false);
            btwBrowse.setEnabled(false);

        } else if (obj == radCustomLocation) {
            lblCurrentProject.setEnabled(false);
            cmbCurrentProject.setEnabled(false);
            lblModuleSrc.setEnabled(false);
            cmbModuleSrc.setEnabled(false);
            txtoutput.setEnabled(true);
            btwBrowse.setEnabled(true);
        }
        else if (obj == cmbCurrentProject) {
            loadcmbModuleSrcProject();
        }
    }

    class OutPutPaneLayout implements LayoutManager {

        public OutPutPaneLayout() {
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = new Dimension(0, 0);

            Insets insets = parent.getInsets();
            dim.width = 611 + insets.left + insets.right;
            dim.height = 600 + insets.top + insets.bottom;

            return dim;
        }

        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();

            Component c;
            c = parent.getComponent(2);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 8, 350, 24);
            }
            c = parent.getComponent(3);
            if (c.isVisible()) {
                c.setBounds(insets.left + 30, insets.top + 40, 150, 24);
            }
            c = parent.getComponent(5);
            if (c.isVisible()) {
                c.setBounds(insets.left + 200, insets.top + 40, 330, 24);
            }
            c = parent.getComponent(6);
            if (c.isVisible()) {
                c.setBounds(insets.left + 30, insets.top + 70, 150, 24);
            }
            c = parent.getComponent(7);
            if (c.isVisible()) {
                c.setBounds(insets.left + 200, insets.top + 70, 330, 24);
            }
            c = parent.getComponent(4);
            if (c.isVisible()) {
                c.setBounds(insets.left + 8, insets.top + 100, 350, 24);
            }
            c = parent.getComponent(0);
            if (c.isVisible()) {
                c.setBounds(insets.left + 30, insets.top + 130, 150, 24);
            }
            c = parent.getComponent(1);
            if (c.isVisible()) {
                c.setBounds(insets.left + 200, insets.top + 130, 150, 24);
            }
        }
    }

}

