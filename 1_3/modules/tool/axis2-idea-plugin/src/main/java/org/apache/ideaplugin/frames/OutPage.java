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
package org.apache.ideaplugin.frames;

import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.ObjectKeeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 23, 2005
 * Time: 5:12:13 PM
 */
public class OutPage extends JPanel implements ObjectKeeper, ActionListener {
    JLabel lblout;
    JTextField txtoutput;
    JButton butselect;
    JLabel lblname;
    JTextField txtjarName;

    public final JFileChooser fc = new JFileChooser();
    ServiceArciveFrame parent;
    private JPanel previous;


    public OutPage(ServiceArciveFrame parent) {
        this.parent = parent;
        OutPageLayout customLayout = new OutPageLayout();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        lblout = new JLabel("Select Output location");
        add(lblout);

        txtoutput = new JTextField(".");
        add(txtoutput);

        butselect = new JButton("Browse...");
        butselect.addActionListener(this);
        add(butselect);

        lblname = new JLabel("Archive name");
        add(lblname);

        txtjarName = new JTextField("MyArchive");
        add(txtjarName);

        setSize(getPreferredSize());
    }

    public void fillBean(ArchiveBean bean) {
        bean.setOutPath(txtoutput.getText().trim());
        bean.setArchiveName(txtjarName.getText().trim());
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {

    }

    public JPanel getNext() {
        return null;
    }

    //to keep a refernce to previous panel
    public void setPrivious(JPanel privious) {
        this.previous = privious;
    }

    public JPanel getPrivious() {
        return this.previous;
    }

    public String getTopLable() {
        return "Output location selection";
    }

    public String getLable() {
        return "Select output location and archive name";
    }


    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == butselect) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File newfile = fc.getSelectedFile();
                txtoutput.setText(newfile.getAbsolutePath());
            } else {
                txtoutput.setText("");
            }
            parent.setEnable(false, false, true, true);
        }
    }
}

class OutPageLayout implements LayoutManager {

    public OutPageLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 594 + insets.left + insets.right;
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
            c.setBounds(insets.left + 8, insets.top + 16, 152, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 168, insets.top + 16, 312, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 488, insets.top + 16, 90, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 56, 152, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 168, insets.top + 56, 312, 24);
        }
    }
}

