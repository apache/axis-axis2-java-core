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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 23, 2005
 * Time: 11:26:02 PM
 */
public class XMLSelectionPage extends JPanel implements ObjectKeeper, ActionListener {
    JLabel selectxml;
    JTextField txtService;
    JButton butSelect;

    JLabel selctclass;
    JTextField txtclass;
    File file;
    JButton bustSelectclss;
    String value;
    private JPanel previous;

    protected ServiceArciveFrame parent;

    public XMLSelectionPage(ServiceArciveFrame parent) {
        Insets insets = parent.getInsets();
        this.parent = parent;
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(null);

        selectxml = new JLabel("Select services.xml");
        add(selectxml);

        txtService = new JTextField("");
        add(txtService);

        butSelect = new JButton("Browse...");
        butSelect.addActionListener(this);
        add(butSelect);

        selctclass = new JLabel("Select class location");
        add(selctclass);

        txtclass = new JTextField("");
        add(txtclass);

        bustSelectclss = new JButton("Browse...");
        bustSelectclss.addActionListener(this);
        add(bustSelectclss);

        selectxml.setBounds(insets.left + 16, insets.top + 16, 168, 24);
        txtService.setBounds(insets.left + 192, insets.top + 16, 288, 24);
        butSelect.setBounds(insets.left + 488, insets.top + 16, 90, 24);

        selctclass.setBounds(insets.left + 16, insets.top + 45, 168, 24);
        txtclass.setBounds(insets.left + 192, insets.top + 45, 288, 24);
        bustSelectclss.setBounds(insets.left + 488, insets.top + 45, 90, 24);

        setSize(getPreferredSize());
    }

    public void fillBean(ArchiveBean bean) {
        bean.addClassLocation(file);
        bean.setClassLoc(file);
        bean.setServiceXML(value);
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {

    }

    public String getTopLable() {
        return "Class location & Service descriptor selection";
    }

    public String getLable() {
        return " Select the location of service classes directory and services.xml";
    }

    public JPanel getNext() {

        ResourceChooser resourceChooser = new ResourceChooser(parent);
        parent.setEnable(true,true,false,true);
        resourceChooser.setPrivious(this);
        return resourceChooser;


        /*
        DescriptorFile disfile = new DescriptorFile(parent, value);
        disfile.setPrivious(this);
        return disfile;
        */
    }

    //to keep a refernce to previous panel
    public void setPrivious(JPanel privious) {
        this.previous = privious;
    }

    public JPanel getPrivious() {
        return this.previous;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == bustSelectclss) {
            parent.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = parent.fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = parent.fc.getSelectedFile();
                txtclass.setText(file.getAbsolutePath());
                if ((new File(txtService.getText())).isFile())
                parent.setEnable(false, true, false, true);
            } else {
                txtclass.setText("");
                parent.setEnable(true, false, false, true);
            }

        } else if (obj == butSelect) {
            parent.fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = parent.fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = parent.fc.getSelectedFile();

                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                try {
                    FileInputStream in = new FileInputStream(file);

                    out = new ByteArrayOutputStream();
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                    in.close();
                    value = new String(out.toByteArray());
                } catch (IOException e1) {
                }
                txtService.setText(file.getAbsolutePath());
                if ((new File(txtclass.getText())).isDirectory())
                parent.setEnable(false, true, false, true);
            } else {
                txtService.setText("");
                parent.setEnable(true, false, false, true);
            }

        }
    }
}

