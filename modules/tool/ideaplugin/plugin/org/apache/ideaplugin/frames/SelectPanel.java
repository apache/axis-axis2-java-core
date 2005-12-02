package org.apache.ideaplugin.frames;

import org.apache.ideaplugin.bean.ObjectKeeper;
import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.OprationObj;
import org.apache.ideaplugin.bean.ServiceObj;
import org.apache.ideaplugin.frames.table.ArchiveTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
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
 * Date: Sep 18, 2005
 * Time: 9:11:54 PM
 */
public class SelectPanel extends JPanel implements ObjectKeeper, ActionListener {

    protected JLabel lblClass;
    protected JLabel lblServiceNam;
    protected JTextField txtClassDir;
    protected JTextField txtServiceName;
    protected JButton butSelect;
    protected JButton butDone;
    protected JButton load;
    protected JScrollPane sp;
    protected JLabel tablelbl;

//    private JPanel next;
    private JPanel previous;
    protected File file;
    protected Insets insets;
    protected MainFrame parent;
    protected String fileName;
    protected int count = 1;
    protected HashMap operations;
    protected ArrayList servicelsit = new ArrayList();
    protected DescriptorFile disfile;
    protected String sgXMl;

    public SelectPanel(MainFrame parent , File file ) {
        this.parent = parent;
        this.file =file;

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        this.setLayout(null);

        insets = parent.getInsets();

        lblClass = new JLabel("Select Service Classes");
        add(lblClass);
        lblClass.setBounds(insets.left + 8, insets.top + 2, 150 , 24);

        txtClassDir = new JTextField("");
        add(txtClassDir);
        txtClassDir.setBounds(insets.left + 150 , insets.top + 2, 336, 24);

        butSelect = new JButton(" ... ");
        add(butSelect);
        butSelect.addActionListener(this);
        butSelect.setBounds(insets.left + 487, insets.top + 2, 10, 24);


        load = new JButton(" Load ");
        add(load);
        load.addActionListener(this);
        load.setBounds(insets.left + 502, insets.top + 2, 70, 24);

        butDone = new JButton("Done");
        butDone.addActionListener(this);
        add(butDone);
        butDone.setBounds(insets.left + 250, insets.top + 185, 70, 24);
        lblServiceNam = new JLabel("Service Name : ");
        add(lblServiceNam);
        lblServiceNam.setBounds(insets.left + 10, insets.top + 185, 100, 24);
        txtServiceName = new JTextField("");
        add(txtServiceName);
        txtServiceName.setBounds(insets.left + 115, insets.top + 185, 120, 24);
        butDone.setVisible(false);
        lblServiceNam.setVisible(false);
        txtServiceName.setVisible(false);
        setSize(getPreferredSize());
    }

