package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import java.awt.*;

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
 * Author : Deepal Jayasinghe
 * Date: Jul 19, 2005
 * Time: 2:26:15 PM
 */
public class Java2CodeFrame extends JFrame {
    ImagePanel panel_3;
    JPanel plMiddle;
    BottomPanel lblBottom;
//    SecondPanel secondPanel;
    SecondFrame secondPanel;
    OutPutPane outputpane;
    private int panleID = 0;
    private ClassLoader classLoader;

    // To keep the value of wsdl wizzard
    private CodegenBean codegenBean;

    public Java2CodeFrame() {
        windowLayout customLayout = new windowLayout(1);

        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(customLayout);

        codegenBean = new CodegenBean();

        panel_3 = new ImagePanel();
        panel_3.setCaptions("  WSDL selection page"
                , "  Welcome to the Axis2 code generation wizard. Select the WSDL file");

        getContentPane().add(panel_3);

        plMiddle = new FirstPanel(codegenBean);
        getContentPane().add(plMiddle);

        lblBottom = new BottomPanel(this);
        BottomPanel.setEnable(false, false, true);
        getContentPane().add(lblBottom);

        secondPanel = new SecondFrame();
        secondPanel.setVisible(false);
        getContentPane().add(secondPanel);

        outputpane = new OutPutPane(codegenBean);
        outputpane.setVisible(false);
        getContentPane().add(outputpane);

        Dimension dim = getPreferredSize();
        setSize(dim);
        setBounds(200, 200, dim.width, dim.height);
        this.setResizable(false);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void showUI() {
        Java2CodeFrame java2CodeFrame = new Java2CodeFrame();
        java2CodeFrame.setTitle("Axis2 Code generation");
        java2CodeFrame.pack();
        java2CodeFrame.show();
    }

    public void generatecode() throws Exception {
        secondPanel.fillBean();
        codegenBean.generate();
    }


    public void setPane() {
        panleID ++;
        switch (panleID) {
            case 1 : {
                panel_3.setCaptions("  Options"
                        , "  Set the options for the code generation");
                this.secondPanel.setVisible(true);
                this.secondPanel.setCodeGenBean(codegenBean);
                this.plMiddle.setVisible(false);
                BottomPanel.setEnable(true, false, true);
                break;
            }
            case 2 : {
                panel_3.setCaptions("  Output"
                        , "  set the output project for the generated code");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.outputpane.setVisible(true);
                BottomPanel.setEnable(true, true, true);
                break;
            }
        }
    }

    public void setMiddlerPanel(int panel) {
        this.panleID = panel;
        if (panleID == 2) {
            panel_3.setCaptions("  Options"
                    , "  Set the options for the code generation");
            this.secondPanel.setVisible(true);
            this.plMiddle.setVisible(false);
            BottomPanel.setEnable(true, true, true);
        }
        this.pack();
        this.show();
    }


    public static void main(String[] args) {
        Java2CodeFrame java2CodeFrame = new Java2CodeFrame();
        java2CodeFrame.setTitle("Axis2 Code generation");
        java2CodeFrame.pack();
        java2CodeFrame.show();
    }
}

class windowLayout implements LayoutManager {

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
        dim.width = 541 + insets.left + insets.right;
        dim.height = 340 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top, 544, 80);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 544, 180);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 544, 180);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 544, 180);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 290, 544, 50);
        }
    }
}
