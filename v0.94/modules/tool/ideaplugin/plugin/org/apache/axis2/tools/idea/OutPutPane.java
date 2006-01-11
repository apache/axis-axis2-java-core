package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
 * Date: Jul 22, 2005
 * Time: 12:52:31 PM
 */
public class OutPutPane extends JPanel implements ActionListener {
    JLabel lbloutput;
    JTextField txtoutput;
    JButton btwBrowse;
    private CodegenBean cogenbean;
    final JFileChooser fc = new JFileChooser();

    public OutPutPane(CodegenBean bean) {
        this.cogenbean = bean;
        OutPutPaneLayout customLayout = new OutPutPaneLayout();

        setLayout(customLayout);

        lbloutput = new JLabel("Output Path");
        add(lbloutput);

        txtoutput = new JTextField("");
        add(txtoutput);

        btwBrowse = new JButton("Browse..");
        add(btwBrowse);
        btwBrowse.addActionListener(this);

        setSize(getPreferredSize());
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btwBrowse) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                cogenbean.setOutput(file.getAbsolutePath());
                txtoutput.setText(file.getAbsolutePath());
            } else {
                System.out.println("no file");
            }
        }
    }
}

class OutPutPaneLayout implements LayoutManager {

    public OutPutPaneLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 611 + insets.left + insets.right;
        dim.height = 57 + insets.top + insets.bottom;

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
            c.setBounds(insets.left + 8, insets.top + 8, 72, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 88, insets.top + 8, 354, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 450, insets.top + 8, 80, 24);
        }
    }
}

