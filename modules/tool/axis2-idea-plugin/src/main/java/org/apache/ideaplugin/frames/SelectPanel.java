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
import org.apache.ideaplugin.bean.OperationObj;
import org.apache.ideaplugin.bean.ServiceObj;
import org.apache.ideaplugin.frames.table.ArchiveTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 18, 2005
 * Time: 9:11:54 PM
 */
public class SelectPanel extends JPanel implements ObjectKeeper, ActionListener {

    protected JLabel lblClass;
    protected JLabel lblServiceNam;
    protected JTextField txtClassDir;
    protected JTextField txtServiceName;
    protected JButton butSelect;
    protected JButton load;
    protected JScrollPane sp;
    protected JLabel tablelbl;

    private JPanel previous;
    protected File file;
    protected Insets insets;
    protected ServiceArciveFrame parent;
    protected String fileName;
    protected int count = 1;
    protected HashMap operations;
    protected DescriptorFile disfile;
    protected ClassSelctionPage classPage;
    protected String sgXMl;
    ArrayList servicelsit;

    public SelectPanel(ServiceArciveFrame parent, File file) {
        this.parent = parent;
        this.file = file;

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        this.setLayout(null);

        insets = parent.getInsets();

        lblClass = new JLabel("Select Service Classes");
        add(lblClass);
        lblClass.setBounds(insets.left + 8, insets.top + 2, 130, 24);

        txtClassDir = new JTextField("");
        add(txtClassDir);
        txtClassDir.setBounds(insets.left + 140, insets.top + 2, 280, 24);

        butSelect = new JButton("Browse...");
        add(butSelect);
        butSelect.addActionListener(this);
        butSelect.setBounds(insets.left + 420, insets.top + 2, 90, 24);


        load = new JButton(" Load ");
        add(load);
        load.addActionListener(this);
        load.setBounds(insets.left + 512, insets.top + 2, 70, 24);


        lblServiceNam = new JLabel("Service Name : ");
        add(lblServiceNam);
        lblServiceNam.setBounds(insets.left + 10, insets.top + 185, 100, 24);
        txtServiceName = new JTextField("");
        add(txtServiceName);
        txtServiceName.setBounds(insets.left + 115, insets.top + 185, 120, 24);
        lblServiceNam.setVisible(false);
        txtServiceName.setVisible(false);
        setSize(getPreferredSize());
        parent.fc.setFileFilter(new ClassFileFilter());
    }

