package org.apache.ideaplugin.frames;

import org.apache.ideaplugin.ModuleDialog;
import org.apache.ideaplugin.ParameterDialog;
import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.ObjectKeeper;

import javax.swing.*;
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

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 22, 2005
 * Time: 11:42:16 PM
 */
public class DescriptorFile extends JPanel implements ObjectKeeper, ActionListener {

    protected JTextArea desArea;
    protected JButton butaddpara;
    protected JButton addModuleRef;
    protected ServiceArciveFrame parent;
    protected Insets insets;
    protected JScrollPane sp;

    protected ParameterDialog pradialog;
    protected ModuleDialog moduledialog;
    private JPanel previous;

    public DescriptorFile(ServiceArciveFrame parent, String XML) {
        this.parent = parent;

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        this.setLayout(null);
        insets = parent.getInsets();

        desArea = new JTextArea(XML);
        sp = new JScrollPane(desArea);
        sp.setAutoscrolls(true);
        add(sp);
        sp.setBounds(insets.left + 8, insets.top + 26, 560, 190);

        butaddpara = new JButton("+Parameter ");
        add(butaddpara);
        butaddpara.addActionListener(this);
        butaddpara.setBounds(insets.left + 10, insets.top + 2, 120, 20);

        addModuleRef = new JButton("+ModuleRef ");
        add(addModuleRef);
        addModuleRef.addActionListener(this);
        addModuleRef.setBounds(insets.left + 135, insets.top + 2, 120, 20);

        pradialog = new ParameterDialog();
        moduledialog = new ModuleDialog();
        setSize(getPreferredSize());


    }


    public void fillBean(ArchiveBean bean) {
        bean.setServiceXML(desArea.getText());
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {
    }

    public JPanel getNext() {
        OutPage out = new OutPage(parent);
        parent.setEnable(true, true, false, true);
        out.setPrivious(this);
        return out;
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
        if (obj == butaddpara) {
            String str = "";
            int cusrpos = desArea.getCaretPosition();
            pradialog.showDialog(str, desArea, cusrpos);
            pradialog.hideForm();
        } else if (obj == addModuleRef) {
            String str = "";
            int cusrpos = desArea.getCaretPosition();
            moduledialog.showDialog(str, desArea, cusrpos);
            moduledialog.hideForm();
        }
    }

    public String getTopLable() {
        return "Edit service descriptor";
    }

    public String getLable() {
        return "Edit description file add parameters and module references";
    }

}
