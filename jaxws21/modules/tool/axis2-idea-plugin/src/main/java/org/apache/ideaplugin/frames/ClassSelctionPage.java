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
 * Date: Sep 22, 2005
 * Time: 5:18:26 PM
 */
public class ClassSelctionPage extends JPanel implements ObjectKeeper, ActionListener {

    protected JLabel lblClass;
    protected JTextField txtClassDir;
    protected JButton butSelect;

    private JPanel previous;

    File file;
    Insets insets;
    ServiceArciveFrame parent;
//    protected SelectPanel selectPanel;


    public ClassSelctionPage(ServiceArciveFrame parent) {
        this.parent = parent;

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        this.setLayout(null);
        insets = parent.getInsets();

        lblClass = new JLabel("Select Classes");
        add(lblClass);
        lblClass.setBounds(insets.left + 8, insets.top + 24, 120, 24);

        txtClassDir = new JTextField("");
        add(txtClassDir);
        txtClassDir.setBounds(insets.left + 136, insets.top + 24, 336, 24);

        butSelect = new JButton("Browse...");
        add(butSelect);
        butSelect.addActionListener(this);
        butSelect.setBounds(insets.left + 480, insets.top + 24, 90, 24);
        setSize(getPreferredSize());
//        this.parent.setEnable(true, false, false, true);
//        this.parent.reShow();


    }

    public void fillBean(ArchiveBean bean) {
        bean.setClassLoc(file);
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {
    }

    public JPanel getNext() {
        ResourceChooser res = new ResourceChooser(parent);
        parent.setEnable(true,true,false,true);
        res.setPrivious(this);
        return res;
//        return selectPanel;
    }

    //to keep a refernce to previous panel
    public void setPrivious(JPanel privious) {
        this.previous = privious;
    }

    public JPanel getPrivious() {
        return this.previous;
    }


    public String getTopLable() {
        return "Class location selection";
    }

    public String getLable() {
        return " Select the location of service classes directory";
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == butSelect) {
            parent.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = parent.fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = parent.fc.getSelectedFile();
                parent.fc.setCurrentDirectory(file);
                txtClassDir.setText(file.getAbsolutePath());
                parent.setEnable(true, true, false, true);
//                selectPanel = new SelectPanel(parent,file);
//                selectPanel.setPrivious(this);
            } else {
                txtClassDir.setText("");
                parent.setEnable(true, false, false, true);
            }
        }
    }
}