    public void fillBean(ArchiveBean bean) {
        bean.addClassLocation(bean.getClassLoc());
        bean.setServiceXML(sgXMl);
        bean.addLibs(bean.getTempLibs());
        bean.addWsdls(bean.getTempWsdls());
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {
        //no one call this
    }

    public JPanel getNext() {
        parent.setEnable(true, true, false, true);
        if (classPage != null) {

         classPage.setPrivious(this);
            return classPage;
        }
        return disfile;
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
        if (obj == butSelect) {
            parent.fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = parent.fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File newfile = parent.fc.getSelectedFile();
                String newFile = newfile.getPath();
                int index = newFile.indexOf(file.getAbsolutePath().trim());
                if (index >= 0) {
                    int lastindex = file.getAbsolutePath().trim().length();
                    newFile = newFile.substring(lastindex + 1);
                    char ch = parent.fileSeparator.toCharArray()[0];
                    char newch = '.';
                    int cindex = newFile.indexOf(ch);
                    while (cindex >= 0) {
                        newFile = newFile.replace(ch, newch);
                        cindex = newFile.indexOf(ch);
                    }
                    fileName = newFile;
                    int classIndex = fileName.lastIndexOf(".");
                    fileName = fileName.substring(0, classIndex);
                    txtClassDir.setText(fileName);


                }
            }
        } else if (obj == load) {
            if (file == null || fileName ==null) {
                return;
            }
            try {
                try {
                    this.remove(sp);
                    this.remove(tablelbl);
                    lblServiceNam.setVisible(false);
                    txtServiceName.setVisible(false);
                } catch (Exception e1) {
//                    e1.printStackTrace();
                }

//                ClassLoader classLoader = new URLClassLoader(
//                        new URL[]{file.toURL()},SelectPanel.class.getClassLoader());

                ClassLoader classLoader = parent.bean.getClassLoader();
                Class serCla = Class.forName(fileName, true, classLoader);
                Method[] methods = serCla.getDeclaredMethods();
                operations = new HashMap();
                if (methods.length > 0) {
                    for (int i = 0; i < methods.length; i++) {
                        Method method = methods[i];
                        OperationObj operationobj = new OperationObj(method.getName(),
                                method.getReturnType().toString(),
                                new Integer(method.getParameterTypes().length), new Boolean(false));
                        operations.put(method.getName(), operationobj);
                    }
                }

                ArchiveTableModel myModel = new ArchiveTableModel(operations);
                JTable table = new JTable(myModel);
                tablelbl = new JLabel("Mark operation you do not want to publish ");
                add(tablelbl);
                tablelbl.setBounds(insets.left + 10, insets.top + 45, 400, 24);

                sp = new JScrollPane(table);
                add(sp);
                sp.setAutoscrolls(true);
                sp.setBounds(insets.left + 10, insets.top + 75, 550, 100);
                txtServiceName.setText("MyService" + count);
                lblServiceNam.setVisible(true);
                txtServiceName.setVisible(true);
                parent.setEnable(true,true,false,true);
            } catch (ClassNotFoundException e1) {
                JOptionPane.showMessageDialog(parent, "The specified file is not a valid java class",
                            "Error!", JOptionPane.ERROR_MESSAGE);
            }
             catch (NoClassDefFoundError e1) {
                JOptionPane.showMessageDialog(parent, "The specified file is not a valid java class",
                            "Error!", JOptionPane.ERROR_MESSAGE);
            }
            parent.repaint();

        }
    }

    public String getTopLable() {
        return "Service class and operation selection";
    }

    public String getLable() {
        return "First select service class and load its method operations";
    }

    public void process(){

        ArrayList ops = new ArrayList();
            Iterator opitr = operations.values().iterator();
            while (opitr.hasNext()) {
                OperationObj operationObj = (OperationObj) opitr.next();
                if (operationObj.getSelect().booleanValue()) {
                    ops.add(operationObj.getOpName());
                }
            }

            ServiceObj service = new ServiceObj(txtServiceName.getText(), fileName, ops);

            parent.bean.addToServicelsit(service);
            if (!parent.singleService) {
                int valu = JOptionPane.showConfirmDialog(parent, "Do you want to add an another service to group", "Service Archive",
                        JOptionPane.YES_NO_OPTION);
                if (valu == 0) {
                    txtClassDir.setText("");
                    fileName = "";
                    try {
                        this.remove(sp);
                        this.remove(tablelbl);
                        lblServiceNam.setVisible(false);
                        txtServiceName.setVisible(false);
                    } catch (Exception e1) {
//                    e1.printStackTrace();
                    }
                    classPage = new ClassSelctionPage(parent);
                    count++;
                    parent.reShow();
                    this.repaint();
                } else {
                    servicelsit = parent.bean.getServicelsit();
                    parent.setEnable(false, true, false, true);
                    sgXMl = "<serviceGroup>\n";
                    for (int i = 0; i < servicelsit.size(); i++) {
                        ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                        sgXMl = sgXMl + serviceObj.toString();
                    }
                    sgXMl = sgXMl + "</serviceGroup>";
                    disfile = new DescriptorFile(parent, sgXMl);
                    disfile.setPrivious(this);
                }
            } else {
                servicelsit = parent.bean.getServicelsit();
                parent.setEnable(false, true, false, true);
                sgXMl = "<serviceGroup>\n";
                for (int i = 0; i < servicelsit.size(); i++) {
                    ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                    sgXMl = sgXMl + serviceObj.toString();
                }
                sgXMl = sgXMl + "</serviceGroup>";
                disfile = new DescriptorFile(parent, sgXMl);
                disfile.setPrivious(this);

            }

    }


class ClassFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            return extension.equals("class");
        }

        return false;

    }

    public String getDescription() {
        return ".class";
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }



}
}

