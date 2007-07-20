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
 * Time: 12:11:36 PM
 */
public class ParameterDialog extends JFrame implements ActionListener {

    Insets insets;

    protected JLabel paraName;
    protected JLabel paravale;
    protected JTextField txtpraName;
    protected JTextArea txtparaValue;
    protected JScrollPane sp;
    protected JButton addbut;
    protected String val;
    JTextArea textarea;
    int position;

    public ParameterDialog() {
        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(null);
        getContentPane().setBounds(150, 150, 300, 180);
        insets = getInsets();

        paraName = new JLabel("Parameter Name: ");
        getContentPane().add(paraName);

        paravale = new JLabel("Parameter Value: ");
        getContentPane().add(paravale);


        txtpraName = new JTextField("-para name -");
        getContentPane().add(txtpraName);


        txtparaValue = new JTextArea("-para vale -");
        sp = new JScrollPane(txtparaValue);
        getContentPane().add(sp);
        sp.setAutoscrolls(true);

        addbut = new JButton("Add");
        getContentPane().add(addbut);
        addbut.addActionListener(this);


        paraName.setBounds(insets.left + 5, insets.top + 6, 100, 24);
        paravale.setBounds(insets.left + 5, insets.top + 34, 100, 24);
        sp.setBounds(insets.left + 110, insets.top + 34, 160, 60);
        txtpraName.setBounds(insets.left + 110, insets.top + 6, 160, 24);
        addbut.setBounds(insets.left + 100, insets.top + 100, 60, 24);

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
            String str = "<parameter locked=\"false\" name=\"" + txtpraName.getText().trim() + "\">\n"
                    + txtparaValue.getText() + "\n" +
                    "</parameter>\n";
            val = str;
            textarea.insert(str, position + 1);
            this.setVisible(false);
        }
    }
}
