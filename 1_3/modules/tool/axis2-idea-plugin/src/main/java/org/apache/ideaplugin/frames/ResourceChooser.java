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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 23, 2005
 * Time: 4:30:18 PM
 */
public class ResourceChooser extends JPanel implements ObjectKeeper, ActionListener {

    JLabel libLbl;
    JTextField txtLibs;
    JButton butLoad;
    JButton butselect;
    JList lisLibs;
    JScrollPane sp_lisLibs;
    JLabel lblwsdl;
    JTextField txtwsdl;
    JButton butSelectwsdl;
    JButton butaddwsdl;
    JList listwsdl;
    JScrollPane sp_listwsdl;
    private DefaultListModel listModellibs;
    private DefaultListModel listModellwsdls;
    private JPanel previous;

    public final JFileChooser fc = new JFileChooser();
    ServiceArciveFrame parent;

    public ResourceChooser(ServiceArciveFrame parent) {
        this.parent = parent;
        ResourceChooserLayout customLayout = new ResourceChooserLayout();

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        libLbl = new JLabel("Select Lib files : ");
        add(libLbl);

        txtLibs = new JTextField("");
        add(txtLibs);

        butLoad = new JButton("Add");
        butLoad.addActionListener(this);
        add(butLoad);

        butselect = new JButton("Browse...");
        butselect.addActionListener(this);
        add(butselect);

        listModellibs = new DefaultListModel();
        lisLibs = new JList(listModellibs);
        sp_lisLibs = new JScrollPane(lisLibs);
        add(sp_lisLibs);

        lblwsdl = new JLabel("Select WSDLs : ");
        add(lblwsdl);

        txtwsdl = new JTextField("");
        add(txtwsdl);

        butSelectwsdl = new JButton("Browse...");
        butSelectwsdl.addActionListener(this);
        add(butSelectwsdl);

        butaddwsdl = new JButton("Add");
        butaddwsdl.addActionListener(this);
        add(butaddwsdl);

        listModellwsdls = new DefaultListModel();
        listwsdl = new JList(listModellwsdls);
        sp_listwsdl = new JScrollPane(listwsdl);
        add(sp_listwsdl);

   setSize(getPreferredSize());

    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == butLoad) {
            File lib = new File(txtLibs.getText());
            if (lib.isFile() ) {
                if( !listModellibs.contains(txtLibs.getText()))
            listModellibs.addElement(txtLibs.getText());
            }
            else{
                JOptionPane.showMessageDialog(parent, "The file selected is not a valid jar file",
                    "Axis2 ServiceArchieve creation", JOptionPane.ERROR_MESSAGE);
            }
        } else if (obj == butselect) {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File newfile = fc.getSelectedFile();
                txtLibs.setText(newfile.getAbsolutePath());
            } else {
                txtLibs.setText("");
            }
        } else if (obj == butSelectwsdl) {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File newfile = fc.getSelectedFile();
                txtwsdl.setText(newfile.getAbsolutePath());
            } else {
                txtwsdl.setText("");
            }
        } else if (obj == butaddwsdl) {
            File wsdl = new File(txtwsdl.getText());
            if (wsdl.isFile()){
                if(!listModellwsdls.contains(txtwsdl.getText()))
            listModellwsdls.addElement(txtwsdl.getText());
            }
            else{
                JOptionPane.showMessageDialog(parent, "The file selected is not a valid jar file",
                    "Axis2 ServiceArchieve creation", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void fillBean(ArchiveBean bean) {

        Enumeration enumerator = listModellibs.elements();
        ArrayList libs = new ArrayList();
        URL urllist[] = new URL[listModellibs.size() + 1];
        int count = 0;
        while (enumerator.hasMoreElements()) {
            String s = (String) enumerator.nextElement();
            File file = new File(s);
            if (file.exists()) {
                try {
                    urllist[count] = file.toURL();
                } catch (MalformedURLException e) {
                    System.out.println("Error");
                }
            }
            libs.add(s);
            count++;
        }
        try {
            urllist[count] = bean.getClassLoc().toURL();
        } catch (MalformedURLException e) {

        }
        ClassLoader cls = new URLClassLoader(urllist, ResourceChooser.class.getClassLoader());
        bean.setClassLoader(cls);
//        Enumeration enumerator =listModellibs.elements();
//        ArrayList libs = new ArrayList();
//        while (enumerator.hasMoreElements()) {
//            String s = (String) enumerator.nextElement();
//            libs.add(s);
//        }
        bean.setTempLibs(libs);
        enumerator = listModellwsdls.elements();
        ArrayList wsdls = new ArrayList();
        while (enumerator.hasMoreElements()) {
            String s = (String) enumerator.nextElement();
            wsdls.add(s);
        }
        bean.setTempWsdls(wsdls);
    }


    public String getTopLable() {
        return "Resource Selection";
    }

    public String getLable() {
        return "Select service specific lib and service WSDLs";
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {

    }

    public JPanel getNext() {




        if (parent.generateServiceXML) {
            parent.setEnable(true, false, false, true);
            SelectPanel sp = new SelectPanel(parent, parent.bean.getClassLoc());
            
            
            sp.setPrivious(this);
            return sp;
        } else {
            DescriptorFile dis = new DescriptorFile(parent, parent.bean.getServiceXML());
            parent.setEnable(true, true, false, true);
            dis.setPrivious(this);
            return dis;
        }


//        OutPage op = new OutPage(parent);
//        op.setPrivious(this);
//        return op;
    }

    //to keep a refernce to previous panel
    public void setPrivious(JPanel privious) {
        this.previous = privious;
    }

    public JPanel getPrivious() {
        return this.previous;
    }
}

class ResourceChooserLayout implements LayoutManager {

    public ResourceChooserLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 587 + insets.left + insets.right;
        dim.height = 278 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        return dim;
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 16, insets.top + 16, 100, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 120, insets.top + 16, 300, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 530, insets.top + 16, 60, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 430, insets.top + 16, 90, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 120, insets.top + 48, 300, 72);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 16, insets.top + 128, 100, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 120, insets.top + 128, 300, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 430, insets.top + 128, 90, 24);
        }
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 530, insets.top + 128, 60, 24);
        }
        c = parent.getComponent(9);
        if (c.isVisible()) {
            c.setBounds(insets.left + 120, insets.top + 160, 300, 72);
        }
    }
}

