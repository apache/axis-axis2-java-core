package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
 * Date: Jul 20, 2005
 * Time: 3:35:47 PM
 */
public class FirstPanel extends JPanel implements ActionListener, MouseListener {

    JLabel lblWSDL;
    JTextField txtWSDL;
    JButton btnBrowse;
    final JFileChooser fc = new JFileChooser();
    private CodegenBean codegenBean;

    public FirstPanel(CodegenBean codegenBean) {
        this.codegenBean = codegenBean;
        FirstPanelLayout customLayout = new FirstPanelLayout();
        setLayout(customLayout);

        setFont(new Font("Helvetica", Font.PLAIN, 12));
        lblWSDL = new JLabel("WSDL File");
        add(lblWSDL);

        txtWSDL = new JTextField("");
        add(txtWSDL);
        txtWSDL.addActionListener(this);
        txtWSDL.addMouseListener(this);

        btnBrowse = new JButton("Browse...");
        add(btnBrowse);
        btnBrowse.addActionListener(this);


        Dimension dim = new Dimension(450, 600);
        setSize(dim);
        fc.setFileFilter(new WSDLFileFilter());


    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnBrowse) {

            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                txtWSDL.setText(file.getAbsolutePath());
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(file.getAbsolutePath());
            } 
        } else if (obj == txtWSDL) {
            if (txtWSDL.getText() != null && !txtWSDL.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(txtWSDL.getText().trim());
            }

        }

    }

    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtWSDL) {
            if (txtWSDL.getText() != null && !txtWSDL.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(txtWSDL.getText().trim());
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtWSDL) {
            if (txtWSDL.getText() != null && !txtWSDL.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(txtWSDL.getText().trim());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtWSDL) {
            if (txtWSDL.getText() != null && !txtWSDL.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(txtWSDL.getText().trim());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        if (obj == txtWSDL) {
            if (txtWSDL.getText() != null && !txtWSDL.getText().trim().equals("")) {
                BottomPanel.setEnable(false,true, false, true);
                codegenBean.setWSDLFileName(txtWSDL.getText().trim());
            }
        }
    }
}

class WSDLFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            return extension.equals("wsdl");
        }

        return false;

    }

    public String getDescription() {
        return ".wsdl";
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}

class FirstPanelLayout implements LayoutManager {

    public FirstPanelLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 541 + insets.left + insets.right;
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
            c.setBounds(insets.left + 8, insets.top + 8, 72, 24);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 88, insets.top + 8, 350, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 448, insets.top + 8, 90, 24);
        }
    }
}
