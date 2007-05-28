package org.apache.axis2.tools.java2wsdl;

import org.apache.axis2.tools.bean.ClassLoadingTestBean;
import org.apache.axis2.tools.bean.WsdlgenBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.intellij.openapi.ui.MultiLineLabelUI;


public class MiddlePanel extends JPanel implements ActionListener, MouseListener {

    JLabel lblClass;
    JLabel lblPath;
    JLabel lblTest;
    JLabel lblHint;

    JTextField txtClass;

    JButton btnFolder;
    JButton btnJar;
    JButton btnRemove;
    JButton btnTest;

    JList listPathDisply;
    DefaultListModel listModel;

    private String hint ="Hint : Please give the fully qualified class name, example :com.foo.BarService\n" +
            "        Then add the folder or the jar file which contains that class file.\n" +
            "        Finally check whether the class file can be loaded from the plugin.\n\n" +
            "        If the class that you are going to load contains any dependencies\n" +
            "        on other axis2 libraries ( for example like axiom*.jar), please add those\n" +
            "        libraries as well and try to load the class.";

    final JFileChooser FileChooser =new JFileChooser();
    final JFileChooser DirChooser=new JFileChooser();

    private WsdlgenBean wsdlgenBean;
    private Java2WSDLFrame java2WSDLFrame;

    public MiddlePanel (Java2WSDLFrame java2WSDLFrame,WsdlgenBean wsdlgenBean){
        this.wsdlgenBean = wsdlgenBean;
        this.java2WSDLFrame =java2WSDLFrame;

        MiddleLayout customLayout=new MiddleLayout();
        setLayout(customLayout);

        setFont(new Font("Helvetica", Font.PLAIN, 12));

        //add class lable and comboBox

        lblClass=new JLabel("Fully Qualified Class Name : ");
        add(lblClass);

        txtClass=new JTextField();
        txtClass.addActionListener(this);
        txtClass.addMouseListener(this);
        add(txtClass);

        //add folder and jar  display

        lblPath =new JLabel("Java class path entries.Select either folders or jar files ");
        add(lblPath);

        btnFolder=new JButton("Add Folder");
        btnFolder .addActionListener(this);
        add(btnFolder);

        btnJar=new JButton("Add Jar");
        btnJar.addActionListener(this);
        add(btnJar);

        btnRemove=new JButton("Remove");
        btnRemove.addActionListener(this);
        add(btnRemove);

        listModel = new DefaultListModel();
        listPathDisply =new JList(listModel);
        add(listPathDisply);

        //testting class loading

        btnTest=new JButton("Test Class Loading");
        btnTest .addActionListener(this);
        add(btnTest);

        lblTest=new JLabel(" ");
        add(lblTest);

        //Hint Area



        lblHint =new JLabel(hint);
        lblHint .setHorizontalTextPosition( SwingConstants.LEFT );
        lblHint .setUI( new MultiLineLabelUI() );
        add(lblHint );


        setSize(getPreferredSize());

    }



