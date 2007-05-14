package org.apache.axis2.tools.java2wsdl;

import org.apache.axis2.tools.bean.WsdlgenBean;
import javax.swing.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener ;
import java.awt.event.MouseEvent ;
import java.awt.*;
import java.io.File;


public class OutputPanel extends JPanel implements ActionListener,MouseListener  {

    JLabel lblTitle;
    JLabel lblLocation;
    JLabel lblFileName;

    JRadioButton rbtnAdd;
    JRadioButton rbtnSave;

    JTextField txtLocation;
    JTextField txtFileName;

    JButton btnBrowes;

    final JFileChooser DirChooser=new JFileChooser();
    private WsdlgenBean wsdlgenBean;
    private Java2WSDLFrame java2WSDLFrame;

    public OutputPanel(Java2WSDLFrame java2WSDLFrame,WsdlgenBean wsdlgenBean){

        this.wsdlgenBean=wsdlgenBean;
        this.java2WSDLFrame=java2WSDLFrame;
        OutputLayout customLayout=new OutputLayout();
        setLayout(customLayout);

        setFont(new Font("Helvetica", Font.PLAIN, 12));

        // Add label and textfield
        lblTitle =new JLabel("Select the location where to put the output");
        add(lblTitle );

        rbtnAdd =new JRadioButton("Browes and Add the WSDL to a project on current workspace");
        add(rbtnAdd );
        rbtnAdd .setSelected(false);
        rbtnAdd .addActionListener(this);


        rbtnSave =new JRadioButton("Browes and Save the WSDL file on local filesystem ");
        add(rbtnSave );
        rbtnSave .addActionListener(this);
        rbtnSave .setSelected(true);



        lblLocation =new JLabel("OutPut Location");
        add(lblLocation );

        txtLocation =new JTextField();
        add(txtLocation );
        txtLocation .setEnabled(true);
        txtLocation .addActionListener(this);
        txtLocation.addMouseListener(this);


        btnBrowes=new JButton("Browse...");
        add(btnBrowes);
        btnBrowes.setEnabled(true);
        btnBrowes.addActionListener(this);


        lblFileName =new JLabel("OutPut File Name");
        add(lblFileName );

        txtFileName =new JTextField();
        add(txtFileName );
        txtFileName .setEnabled(true);
        txtFileName .addActionListener(this);
        txtFileName .addMouseListener(this);


        setSize(getPreferredSize());


    }
    protected void initializeDefaultSettings() {

        txtLocation.setText(wsdlgenBean.getOutputLocation() );
        txtFileName.setText(wsdlgenBean.getOutputWSDLName());

    }
    public void setOutput(){

        wsdlgenBean.setOutputLocation(txtLocation .getText() );
        wsdlgenBean.setOutputWSDLName(txtFileName .getText() );

    }

    public void actionPerformed(ActionEvent e){
        Object obj=e.getSource();
        if(obj==btnBrowes) {
            DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = DirChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                File newfile = DirChooser.getSelectedFile();
                BottomPanel.setEnable(true,false, true, true);
                txtLocation.setText(newfile.getAbsolutePath());
            }
        } else if(obj == rbtnAdd ) {
            rbtnSave .setSelected(false);
            txtLocation .setEnabled(false);
            btnBrowes.setEnabled(false);
            txtFileName .setEnabled(false);
            BottomPanel.setEnable(true,false, true, true);
            wsdlgenBean.setOutputLocation(java2WSDLFrame .getActiveProject().getProjectFilePath() );
            wsdlgenBean.setOutputWSDLName("Services.wsdl" );            
        }else if(obj == rbtnSave ) {
            rbtnAdd .setSelected(false);
            txtLocation .setEnabled(true);
            btnBrowes.setEnabled(true);
            txtFileName .setEnabled(true);

        } else if(obj ==txtFileName ){
            if (txtFileName .getText() != null && !txtFileName.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtFileName.getText().trim());
            }
        } else if(obj ==txtLocation ){
            if (txtLocation .getText() != null && !txtLocation.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtLocation.getText().trim());
            }
        }
    }
    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtFileName ){
            if (txtFileName .getText() != null && !txtFileName.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtFileName.getText().trim());
            }
        }  else if(obj ==txtLocation ){
            if (txtLocation .getText() != null && !txtLocation.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtLocation.getText().trim());
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        Object obj = e.getSource();
         if(obj ==txtFileName ){
            if (txtFileName .getText() != null && !txtFileName.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtFileName.getText().trim());
            }
        }  else if(obj ==txtLocation ){
            if (txtLocation .getText() != null && !txtLocation.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtLocation.getText().trim());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtFileName ){
            if (txtFileName .getText() != null && !txtFileName.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtFileName.getText().trim());
            }
        }  else if(obj ==txtLocation ){
            if (txtLocation .getText() != null && !txtLocation.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtLocation.getText().trim());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        if(obj ==txtFileName ){
            if (txtFileName .getText() != null && !txtFileName.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtFileName.getText().trim());
            }
        } else if(obj ==txtLocation ){
            if (txtLocation .getText() != null && !txtLocation.getText().trim().equals("")) {
                BottomPanel.setEnable(true,false, true, true);
                wsdlgenBean.setServiceName(txtLocation.getText().trim());
            }
        }
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