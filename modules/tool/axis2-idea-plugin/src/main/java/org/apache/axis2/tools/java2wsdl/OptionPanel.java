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
package org.apache.axis2.tools.java2wsdl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener ;
import java.awt.event.MouseEvent ;
import org.apache.axis2.tools.bean.NamespaceFinder ;
import org.apache.axis2.tools.bean.WsdlgenBean;


public class OptionPanel extends JPanel implements ActionListener,MouseListener {

    private   JLabel lblNsp;
    private   JLabel lblNspPrefix;
    private   JLabel lblSchemaTargetNsp;
    private   JLabel lblSchemaTargetNspPrefix;
    private   JLabel lblService;

    JTextField txtNsp;
    JTextField txtNspPrefix;
    JTextField txtSchemaTargetNsp;
    JTextField txtSchemaTargetNspPrefix;
    JTextField txtService;

    Java2WSDLFrame java2WSDLFrame;
    WsdlgenBean wsdlgenBean;

    public OptionPanel(Java2WSDLFrame java2WSDLFrame,WsdlgenBean wsdlgenBean){

        this.java2WSDLFrame =java2WSDLFrame ;
        this.wsdlgenBean=wsdlgenBean;

        OptionLayout customLayout=new OptionLayout();
        setLayout(customLayout);

        setFont(new Font("Helvetica", Font.PLAIN, 12));

        //add lable and textfield

        lblNsp =new JLabel("Target Namespace");
        add(lblNsp);

        txtNsp=new JTextField();
        add(txtNsp);

        lblNspPrefix =new JLabel("Target Namespace Prefix");
        add(lblNspPrefix );

        txtNspPrefix =new JTextField();
        add(txtNspPrefix);

        lblSchemaTargetNsp=new JLabel("Schema Target Namespace");
        add(lblSchemaTargetNsp);

        txtSchemaTargetNsp =new JTextField();
        add(txtSchemaTargetNsp);

        lblSchemaTargetNspPrefix =new JLabel("Schema Target Namespace Prefix");
        add(lblSchemaTargetNspPrefix);

        txtSchemaTargetNspPrefix =new JTextField();
        add(txtSchemaTargetNspPrefix);

        lblService =new JLabel("Service Name");
        add(lblService );

        txtService =new JTextField();
        add(txtService );
        txtService .addActionListener(this);
        txtService.addMouseListener(this);

        setSize(getPreferredSize());

    }

    public String getTargetNamespace() {
        return txtNsp.getText() ;
    }

    public String getTargetNamespacePrefix() {
        return txtNspPrefix .getText() ;
    }


    public String getSchemaTargetNamespace() {
        return txtSchemaTargetNsp.getText() ;
    }

    public String getSchemaTargetNamespacePrefix () {
        return txtSchemaTargetNspPrefix .getText() ;
    }
    public String getServiceName(){
        return txtService .getText() ;
    }


    public void actionPerformed(ActionEvent e) {
        Object obj=e.getSource();
        if(obj ==txtService ){
            if (txtService .getText() != null && !txtService.getText().trim().equals("")) {
                BottomPanel.setEnable(true,true, true, true);
                wsdlgenBean.setServiceName(txtService.getText().trim());
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtService ){
            if (txtService .getText() != null && !txtService.getText().trim().equals("")) {
                BottomPanel.setEnable(true,true, true, true);
                wsdlgenBean.setServiceName(txtService.getText().trim());
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtService ){
            if (txtService .getText() != null && !txtService.getText().trim().equals("")) {
                BottomPanel.setEnable(true,true, true, true);
                wsdlgenBean.setServiceName(txtService.getText().trim());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtService ){
            if (txtService .getText() != null && !txtService.getText().trim().equals("")) {
                BottomPanel.setEnable(true,true, true, true);
                wsdlgenBean.setServiceName(txtService.getText().trim());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtService ){
            if (txtService .getText() != null && !txtService.getText().trim().equals("")) {
                BottomPanel.setEnable(true,true, true, true);
                wsdlgenBean.setServiceName(txtService.getText().trim());
            }
        }
    }
    public void setDefaultNamespaces(String fullyQualifiedClassName){
        this.txtNsp.setText(NamespaceFinder.getTargetNamespaceFromClass(fullyQualifiedClassName));
        this.txtSchemaTargetNsp .setText(NamespaceFinder.getSchemaTargetNamespaceFromClass(fullyQualifiedClassName) );
        this.txtNspPrefix .setText(NamespaceFinder.getDefaultNamespacePrefix() );
        this.txtSchemaTargetNspPrefix .setText(NamespaceFinder.getDefaultSchemaNamespacePrefix() );
        this.txtService .setText(NamespaceFinder.getServiceNameText(fullyQualifiedClassName) );
        setNamespaceDefaults();
    }

    public void setNamespaceDefaults(){
        wsdlgenBean.setTargetNamespace(txtNsp.getText() );
        wsdlgenBean.setTargetNamespacePrefix(txtNspPrefix .getText() );
        wsdlgenBean.setSchemaTargetNamespace(txtSchemaTargetNsp .getText() );
        wsdlgenBean.setSchemaTargetNamespacePrefix(txtSchemaTargetNspPrefix .getText() );
        wsdlgenBean.setServiceName(txtService .getText() );
    }
}


class OptionLayout  implements LayoutManager {

    public OptionLayout (){

    }
    public void addLayoutComponent(String name, Component comp){

    }

    public void removeLayoutComponent(Component comp){

    }
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 500 + insets.left + insets.right;
        dim.height =500 + insets.top + insets.bottom;

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
            c.setBounds(insets.left + 24, insets.top +20,200, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 225, insets.top + 20, 275, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 60, 200, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 225, insets.top + 60, 275, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 100, 200, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 225, insets.top + 100, 275, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 140, 200, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 225, insets.top + 140, 275, 24);
        }
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 180, 200, 24);
        }
        c = parent.getComponent(9);
        if (c.isVisible()) {
            c.setBounds(insets.left + 225, insets.top + 180, 275, 24);
        }



    }
}
