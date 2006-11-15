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
 * Date: Sep 17, 2005
 * Time: 10:09:08 PM
 */
public class FirstFrame extends JPanel implements ObjectKeeper {
    JLabel lblArchivetype;
    ButtonGroup cbgservoceType;
    ButtonGroup cbggenerateserviceDesc;
    JRadioButton radioSingle;
    JRadioButton serviGroup;
    JLabel label_1;
    JRadioButton radioGenerate;
    JRadioButton radihaveService;

    private JPanel previous;
    protected ClassSelctionPage classPage;
    ServiceArciveFrame parent;

    public FirstFrame(ServiceArciveFrame parent) {
        this.parent = parent;
        FirstFrameLayout customLayout = new FirstFrameLayout();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        lblArchivetype = new JLabel("Select Archive Type");
        add(lblArchivetype);

        cbgservoceType = new ButtonGroup();
        radioSingle = new JRadioButton("Single service Archive", true);
        cbgservoceType.add(radioSingle);
        add(radioSingle);

        serviGroup = new JRadioButton("Service Group Archive", false);
        cbgservoceType.add(serviGroup);
        add(serviGroup);

        label_1 = new JLabel("Do you want to generate services.xml");
        add(label_1);

        cbggenerateserviceDesc = new ButtonGroup();
        radioGenerate = new JRadioButton("Generate services.xml", true);
        cbggenerateserviceDesc.add(radioGenerate);
        add(radioGenerate);

        radihaveService = new JRadioButton("I already have services.xml", false);
        cbggenerateserviceDesc.add(radihaveService);
        add(radihaveService);

        //creating next page
//        classPage = new ClassSelctionPage(parent);
//        classPage.setPrivious(this);
        setSize(getPreferredSize());
    }

    public void fillBean(ArchiveBean bean) {
        bean.setGeneretServiceDesc(radioGenerate.isSelected());
        bean.setSingleService(radioSingle.isSelected());
    }

    //to keep a refernce to next panel
    public void setNext(JPanel next) {
//        this.next = next;
    }

    public JPanel getNext() {
        boolean singleservice = radioSingle.isSelected();
        boolean generateXML = radioGenerate.isSelected();
        
        if (singleservice && generateXML) {
            parent.singleService = true;
            parent.generateServiceXML = true;
            ClassSelctionPage classPage = new ClassSelctionPage(parent);
            classPage.setPrivious(this);
            return classPage;
        } else if (!generateXML) {
            parent.generateServiceXML = false;
            XMLSelectionPage xml = new XMLSelectionPage(parent);
            xml.setPrivious(this);
            return xml;
        } else {
            parent.generateServiceXML = true;
            parent.singleService = false;
            ClassSelctionPage classPage = new ClassSelctionPage(parent);
            classPage.setPrivious(this);
            return classPage;
        }
    }

    //to keep a refernce to previous panel
    public void setPrivious(JPanel privious) {
        this.previous = privious;
    }

    public JPanel getPrivious() {
        return this.previous;
    }

    public String getTopLable() {
        return "Service Type selection";
    }

    public String getLable() {
        return "Welcome to Axis2 service archive generation. " +
                "Select service type";
    }
}

class FirstFrameLayout implements LayoutManager {

    public FirstFrameLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 494 + insets.left + insets.right;
        dim.height = 281 + insets.top + insets.bottom;

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
            c.setBounds(insets.left + 8, insets.top + 24, 208, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 32, insets.top + 56, 184, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 32, insets.top + 88, 184, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 136, 250, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 32, insets.top + 168, 224, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 32, insets.top + 200, 224, 16);
        }
    }
}
