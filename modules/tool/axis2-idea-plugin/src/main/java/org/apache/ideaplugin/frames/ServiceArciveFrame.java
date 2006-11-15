package org.apache.ideaplugin.frames;

import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.ObjectKeeper;

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
 * Author: Deepal Jayasinghe
 * Date: Sep 18, 2005
 * Time: 11:45:58 AM
 */
public class ServiceArciveFrame extends JFrame {

    protected ImagePanel topPanel;
    protected BottomPanel bottomPanel;
    public boolean singleService;
    public boolean generateServiceXML;
    //  protected JPanel firstpanel ;
    //    protected SelectPanel slectpanel;
    protected JPanel currentpanle;
    public String fileSeparator = System.getProperty("file.separator");
    public final JFileChooser fc = new JFileChooser();
    Insets insets;
    ArchiveBean bean;

    public ServiceArciveFrame() {
        setBounds(200, 200, 600, 420);
        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(null);
        getContentPane().setBounds(200, 200, 600, 420);
        bean = new ArchiveBean();

        topPanel = new ImagePanel();
        getContentPane().add(topPanel);
        topPanel.setCaptions("Service Type selection", "Welcome to Axis2 service archive generation. " +
                "Select service type");

        currentpanle = new FirstFrame(this);
        getContentPane().add(currentpanle);


        bottomPanel = new BottomPanel(currentpanle, this, bean);
        getContentPane().add(bottomPanel);
        bottomPanel.setEnable(false, true, false, true);

        insets = getInsets();
        topPanel.setBounds(insets.left, insets.top, 608, 80);
        currentpanle.setBounds(insets.left, insets.top + 80, 608, 260);
        bottomPanel.setBounds(insets.left, insets.top + 328, 608, 60);
        setSize(getPreferredSize());
        setResizable(false);
        

    }

    public void setEnable(boolean back, boolean next, boolean finish, boolean cancel) {
        if (currentpanle instanceof FirstFrame) {
            bottomPanel.setEnable(back, next, finish, cancel);
        } else {
            bottomPanel.setEnable(back, next, finish, cancel);
        }

    }

    public void Next(JPanel current) {
        currentpanle.setVisible(false);
        try {
            remove(currentpanle);
        } catch (Exception e) {
            System.out.println("");
        }
        getContentPane().add(current);
        current.setBounds(insets.left, insets.top + 80, 608, 260);
        currentpanle = current;
        
        reShow();
    }

    public void Back(JPanel current) {
        currentpanle.setVisible(false);
        try {
            remove(currentpanle);
        } catch (Exception e) {
            System.out.println("");
        }
        getContentPane().add(current);
        current.setBounds(insets.left, insets.top + 80, 608, 260);
        currentpanle = current;
        currentpanle.setVisible(true);
        setEnable(true, true, false, true);
        reShow();
    }

    public void reShow() {
        ObjectKeeper keeper = (ObjectKeeper) currentpanle;
        topPanel.setCaptions(keeper.getTopLable(), keeper.getLable());
        this.show();
    }

    class MainFrameLayout implements LayoutManager {

        public MainFrameLayout() {
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = new Dimension(0, 0);

            Insets insets = parent.getInsets();
            dim.width = 608 + insets.left + insets.right;
            dim.height = 400 + insets.top + insets.bottom;

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
                c.setBounds(insets.left, insets.top, 608, 80);
            }
            c = parent.getComponent(1);
            if (c.isVisible()) {
                c.setBounds(insets.left, insets.top + 80, 608, 260);
            }
            c = parent.getComponent(2);
            if (c.isVisible()) {
                c.setBounds(insets.left, insets.top + 328, 608, 60);
            }
//        c = parent.getComponent(3);
//        if (c.isVisible()) {c.setBounds(insets.left+0,insets.top+80,608,260);}
        }
    }
}