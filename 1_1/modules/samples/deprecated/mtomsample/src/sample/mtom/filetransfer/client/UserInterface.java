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

package sample.mtom.filetransfer.client;

import org.apache.axiom.om.OMElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class UserInterface extends JPanel implements ActionListener {
    public static final int WIDTH = 480;
    public static final int HEIGHT = 560;

    JButton brwsBut1;
    JButton brwsBut2;
    JButton addFileButton;
    JButton removeButton;
    JButton executeButton;

    JRadioButton sendRadio;
    JRadioButton sendRecRadio;
    JRadioButton MTOMRadio;
    JRadioButton SOAPRadio;

    JCheckBox cacheBox;

    DefaultListModel model;
    JList fileList;
    JScrollPane fileListScroller;
    JFileChooser fileChooser;

    File file = null;

    JTextField cacheThresholdText;
    JTextField cacheFolderText;
    JTextField EPRText;
    JTextField destFolderText;
    JTextField fileField;

    JLabel fileListLabel;
    JLabel EPRLabel;
    JLabel destDir;
    JLabel opLabel;
    JLabel MTOMSOAPLabel;
    JLabel thresholdLabel;
    JLabel cacheFolderLabel;
    JLabel bytesLabel;

    private boolean cacheEnable = false;

    private String destFolder = null;

    private String EPR = null;

    private ArrayList files;

    private MTOMClient parent;

    private MTOMClientModel mtomTest;

    public UserInterface(MTOMClient parent) {
        this.parent = parent;
        initComponents();

        brwsBut1.addActionListener(this);
        brwsBut2.addActionListener(this);
        addFileButton.addActionListener(this);
        removeButton.addActionListener(this);

        fileField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                addFileButton.setEnabled(true);

            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (fileField.getText().length() == 0) {
                    addFileButton.setEnabled(false);
                }
            }
        });

        MTOMRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchRadios(MTOMRadio, SOAPRadio);
            }
        });
        SOAPRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchRadios(SOAPRadio, MTOMRadio);
            }
        });

        sendRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchRadios(sendRadio, sendRecRadio);
                cacheBox.setEnabled(false);
                cacheBox.setSelected(false);
                enableCaching();
            }
        });
        sendRecRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchRadios(sendRecRadio, sendRadio);
                cacheBox.setEnabled(true);
                enableCaching();
            }
        });

        cacheBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableCaching();
            }
        });

        executeButton.addActionListener(this);


        Container pane = parent.getContentPane();
        this.setLayout(null);

        pane.add(fileField);
        pane.add(brwsBut1);
        pane.add(addFileButton);
        pane.add(removeButton);

        pane.add(fileListLabel);
        pane.add(fileListScroller);

        pane.add(destDir);
        pane.add(destFolderText);

        pane.add(EPRLabel);
        pane.add(EPRText);

        pane.add(opLabel);
        pane.add(MTOMRadio);
        pane.add(SOAPRadio);

        pane.add(MTOMSOAPLabel);
        pane.add(sendRadio);
        pane.add(sendRecRadio);

        pane.add(cacheBox);

        pane.add(thresholdLabel);
        pane.add(cacheThresholdText);
        pane.add(bytesLabel);

        pane.add(cacheFolderLabel);
        pane.add(cacheFolderText);
        pane.add(brwsBut2);

        pane.add(executeButton);
    }

    public void initComponents() {
        files = new ArrayList(0);

        fileField = new JTextField();
        fileField.setBounds(20, 20, 320, 20);

        this.brwsBut1 = new JButton("Browse");
        brwsBut1.setBounds(350, 20, 100, 20);
        brwsBut1.setToolTipText("Browse a file");

        addFileButton = new JButton("Add");
        addFileButton.setBounds(20, 50, 100, 20);
        addFileButton.setToolTipText("Add file to the file list");
        addFileButton.setEnabled(false);

        removeButton = new JButton("Remove Selection");
        removeButton.setBounds(140, 50, 150, 20);
        removeButton.setToolTipText("Remove selected file from the file list");
        removeButton.setEnabled(false);

        fileListLabel = new JLabel("File List");
        fileListLabel.setBounds(20, 80, 50, 20);

        model = new DefaultListModel();
        fileList = new JList(model);
        fileListScroller = new JScrollPane(fileList);
        fileListScroller.setBounds(20, 100, 430, 80);


        destDir = new JLabel("Dest. Folder: ", JLabel.RIGHT);
        destDir.setBounds(20, 200, 100, 20);
        destFolderText = new JTextField();
        destFolderText.setBounds(120, 200, 330, 20);


        EPRLabel = new JLabel("End Point: ", JLabel.RIGHT);
        EPRLabel.setBounds(20, 230, 100, 20);
        EPRText = new JTextField();
        EPRText.setText("http://127.0.0.1:8080/axis2/services/mtomSample");
        EPRText.setBounds(120, 230, 330, 20);

        MTOMSOAPLabel = new JLabel("Send Using");
        MTOMSOAPLabel.setBounds(20, 270, 150, 20);

        MTOMRadio = new JRadioButton("MTOM");
        MTOMRadio.setBounds(20, 295, 100, 20);
        MTOMRadio.setSelected(true);

        SOAPRadio = new JRadioButton("SOAP with Attachments");
        SOAPRadio.setBounds(140, 295, 200, 20);

        opLabel = new JLabel("Select Operation");
        opLabel.setBounds(20, 320, 150, 20);

        sendRadio = new JRadioButton("Send");
        sendRadio.setBounds(20, 345, 100, 20);
        sendRadio.setSelected(true);

        sendRecRadio = new JRadioButton("Send & Receive");
        sendRecRadio.setBounds(140, 345, 150, 20);

        cacheBox = new JCheckBox("Enable Client Side File Caching");
        cacheBox.setSelected(false);
        cacheBox.setEnabled(false);
        cacheBox.setBounds(20, 380, 250, 20);

        thresholdLabel = new JLabel("File Cache Threshold: ");
        thresholdLabel.setBounds(50, 410, 150, 20);
        thresholdLabel.setEnabled(false);
        cacheThresholdText = new JTextField();
        cacheThresholdText.setBounds(200, 410, 40, 20);
        cacheThresholdText.setEnabled(false);

        bytesLabel = new JLabel("(in bytes)");
        bytesLabel.setBounds(250, 410, 100, 20);
        bytesLabel.setEnabled(false);

        cacheFolderLabel = new JLabel("Cache Folder: ");
        cacheFolderLabel.setBounds(50, 440, 150, 20);
        cacheFolderLabel.setEnabled(false);
        cacheFolderText = new JTextField();
        cacheFolderText.setBounds(200, 440, 210, 20);
        cacheFolderText.setEnabled(false);

        brwsBut2 = new JButton("...");
        brwsBut2.setBounds(420, 440, 30, 20);
        brwsBut2.setToolTipText("Browse for a cache folder");
        brwsBut2.setEnabled(false);

        this.executeButton = new JButton("Execute");
        executeButton.setBounds(((WIDTH - 200) / 2), 490, 200, 20);

        fileChooser = new JFileChooser();
        fileChooser.setName("File Chooser");
    }

    public void handleSelection() {
        if (!files.isEmpty()) {
            removeButton.setEnabled(false);
        }
    }

    public void enableCaching() {
        cacheEnable = cacheBox.isSelected();
        thresholdLabel.setEnabled(cacheEnable);
        cacheThresholdText.setEnabled(cacheEnable);
        cacheFolderLabel.setEnabled(cacheEnable);
        cacheFolderText.setEnabled(cacheEnable);
        brwsBut2.setEnabled(cacheEnable);
        bytesLabel.setEnabled(cacheEnable);
    }

    public void switchRadios(JRadioButton me, JRadioButton partner) {
        me.setSelected(true);
        partner.setSelected(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == brwsBut1) {
            String str = browse(JFileChooser.FILES_ONLY);
            if(str != null){
                fileField.setText(str);
                addFileButton.setEnabled(true);
            }
        } else if (e.getSource() == brwsBut2) {
            String str = browse(JFileChooser.FILES_AND_DIRECTORIES);
            if(str != null){
                cacheFolderText.setText(str);
            }
        } else if (e.getSource() == executeButton) {
            execute();
        } else if (e.getSource() == addFileButton) {
            addFile();
        } else if (e.getSource() == removeButton) {
            removeFromList();
        }
    }

    public String browse(int selectionMode) {
        fileChooser.setFileSelectionMode(selectionMode);
        int returnVal = fileChooser.showDialog(this, "Select");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (file.getAbsolutePath() != null) {
                return file.getAbsolutePath();
            }
        }
        fileChooser.setSelectedFile(null);
        return null;
    }

    public void addFile() {
        file = new File(fileField.getText());
        if (file.exists() && file.isFile()) {
            files.add(file);
            model.addElement(file.getAbsolutePath());
            fileList.setSelectedIndex(files.size() - 1);
            removeButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(parent,
                    "File does not exist", "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeFromList() {
        String selection = (String) fileList.getSelectedValue();
        if (selection != null) {
            file = new File(selection);
            files.remove(file);
            model.remove(fileList.getSelectedIndex());
            fileList.setSelectedIndex(files.size() - 1);
            if (files.isEmpty()) {
                removeButton.setEnabled(false);
            }
        }
    }

    public void execute() {
        EPR = EPRText.getText();
        String operation;
        String sendMethod;
        String cacheFolder = null;
        int cacheThreshold = 0;
        File cache;
        destFolder = destFolderText.getText();

        if (!model.isEmpty()) {
            if (destFolder.length() != 0 && EPR.length() != 0) {
                sendMethod = (MTOMRadio.isSelected() ? "MTOM" : "SOAP");
                operation = (sendRadio.isSelected() ? "send" : "sendreceive");
                if (cacheEnable) {
                    try {
                        String temp = cacheThresholdText.getText();
                        if (temp.length() != 0) {
                            cacheThreshold = Integer.parseInt(temp);
                        } else {
                            throw new NumberFormatException();
                        }

                        cache = new File(cacheFolderText.getText());
                        if (!cache.exists()) {
                            cache.mkdirs();
                        }
                        cacheFolder = cache.getAbsolutePath();

                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(parent, "Please enter an integer value",
                                "Cache Threshold Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                mtomTest = new MTOMClientModel();
                mtomTest.setFileList(files);
                mtomTest.setTargetEPR(EPR);

                if (operation.equals("send")) {
                    send(sendMethod);
                } else {
                    sendAndReceive(sendMethod, cacheThreshold, cacheFolder);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Destination Folder or End Point cannot be null",
                        "Data Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Add at least one file",
                    "File List Empty", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendAndReceive(String sendMethod, int cacheThreshold, String cacheFolder) {
        OMElement result;
        try {
            mtomTest.setCacheFolder(cacheFolder);
            mtomTest.setCacheThreshold(cacheThreshold);
            String temp = (cacheEnable) ? "Enabled" : "Disabled";
            if (sendMethod.equals("MTOM")) {
                result = mtomTest.sendReceiveUsingMTOM(destFolder, cacheEnable);

                temp = "File Caching " + temp + "\n\n" + result.getText();
                JOptionPane.showMessageDialog(parent, temp,
                        "Result of Send & Receive using MTOM", JOptionPane.PLAIN_MESSAGE);
            } else {
                result = mtomTest.sendReceiveUsingSwA(destFolder, cacheEnable);

                temp = "File Caching " + temp + "\n\n" + result.getText();
                JOptionPane.showMessageDialog(parent, temp,
                        "Result of Send & Receive using SwA", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void send(String sendMethod) {
        OMElement result;
        try {
            if (sendMethod.equals("MTOM")) {
                result = mtomTest.sendFilesUsingMTOM(destFolder);
                JOptionPane.showMessageDialog(parent, result.getText(),
                        "Result of Send using MTOM", JOptionPane.PLAIN_MESSAGE);
            } else {
                result = mtomTest.sendFilesUsingSwA(destFolder);
                JOptionPane.showMessageDialog(parent, result.getText(),
                        "Result of Send using SOAP with Attachments", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}