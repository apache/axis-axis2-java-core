package org.apache.axis2.tools.java2wsdl;

import org.apache.axis2.tools.idea.*;
import org.apache.axis2.tools.bean.WsdlgenBean;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import java.awt.*;


public class Java2WSDLFrame extends JFrame {

    // first panel
    ImagePanel imgPanel;
    MiddlePanel plMiddle;
    BottomPanel plBottom;
    OptionPanel opPanel;
    OutputPanel outPanel;


    private int panleID = 1;
    private ClassLoader classLoader;
    Project project;

    private WsdlgenBean wsdlgenBean;
    private int defaultCloseOperation;
    public Java2WSDLFrame (){

        windowLayout customLayout = new windowLayout(1);

        setTitle("Axis2 Codegen Wizard ");

        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(customLayout);

        wsdlgenBean=new WsdlgenBean();
        //add image panel

        imgPanel = new ImagePanel();
        imgPanel.setCaptions("  Java source/classpath selection"
                , "  Welcome to the Axis2 Java source code generation wizard.");
        getContentPane().add(imgPanel);

        //add bottom panel

        plBottom = new BottomPanel(this);
        BottomPanel.setEnable(false,false, false, true);
        getContentPane().add(plBottom);

        //add middle panel

        plMiddle = new MiddlePanel(this,wsdlgenBean);
        getContentPane().add(plMiddle);

        //add option panel

        opPanel =new OptionPanel(this,wsdlgenBean);
        opPanel.setVisible(false);
        getContentPane().add(opPanel);

        //add option panel

        outPanel=new OutputPanel(this,wsdlgenBean);
        outPanel .setVisible(false);
        getContentPane() .add(outPanel);

        //add progress panel



        Dimension dim = new Dimension(550, 550);
        setSize(dim);
        setBounds(200, 200, dim.width, dim.height);


    }

    public void setDefaultCloseOperation(int operation) {
        if (operation != DO_NOTHING_ON_CLOSE &&
                operation != HIDE_ON_CLOSE &&
                operation != DISPOSE_ON_CLOSE &&
                operation != EXIT_ON_CLOSE) {
            throw new IllegalArgumentException("defaultCloseOperation must be one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, DISPOSE_ON_CLOSE, or EXIT_ON_CLOSE");
        }
        if (this.defaultCloseOperation != operation) {
            if (operation == EXIT_ON_CLOSE) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkExit(0);
                }
            }
            int oldValue = this.defaultCloseOperation;
            this.defaultCloseOperation = operation;
            firePropertyChange("defaultCloseOperation", oldValue, operation);
        }
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getActiveProject() {
        return project;

    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generatecode() throws Exception {

        wsdlgenBean.generate();
    }


    public void setPanel(){

        panleID++;

        switch (panleID) {
            case 1:{
                this.imgPanel .setCaptions(" Java source/classpath selection" ,
                        "  Select the classes and the libraries.");

                this.plMiddle .setVisible(true);

                this.opPanel.setVisible(false);

                this.outPanel .setVisible(false);

                plBottom.setEnable(false,false,false, true);

                break;
            }
            case 2: {
                this.imgPanel .setCaptions(" Java to WSDL Options " ,
                        "  Set the Options for the generator.");
                this.plMiddle .setVisible(false);

                this.opPanel.setVisible(true);

                this.outPanel .setVisible(false);

                plBottom.setEnable(true,true, true, true);

                break;
            }
            case 3: {
                this.imgPanel .setCaptions(" WSDL file Output location " ,
                        "  Select the location for the generated WSDL.");
                this.plMiddle .setVisible(false);

                this.opPanel.setVisible(false);

                this.outPanel .setVisible(true);

                plBottom.setEnable(true,false, true, true);

                break;
            }
        }

    }
    public void backButtonImpl(){
        panleID--;
        switch (panleID) {
            case 1: {

                this.imgPanel .setCaptions(" Java source/classpath selection" ,
                        "  Select the classes and the libraries.");

                this.opPanel.setVisible(false);

                this.plMiddle.setVisible(true);

                this.outPanel.setVisible(false);

                BottomPanel.setEnable(false,true, false, true);

                break;
            }

            case 2: {

                this.imgPanel .setCaptions(" Java to WSDL Options " ,
                        "  Set the Options for the generator.");

                this.plMiddle .setVisible(false);

                this.outPanel.setVisible(false);

                this.opPanel.setVisible(true);

                BottomPanel.setEnable(true,true, true, true);

                break;
            }

            case 3: {

                this.imgPanel .setCaptions(" WSDL file Output location " ,
                        "  Select the location for the generated WSDL.");

                this.plMiddle .setVisible(false);

                this.outPanel.setVisible(true);

                this.opPanel.setVisible(false);

                BottomPanel.setEnable(true,false, true, true);

                break;

            }

        }

    }

}

class windowLayout implements LayoutManager{

    int paneID;

    public windowLayout(int panelid) {
        paneID = panelid;
    }
    public void addLayoutComponent(String name, Component comp) {
    }
    public void removeLayoutComponent(Component comp) {
    }
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 550+ insets.left + insets.right;
        dim.height = 600 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top, 550, 80);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 500, 450);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 500, 450);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 500, 450);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 525, 500, 50);
        }

    }

}