    public void actionPerformed(ActionEvent e) {
        Object obj=e.getSource();
        if(obj == btnFolder ) {
            DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = DirChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                File newfile = DirChooser.getSelectedFile();
                listModel.addElement(newfile.getAbsolutePath() );
                BottomPanel.setEnable(false,true, false, true);
                setDefaultPathAndName(newfile );
                updateStatusTextField( false,"");

            }

        }else if(obj == btnJar ) {

            FileChooser.setFileFilter(new JarFileFilter() );
            int returnVal= FileChooser.showOpenDialog(this);
            if(returnVal == JFileChooser .APPROVE_OPTION ){
                File file = FileChooser.getSelectedFile();
                listModel.addElement(file.getAbsolutePath() );
                BottomPanel.setEnable(false,true, false, true);
                setDefaultPathAndName(file );
                updateStatusTextField(false,"");
            }

        } else  if( obj == btnRemove){

            handleRemove();

        }else  if(obj == btnTest ){
            if(!testLoading()){
                BottomPanel.setEnable(false,false,false,true);

            }else  {
                BottomPanel.setEnable(false,true,true,true);
                wsdlgenBean.setClassPathList(getClassPathlist());
                wsdlgenBean.setClassName(txtClass.getText().trim() );
            }

        }else if(obj ==txtClass ){
            if (txtClass.getText() != null && !txtClass.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                wsdlgenBean.setClassName(txtClass.getText().trim());
            }
        }
    }
    private void setDefaultPathAndName(File file)  {
        String defualtOutPutPath=file.getParent();
        java2WSDLFrame.outPanel.txtLocation .setText(defualtOutPutPath);
        java2WSDLFrame.outPanel .txtFileName .setText("services.wsdl");

    }

    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtClass) {
            if (txtClass.getText() != null && !txtClass.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                // set wsdlgen FileName
                wsdlgenBean.setClassName(txtClass.getText().trim());
            }
        }

    }

    public void mouseEntered(MouseEvent e){
    }

    public void mouseExited(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtClass) {
            if (txtClass.getText() != null && !txtClass.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                // set wsdlgen FileName
                wsdlgenBean.setClassName(txtClass.getText().trim());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtClass) {
            if (txtClass.getText() != null && !txtClass.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                // set wsdlgen FileName
                wsdlgenBean.setClassName(txtClass.getText().trim());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtClass) {
            if (txtClass.getText() != null && !txtClass.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);

                // set wsdlgen FileName
                wsdlgenBean.setClassName(txtClass.getText().trim());
            }
        }
    }

    public void updateStatusTextField(boolean success,String text){
        if (success){
            java2WSDLFrame.opPanel.setDefaultNamespaces(txtClass.getText());
        }
        lblTest.setText(text);
    }


    //  Pops up the file browse dialog box

    private void handleRemove() {
        int[] selectionIndices = listPathDisply .getSelectedIndices() ;
        for (int i=0;i<selectionIndices.length;i++){
            listModel .remove(selectionIndices[i]);
        }
        updateStatusTextField(false,"");
    }

    public String[] getClassPathlist(){
        Object [] listObject = listModel.toArray() ;
        String [] listString =new String[listObject.length];
        for(int i=0 ;i<listObject.length ;i++){
            listString[i]=listObject[i].toString() ;
        }
        return listString ;
    }

    public boolean testLoading(){
        java.util.List errorListener = new ArrayList();
        String [] listString =getClassPathlist() ;
        if (!ClassLoadingTestBean.tryLoadingClass(txtClass.getText(),listString,errorListener)){
            Iterator it = errorListener.iterator();
            while(it.hasNext()){
                Object nextObject = it.next();
                String errorMessage = nextObject==null? "Unknown error!" :nextObject.toString();
                lblTest .setText(errorMessage );
                updateStatusTextField(false,errorMessage);
            }
            return false;
        }else{

            java2WSDLFrame.opPanel.setDefaultNamespaces(txtClass.getText().trim() );
            updateStatusTextField(true,"Class file loaded successfully");
            return true;
        }

    }


}
class MiddleLayout implements LayoutManager{

    public MiddleLayout (){

    }
    public void addLayoutComponent(String name, Component comp) {
    }
    public void removeLayoutComponent(Component comp) {
    }
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 450 + insets.left + insets.right;
        dim.height = 500 + insets.top + insets.bottom;

        return dim;
    }
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        //class
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 20, 180, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 200, insets.top + 20, 300, 24);
        }
        //folders and jar
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 54, 500, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 88, 144, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 190, insets.top + 88,144, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 355, insets.top + 88, 144, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 130 , 475, 150);
        }
        //test class loading
        c= parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 300 , 144 , 24);
        }
        c= parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 180, insets.top + 300 , 475 , 24);
        }
        //hint Area
        c= parent.getComponent(9);
        if (c.isVisible()) {
            c.setBounds(insets.left + 24, insets.top + 330 , 500 ,100);
        }

    }



}
