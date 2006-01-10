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
 */

package sample.mtom.interop.client;

import org.apache.axis2.om.OMElement;

import javax.swing.*;
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

    private String desiredFileName = null;

    private String EPRName = null;

    private InteropClient parent;

    public UserInterface(InteropClient parent) {
        this.parent = parent;
        EPR = new JTextField();
        fileFeild = new JTextField();
        fileName = new JTextField();
        label = new JLabel("END Point:");
        savefile = new JLabel("Desired File Name:");
        jTextArea = new JTextArea(5, 5);
        jTextArea.setPreferredSize(new Dimension(200, 100));

        jFileChooser = new JFileChooser();
        jFileChooser.setName("File Chooser");
        this.browse = new JButton("Browse");
        this.send = new JButton("Send The File");
        fileFeild.setBounds(20, 20, 270, 20);
        browse.setBounds(300, 20, 120, 20);
        savefile.setBounds(20, 60, 200, 20);
        fileName.setBounds(150, 60, 270, 20);

        label.setBounds(20, 90, 200, 20);
        EPR.setBounds(150, 90, 270, 20);

        EPR.setText("http://127.0.0.1:8080/axis2/services/interopService");

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
                    "Choose the File to Send");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser.getSelectedFile();
                if (file.getAbsolutePath() != null) {
                    fileFeild.setText(file.getAbsolutePath());
                }
            } 
            jFileChooser.setSelectedFile(null);
        }
        if (e.getSource() == send) {
            if (fileName.getText() != null) {
                desiredFileName = fileName.getText();
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
                this.send(desiredFileName);
            }

        }
    }

    public void send(String fileName) {

        InteropClientModel mtomTest = new InteropClientModel();

        try {
            mtomTest.setTargetEPR(EPRName);
            OMElement result = mtomTest.testEchoXMLSync(fileName);
        } catch (Exception e) {
            e.printStackTrace(); 
        }

    }
}