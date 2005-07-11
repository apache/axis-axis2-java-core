/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package sample.mtom.client;

import org.apache.axis2.om.OMElement;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UserInterface extends JPanel implements ActionListener {

    JButton browse;

    JButton send;

    JTextArea jTextArea;

    JFileChooser jFileChooser;

    File file = null;

    JTextField EPR;

    JTextField fileName;

    JTextField fileFeild;

    JLabel label;

    JLabel savefile;

    private String deriredFileName = null;

    private String EPRName = null;

    private MTOMClient parent;

    public UserInterface(MTOMClient parent) {
        this.parent = parent;
        EPR = new JTextField();
        fileFeild = new JTextField();
        fileName = new JTextField();
        label = new JLabel("END Point:");
        savefile = new JLabel("Desired File Name:");
        jTextArea = new JTextArea(5, 5);
        jTextArea.setPreferredSize(new Dimension(200, 100));

        jFileChooser = new JFileChooser();
        jFileChooser.addChoosableFileFilter(new ImageFilter());
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setName("Image Chosser");
        this.browse = new JButton("Browse");
        this.send = new JButton("Send The Image");
        fileFeild.setBounds(20, 20, 270, 20);
        browse.setBounds(300, 20, 120, 20);
        savefile.setBounds(20, 60, 200, 20);
        fileName.setBounds(150, 60, 270, 20);

        label.setBounds(20, 90, 200, 20);
        EPR.setBounds(150, 90, 270, 20);

        EPR.setText("http://127.0.0.1:8080/axis2/services/MTOMService");

        send.setBounds(140, 120, 150, 20);

        jTextArea.setBounds(20, 150, 400, 180);

        browse.addActionListener(this);
        send.addActionListener(this);

        Container pane = parent.getContentPane();
        this.setLayout(null);

        pane.add(browse);

        pane.add(send);
        pane.add(jTextArea);
        pane.add(EPR);
        pane.add(fileFeild);
        pane.add(label);
        pane.add(savefile);
        pane.add(fileName);

    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == browse) {

            int returnVal = jFileChooser.showDialog(this,
                    "Choose the Image to Send");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser.getSelectedFile();
                if (file.getAbsolutePath() != null) {
                    fileFeild.setText(file.getAbsolutePath());
                }
            } else {

            }

            jFileChooser.setSelectedFile(null);

        }
        if (e.getSource() == send) {
            if (fileName.getText() != null) {
                deriredFileName = fileName.getText();
            }
            if (file == null) {
                JOptionPane.showMessageDialog(parent,
                        "Attachments should not be null.", " error",
                        JOptionPane.ERROR_MESSAGE);

            } else if (("").equals(EPR.getText())) {
                JOptionPane.showMessageDialog(parent, "END Point null",
                        " error", JOptionPane.ERROR_MESSAGE);

            } else {
                EPRName = EPR.getText();
                this.send(deriredFileName);
            }

        }
    }

    public void send(String fileName) {

        MTOMClientModel mtomTest = new MTOMClientModel();

        try {
            mtomTest.setInputFile(file);
            mtomTest.setTargetEPR(EPRName);
            OMElement result = (OMElement) mtomTest.testEchoXMLSync(fileName);
            jTextArea.setText(result.toString());
            JOptionPane.showMessageDialog(parent, "Sent & saved Image Succesfully",
                    " Success", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File |
            // Settings | File Templates.
        }

    }

    class ImageFilter extends FileFilter {

        //Accept all directories and all gif, jpg, tiff, or png files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("jpg") || extension.equals("JPEG"))
                    return true;
                else {
                    return false;
                }
            }

            return false;
        }

        public String getDescription() {
            return null; //To change body of implemented methods use File |
            // Settings | File Templates.
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

    //The description of this filter
    public String getDescription() {
        return "Just Images";
    }

}