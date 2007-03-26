package org.apache.axis2.tools.java2wsdl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.axis2.tools.bean.NamespaceFinder ;
import org.apache.axis2.tools.bean.WsdlgenBean;


public class OptionPanel extends JPanel implements ActionListener {

    private   JLabel lblNsp;
    private   JLabel lblNspPrefix;
    private   JLabel lblSchemaTargetNsp;
    private   JLabel lblSchemaTargetNspPrefix;
    private   JLabel lblService;

    private   JTextField txtNsp;
    private   JTextField txtNspPrefix;
    private   JTextField txtSchemaTargetNsp;
    private   JTextField txtSchemaTargetNspPrefix;
    private   JTextField txtService;

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

    }


    public void setDefaultNamespaces(String fullyQualifiedClassName){
        this.txtNsp.setText(NamespaceFinder.getTargetNamespaceFromClass(fullyQualifiedClassName));
        this.txtSchemaTargetNsp .setText(NamespaceFinder.getSchemaTargetNamespaceFromClass(fullyQualifiedClassName) );
        this.txtNspPrefix .setText(NamespaceFinder.getDefaultNamespacePrefix() );
        this.txtSchemaTargetNspPrefix .setText(NamespaceFinder.getDefaultSchemaNamespacePrefix() );
        this.txtService .setText(NamespaceFinder.getServiceNameText(fullyQualifiedClassName) );
        setNamespaceDefaults();
    }

    private void setNamespaceDefaults(){
        wsdlgenBean.setTargetNamespace(txtNsp.getText() );
        wsdlgenBean.setTargetNamespacePrefix(txtNspPrefix .getText() );
        wsdlgenBean.setSchemaTargetNamespace(txtSchemaTargetNsp .getText() );
        wsdlgenBean.setSchemaTargetNamespacePrefix(txtSchemaTargetNspPrefix .getText() );
        wsdlgenBean.setServiceName(txtService .getText() );
    }
}


class OptionLayout   implements LayoutManager {

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
        /*c = parent.getComponent(8);
    if (c.isVisible()) {
        c.setBounds(insets.left + 24, insets.top + 150, 200, 24);
    }
    c = parent.getComponent(9);
    if (c.isVisible()) {
        c.setBounds(insets.left + 225, insets.top + 150, 275, 24);
    }    */


    }
}
