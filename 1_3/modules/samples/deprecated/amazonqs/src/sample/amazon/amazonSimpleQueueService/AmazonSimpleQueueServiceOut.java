/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample.amazon.amazonSimpleQueueService;

import sample.amazon.amazonSimpleQueueService.util.QueueManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * GUI class which handles the OUT operations of the queue
 */
public class AmazonSimpleQueueServiceOut extends JFrame {
    private static final String HELP_FILE_NAME = "/docs/AmazonSimpleWebService.html";
    JTextField createQueue;
    JTextField queueCode;
    JTextField read;
    JTextArea resuts;
    JButton loadButton;
    JButton deleteButton;

    public AmazonSimpleQueueServiceOut() {
        this.setBounds(200, 200, 450, 500);
        this.setTitle("Amazon Simple Queue WS - Out");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.guiInit();
    }

    private void guiInit() {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.BOTH;
        cons.insets = new Insets(5, 5, 5, 5);
        cons.weightx = 100;
        JLabel label0 = new JLabel("Enter Queue Name");
        this.add(label0, cons, 0, 0, 1, 1);
        JLabel lable1 = new JLabel("Queue Code");
        this.add(lable1, cons, 1, 0, 1, 1);
        createQueue = new JTextField();
        createQueue.setEditable(false);
        this.add(createQueue, cons, 0, 1, 1, 1);
        queueCode = new JTextField();
        queueCode.setEditable(false);
        this.add(queueCode, cons, 1, 1, 1, 1);
        JLabel lable2 = new JLabel("Read");
        this.add(lable2, cons, 0, 2, 1, 1);
        read = new JTextField();
        read.setEditable(false);
        this.add(read, cons, 0, 3, 2, 1);
        JLabel label3 = new JLabel("Results");
        this.add(label3, cons, 0, 4, 1, 1);
        resuts = new JTextArea();
        resuts.setEditable(false);
        JScrollPane resultPane = new JScrollPane(resuts);
        cons.weighty = 100;
        this.add(resultPane, cons, 0, 5, 2, 1);
        JPanel buttonPannel = new JPanel();
        loadButton = new JButton("Load Queue");
        loadButton.setActionCommand("1");
        deleteButton = new JButton("Delete Queue");
        deleteButton.setActionCommand("2");
        buttonPannel.add(loadButton);
        buttonPannel.add(deleteButton);
        cons.weightx = 0;
        cons.weighty = 0;
        this.add(buttonPannel, cons, 0, 6, 2, 1);
        this.createQueue.addKeyListener(
                new ListenersOut(this.createQueue,
                        this.queueCode,
                        this.read,
                        this.resuts,
                        this.loadButton,
                        this.deleteButton));
        this.loadButton.addActionListener(
                new ListenersOut(this.createQueue,
                        this.queueCode,
                        this.read,
                        this.resuts,
                        this.loadButton,
                        this.deleteButton));
        this.resuts.addMouseMotionListener(
                new ListenersOut(this.createQueue,
                        this.queueCode,
                        this.read,
                        this.resuts,
                        this.loadButton,
                        this.deleteButton));
        this.deleteButton.addActionListener(
                new ListenersOut(this.createQueue,
                        this.queueCode,
                        this.read,
                        this.resuts,
                        this.loadButton,
                        this.deleteButton));

        AddMenuItems();
    }

    private void add(Component c,
                     GridBagConstraints cons,
                     int x,
                     int y,
                     int w,
                     int h) {
        cons.gridx = x;
        cons.gridy = y;
        cons.gridheight = h;
        cons.gridwidth = w;
        this.getContentPane().add(c, cons);
    }

    private void AddMenuItems() {
        //add the menus
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        JMenuItem amazonKeyMenu = new JMenuItem("Set Amazon Key",
                KeyEvent.VK_G);
        amazonKeyMenu.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        amazonKeyMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setKey();
            }
        });
        settingsMenu.add(amazonKeyMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem mnuItemHelp = new JMenuItem("Show Help");
        helpMenu.add(mnuItemHelp);

        mnuItemHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });

        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setKey() {
        String key = JOptionPane.showInputDialog(this,
                "Set the Amazon Key",
                QueueManager.getKey());
        if (key != null && key.trim().length() != 0) {
            QueueManager.setKey(key);
        }
    }

    /**
     * method showHelp
     */
    private void showHelp() {

        JFrame frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width / 5,
                screenSize.height / 5);
        frame.setSize(screenSize.width / 2, screenSize.height / 2);

        BorderLayout layout = new BorderLayout();

        JScrollPane jsp;
        JEditorPane jep;

        jep = new JEditorPane();
        //jep.addHyperlinkListener(new LinkFollower());
        jep.setEditable(false);
        jep.setContentType("text/html");

        jsp = new JScrollPane(jep);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(layout);
        contentPane.add(jsp, BorderLayout.CENTER);
        String helpDoc = System.getProperty("user.dir") + HELP_FILE_NAME;

        try {
            jep.setPage(new File(helpDoc).toURL());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Help file not detected",
                    "Help file error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.setVisible(true);
    }

}
