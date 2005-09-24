package org.apache.idaeplugin.frames;

import org.apache.idaeplugin.bean.ObjectKeeper;
import org.apache.idaeplugin.bean.ArchiveBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
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
    JButton butDone;
    private DefaultListModel listModellibs;
    private DefaultListModel listModellwsdls;
    private JPanel previous;

    public final JFileChooser fc = new JFileChooser();
    MainFrame parent;

    public ResourceChooser(MainFrame parent) {
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

        butselect = new JButton("...");
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

        butSelectwsdl = new JButton("...");
        butSelectwsdl.addActionListener(this);
        add(butSelectwsdl);

        butaddwsdl = new JButton("Add");
        butaddwsdl.addActionListener(this);
        add(butaddwsdl);

        listModellwsdls = new DefaultListModel();
        listwsdl = new JList(listModellwsdls);
        sp_listwsdl = new JScrollPane(listwsdl);
        add(sp_listwsdl);

        butDone = new JButton("Done");
        butDone.addActionListener(this);
        add(butDone);
        setSize(getPreferredSize());

    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if(obj == butLoad ) {
            listModellibs.addElement(txtLibs.getText());
        } else if (obj == butselect) {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File newfile = fc.getSelectedFile();
                txtLibs.setText(newfile.getAbsolutePath());
            }  else {
                txtLibs.setText("");
            }
        } else if(obj == butSelectwsdl){
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File newfile = fc.getSelectedFile();
                txtwsdl.setText(newfile.getAbsolutePath());
            }  else {
                txtwsdl.setText("");
            }
        } else if(obj ==butaddwsdl ){
            listModellwsdls.addElement(txtwsdl.getText());
        } else if (obj == butDone){
            parent.setEnable(false,true,false,true);
        }
    }

    public void fillBean(ArchiveBean bean) {

        Enumeration enum =listModellibs.elements();
        ArrayList libs = new ArrayList();
        URL urllist [] = new URL[listModellibs.size() +1];
        int count = 0;
        while (enum.hasMoreElements()) {
            String s = (String) enum.nextElement();
            File file = new File(s);
            if(file.exists()){
                try {
                    urllist[count] = file.toURL();
                } catch (MalformedURLException e) {
                    System.out.println("Error");
                }
            }
            libs.add(s);
            count ++;
        }
        try {
            urllist[count] = bean.getClassLocation().toURL();
        } catch (MalformedURLException e) {
            
        }
        ClassLoader cls = new URLClassLoader(urllist,ResourceChooser.class.getClassLoader());
        bean.setClassLoader(cls);
//        Enumeration enum =listModellibs.elements();
//        ArrayList libs = new ArrayList();
//        while (enum.hasMoreElements()) {
//            String s = (String) enum.nextElement();
//            libs.add(s);
//        }
        bean.setLibs(libs);
        enum =listModellwsdls.elements();
        ArrayList wsdls = new ArrayList();
        while (enum.hasMoreElements()) {
            String s = (String) enum.nextElement();
            wsdls.add(s);
        }
        bean.setWsdls(wsdls);
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
        if(parent.generateServiceXML){
            SelectPanel sp = new SelectPanel(parent,parent.bean.getClassLocation());
            sp.setPrivious(this);
            return sp;
        } else {
            DescriptorFile dis = new DescriptorFile(parent,parent.bean.getServiceXML());
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
        return  this.previous;
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
        if (c.isVisible()) {c.setBounds(insets.left+16,insets.top+16,128,24);}
        c = parent.getComponent(1);
        if (c.isVisible()) {c.setBounds(insets.left+152,insets.top+16,312,24);}
        c = parent.getComponent(2);
        if (c.isVisible()) {c.setBounds(insets.left+496,insets.top+16,72,24);}
        c = parent.getComponent(3);
        if (c.isVisible()) {c.setBounds(insets.left+472,insets.top+16,16,24);}
        c = parent.getComponent(4);
        if (c.isVisible()) {c.setBounds(insets.left+152,insets.top+48,312,72);}
        c = parent.getComponent(5);
        if (c.isVisible()) {c.setBounds(insets.left+16,insets.top+128,128,24);}
        c = parent.getComponent(6);
        if (c.isVisible()) {c.setBounds(insets.left+152,insets.top+128,312,24);}
        c = parent.getComponent(7);
        if (c.isVisible()) {c.setBounds(insets.left+472,insets.top+128,16,24);}
        c = parent.getComponent(8);
        if (c.isVisible()) {c.setBounds(insets.left+496,insets.top+128,72,24);}
        c = parent.getComponent(9);
        if (c.isVisible()) {c.setBounds(insets.left+152,insets.top+160,312,64);}
        c = parent.getComponent(10);
        if (c.isVisible()) {c.setBounds(insets.left+248,insets.top+225,72,24);}
    }
}