    public void fillBean(ArchiveBean bean) {
        bean.setServiceXML(sgXMl);
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {
        //no one call this
    }

    public JPanel getNext() {
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
        if(obj == butSelect){
            parent.fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = parent.fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File newfile = parent.fc.getSelectedFile();
                String newFile =newfile.getPath();
                int index = newFile.indexOf(file.getAbsolutePath().trim());
                if(index >=0){
                    int lastindex= file.getAbsolutePath().trim().length();
                    newFile = newFile.substring(lastindex +1);
                    char ch = parent.fileSeparator.toCharArray()[0];
                    char newch= '.';
                    int cindex = newFile.indexOf(ch);
                    while(cindex >=0){
                        newFile =   newFile.replace(ch,newch);
                        cindex = newFile.indexOf(ch);
                    }
                    fileName = newFile;
                    int classIndex = fileName.indexOf(".class");
                    fileName = fileName.substring(0,classIndex);
                    txtClassDir.setText(fileName);
                }
            }
        } else if(obj == load){
            if(file == null){
                return;
            }
            try {
                try {
                    this.remove(sp);
                    this.remove(tablelbl);
                    butDone.setVisible(false);
                    lblServiceNam.setVisible(false);
                    txtServiceName.setVisible(false);
                } catch (Exception e1) {
//                    e1.printStackTrace();
                }

//                ClassLoader classLoader = new URLClassLoader(
//                        new URL[]{file.toURL()},SelectPanel.class.getClassLoader());

                ClassLoader classLoader = parent.bean.getClassLoader();
                Class serCla= Class.forName(fileName,true,classLoader);
                Method[] methods =  serCla.getDeclaredMethods();
                operations = new HashMap();
                if(methods.length >0){
                    for (int i = 0; i < methods.length; i++) {
                        Method method = methods[i];
                        OprationObj Operationobj = new OprationObj(method.getName(),
                                method.getReturnType().toString(),
                                new Integer(method.getParameterTypes().length),new Boolean(true));
                        operations.put(method.getName() ,Operationobj);
                    }
                }

                ArchiveTableModel myModel = new ArchiveTableModel(operations);
                JTable table = new JTable(myModel);
                tablelbl = new JLabel("Select Operation you want to publish ") ;
                add(tablelbl);
                tablelbl.setBounds(insets.left + 10, insets.top + 45, 400, 24);

                sp =new JScrollPane(table);
                add(sp);
                sp.setAutoscrolls(true);
                sp.setBounds(insets.left + 10, insets.top + 75, 550, 100);
                txtServiceName.setText("MyService" + count);
                butDone.setVisible(true);
                lblServiceNam.setVisible(true);
                txtServiceName.setVisible(true);
            }  catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            parent.reShow();

        } else if(obj == butDone){

            ArrayList ops = new ArrayList();
            Iterator opitr = operations.values().iterator();
            while (opitr.hasNext()) {
                OprationObj oprationObj = (OprationObj) opitr.next();
                if(oprationObj.getSelect().booleanValue()){
                    ops.add(oprationObj.getOpName());
                }
            }

            ServiceObj service= new ServiceObj(txtServiceName.getText(),fileName,ops);
            servicelsit.add(service);
            if(!parent.singleService){
                int valu = JOptionPane.showConfirmDialog(parent,"Do you want to add an another service to group","Service Archive",
                        JOptionPane.YES_NO_OPTION);
                if(valu == 0){
                    txtClassDir.setText("");
                    fileName = "";
                    try {
                        this.remove(sp);
                        this.remove(tablelbl);
                        butDone.setVisible(false);
                        lblServiceNam.setVisible(false);
                        txtServiceName.setVisible(false);
                    } catch (Exception e1) {
//                    e1.printStackTrace();
                    }
                    count ++;
                    parent.reShow();
                    this.repaint();
                } else {
                    parent.setEnable(false,true,false,true);
                    sgXMl = "<serviceGroup>\n";
                    for (int i = 0; i < servicelsit.size(); i++) {
                        ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                        sgXMl = sgXMl + serviceObj.toString();
                    }
                    sgXMl = sgXMl + "</serviceGroup>";
                    disfile = new DescriptorFile(parent,sgXMl);
                    disfile.setPrivious(this);
                }
            } else {
                parent.setEnable(false,true,false,true);
                sgXMl = "<serviceGroup>\n";
                for (int i = 0; i < servicelsit.size(); i++) {
                    ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                    sgXMl = sgXMl + serviceObj.toString();
                }
                sgXMl = sgXMl + "</serviceGroup>";
                disfile = new DescriptorFile(parent,sgXMl);
                disfile.setPrivious(this);
            }

//            int valu = JOptionPane.showConfirmDialog(parent,"Do you want to add an another service to group","Service Archive",
//                    JOptionPane.YES_NO_OPTION);
//            if(valu == 0){
//                txtClassDir.setText("");
//                fileName = "";
//                try {
//                    this.remove(sp);
//                    this.remove(tablelbl);
//                    butDone.setVisible(false);
//                    lblServiceNam.setVisible(false);
//                    txtServiceName.setVisible(false);
//                } catch (Exception e1) {
////                    e1.printStackTrace();
//                }
//                count ++;
//                parent.reShow();
//                this.repaint();
//            } else {
//                parent.setEnable(false,true,false,true);
//                sgXMl = "<serviceGroup>\n";
//                for (int i = 0; i < servicelsit.size(); i++) {
//                    ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
//                    sgXMl = sgXMl + serviceObj.toString();
//                }
//                sgXMl = sgXMl + "</serviceGroup>";
//                disfile = new DescriptorFile(parent,sgXMl);
//                disfile.setPrivious(this);
//            }
        }
    }

    public String getTopLable() {
        return "Service class and operation selection";
    }

    public String getLable() {
        return "First select service class and load its method operations";
    }
}

