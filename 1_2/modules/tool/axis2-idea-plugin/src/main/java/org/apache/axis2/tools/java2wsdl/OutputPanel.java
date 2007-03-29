package org.apache.axis2.tools.java2wsdl;

import org.apache.axis2.tools.bean.WsdlgenBean;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


public class OutputPanel extends JPanel implements ActionListener {

    JLabel lblTitle;
    JLabel lblLocation;
    JLabel lblFileName;

    JRadioButton rbtnAdd;
    JRadioButton rbtnSave;

    JTextField txtLocation;
    JTextField txtFileName;

    JButton btnBrowes;

    final JFileChooser fc = new JFileChooser();
    private WsdlgenBean wsdlgenBean;

    public OutputPanel(WsdlgenBean wsdlgenBean){

        this.wsdlgenBean=wsdlgenBean;

        OutputLayout customLayout=new OutputLayout();
        setLayout(customLayout);

        setFont(new Font("Helvetica", Font.PLAIN, 12));

        // Add label and textfield
        lblTitle =new JLabel("Select the location where to put the output");
        add(lblTitle );

        rbtnAdd =new JRadioButton("Browes and Add the WSDL to a project on current workspace");
        add(rbtnAdd );

        rbtnSave =new JRadioButton("Browes and Save the WSDL file on local filesystem ");
        add(rbtnSave );

        lblLocation =new JLabel("OutPut Location");
        add(lblLocation );

        txtLocation =new JTextField();
        add(txtLocation );

        btnBrowes=new JButton("Browse...");
        add(btnBrowes);

        lblFileName =new JLabel("OutPut File Name");
        add(lblFileName );

        txtFileName =new JTextField();
        add(txtFileName );

        setSize(getPreferredSize());

        initializeDefaultSettings();


    }
    protected void initializeDefaultSettings() {

        txtLocation.setText(wsdlgenBean.getOutputLocation() );
        txtFileName.setText(wsdlgenBean.getOutputWSDLName());

    }
    public void setNamespace(){
        wsdlgenBean.setTargetNamespace(txtLocation .getText() );
        wsdlgenBean.setTargetNamespacePrefix(txtFileName .getText() );

    }

    public void actionPerformed(ActionEvent e){

    }
}

class OutputLayout implements LayoutManager{

    public OutputLayout (){

    }

    public void addLayoutComponent(String name, Component comp) {
    }
    public void removeLayoutComponent(Component comp) {
    }
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 400 + insets.left + insets.right;
        dim.height = 400 + insets.top + insets.bottom;

        return dim;
    }
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent){
        Insets insets = parent.getInsets();

        Component c;

        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 10, 400, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 40, 400, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 70, 400, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 110, 100, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 130, insets.top + 110, 290, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 420, insets.top + 110, 80, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 150, 100, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 130, insets.top + 150, 290, 24);
        }
    }
}