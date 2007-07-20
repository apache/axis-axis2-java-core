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
package org.apache.ideaplugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 23, 2005
 * Time: 4:11:19 PM
 */
public class ModuleDialog extends JFrame implements ActionListener {

    Insets insets;

    protected JLabel moduleName;
    protected JTextField txtModule;
    protected JButton addbut;

    protected String val;
    JTextArea textarea;
    int position;

    public ModuleDialog() {
        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(null);
        getContentPane().setBounds(150, 150, 300, 100);
        insets = getInsets();

        moduleName = new JLabel("Module Name: ");
        getContentPane().add(moduleName);

        txtModule = new JTextField("moduleName");
        getContentPane().add(txtModule);

        addbut = new JButton("Add");
        getContentPane().add(addbut);
        addbut.addActionListener(this);


        moduleName.setBounds(insets.left + 5, insets.top + 6, 100, 24);
        txtModule.setBounds(insets.left + 110, insets.top + 6, 160, 24);
        addbut.setBounds(insets.left + 100, insets.top + 45, 60, 24);

        setSize(getPreferredSize());
        setResizable(false);
    }

    public void showDialog(String in, JTextArea textarea, int position) {
        this.textarea = textarea;
        this.position = position;
        this.show();
        this.val = in;
        this.setVisible(true);
    }

    public String hideForm() {
        return this.val;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == addbut) {
            String str = "<module ref=\"" + txtModule.getText().trim() + "\" \\>";
            val = str;
            textarea.insert(str, position + 1);
            this.setVisible(false);
        }
    }
}
