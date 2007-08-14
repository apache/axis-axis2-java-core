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

package sample.google.search;

import org.apache.axis2.AxisFault;
import sample.google.common.util.PropertyLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

/**
 * Build and desplay the GUI
 * implements both KeyListner and ActionListner
 * KeyListner is used to detect space or Enter key at textField
 * ActionListner is used for menu cammands
 * Thread is run to isolate the GUI from internal actions
 */
public class GUIHandler {
    private static final String HELP_FILE_NAME = "/docs/GoogleSearchHelp.html";
    /**
     * Results are desplayed here
     */
    private JEditorPane textEditorPane;

    /**
     * Query parameters typed here
     */
    private JTextField textBox;

    /**
     * Buttons clicked to view more results and backButton
     */
    private JButton nextButton
    ,
    backButton;

    /**
     * Menu commands to set the key and maximum no of results per page
     */
    private JMenuItem keyMenuItem
    ,
    maxResultsMenuItem;
    private AsynchronousClient asyncClient;

    public GUIHandler(AsynchronousClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Build the GUI using awt and swing components
     */


    public void buildFrame() {
        JFrame frame;
        SpringLayout layout;
        JMenuBar menuBar;
        JMenu settingsMenu;
        Spring xSpring, ySpring, hSpring, wSpring;

        frame = new JFrame("Google Search");
        frame.setResizable(false);
        layout = new SpringLayout();
        Container pane = frame.getContentPane();
        pane.setLayout(layout);

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        settingsMenu = new JMenu("Settings"); // Create Set menu
        menuBar.add(settingsMenu);

        keyMenuItem = new JMenuItem("Key");
        keyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setKey();
            }
        });
        settingsMenu.add(keyMenuItem);

        maxResultsMenuItem = new JMenuItem("Result per page");
        maxResultsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMaxResults();
            }
        });
        settingsMenu.add(maxResultsMenuItem);
        maxResultsMenuItem.setEnabled(true);
        maxResultsMenuItem.setToolTipText(
                "You can set the maximum number of results per page, Maximum allowable is 10");

        JMenu helpMenu = new JMenu("Help");
        JMenuItem mnuItemHelp = new JMenuItem("Show Help");
        helpMenu.add(mnuItemHelp);
        mnuItemHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
        menuBar.add(helpMenu);

        Toolkit theKit = frame.getToolkit(); // Get the window toolkit
        Dimension wndSize = theKit.getScreenSize(); // Get screen size
        // Set the position to screen center & size to half screen size
        frame.setBounds(wndSize.width / 6, wndSize.height / 10, // Position
                wndSize.width * 3 / 5, wndSize.height * 3 / 4); // Size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        nextButton = new JButton("More Results");
	nextButton.setEnabled(false);
        pane.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processNextButton();
            }
        });

        backButton = new JButton("Previous Page");
        backButton.setVisible(false);
        pane.add(backButton);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processBackButton();
            }
        });

        textEditorPane = new JEditorPane();
        textEditorPane.setEditable(false);
        textEditorPane.setContentType("text/html");
        textEditorPane.addHyperlinkListener(new LinkFollower());
        JScrollPane scroll = new JScrollPane(textEditorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.add(scroll);


        textBox = new JTextField();
        textBox.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
		 if (e.getKeyCode() == 10) {
                     setButtons();
                 }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
                processKeyEvent(e.getKeyChar());
            }
        });

        pane.add(textBox);

        SpringLayout.Constraints textBoxConstraints = layout.getConstraints(
                textBox);
        xSpring = Spring.constant(0); // Spring we'll use for X
        ySpring = Spring.constant(0); // Spring we'll use for Y
        wSpring = Spring.constant(frame.getBounds().width - 8); // Spring we'll use for width
        hSpring = Spring.constant(30); // Strut we?ll use for height
        textBoxConstraints.setWidth(wSpring); // Set component width constraint
        textBoxConstraints.setHeight(hSpring);
        textBoxConstraints.setX(xSpring); // Set the WEST edge constraint
        textBoxConstraints.setY(ySpring);

        SpringLayout.Constraints scrollConstraints = layout.getConstraints(
                scroll);
        xSpring = Spring.constant(0); // Spring we'll use for X
        ySpring = Spring.constant(30); // Spring we'll use for Y
        wSpring = Spring.constant(frame.getBounds().width - 8); // Spring we'll use for width
        hSpring = Spring.constant(450); // Strut we'll use for height
        scrollConstraints.setWidth(wSpring); // Set component width constraint
        scrollConstraints.setHeight(hSpring);
        scrollConstraints.setX(xSpring); // Set the WEST edge constraint
        scrollConstraints.setY(ySpring); // Set the NORTH edge constraint

        SpringLayout.Constraints backBtnConstraints = layout.getConstraints(
                backButton);
        xSpring = Spring.constant(50); // Spring we'll use for X
        ySpring = Spring.constant(480); // Spring we'll use for Y
        wSpring = Spring.constant(200); // Spring we'll use for width
        hSpring = Spring.constant(30); // Strut we'll use for height
        backBtnConstraints.setWidth(wSpring); // Set component width constraint
        backBtnConstraints.setHeight(hSpring);
        backBtnConstraints.setX(xSpring); // Set the WEST edge constraint
        backBtnConstraints.setY(ySpring);

        SpringLayout.Constraints nextBtnConstraints = layout.getConstraints(
                nextButton);
        xSpring = Spring.constant(250); // Spring we?ll use for X
        ySpring = Spring.constant(480); // Spring we?ll use for Y
        wSpring = Spring.constant(200); // Spring we?ll use for width
        hSpring = Spring.constant(30); // Strut we?ll use for height
        nextBtnConstraints.setWidth(wSpring); // Set component width constraint
        nextBtnConstraints.setHeight(hSpring);
        nextBtnConstraints.setX(xSpring); // Set the WEST edge constraint
        nextBtnConstraints.setY(ySpring);
        frame.setVisible(true);
    }

    /**
     * method showResults
     * desplay results by ClientCallbackHandler
     *
     * @param results
     */
    protected void showResults(String results) {
        textEditorPane.setText(results);
    }

    /**
     * method setKey
     * Get the key from user via an inputDialog and
     * store it in the properties file
     */
    protected void setKey() {
        String key = JOptionPane.showInputDialog(null,
                "Set the Google Key",
                PropertyLoader.getGoogleKey());
        if (key != null && key.trim().length() != 0) {
            PropertyLoader.setGoogleKey(key);
        }
    }

    private void processBackButton() {
        if (asyncClient.getStartIndex() != 0) {
            int i = Integer.parseInt(asyncClient.getMaxResults());
            asyncClient.setStartIndex(asyncClient.getStartIndex() - i);
            if (asyncClient.getStartIndex() == 0) {
                backButton.setVisible(false);
            }
            doSearch();
        }
    }

    private void setButtons() {
        nextButton.setEnabled(true);
        backButton.setVisible(false);
    }


    /**
     * method keyTyped
     * fires when user typing in TextField textBox
     * act when detects space and Enter key only
     *
     * @param event
     */
    private void processKeyEvent(int event) {
        if (event == KeyEvent.VK_SPACE || event == KeyEvent.VK_ENTER) {
            asyncClient.setSearch(textBox.getText().trim());
            if (!asyncClient.getPrevSearch().equals(asyncClient.getSearch())) {
                doSearch();
            }
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
        jep.addHyperlinkListener(new LinkFollower());
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
            JOptionPane.showMessageDialog(null,
                    "Help file not detected",
                    "Help file error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.setVisible(true);
    }


    private void processNextButton() {
        int i = Integer.parseInt(asyncClient.getMaxResults());
        asyncClient.setStartIndex(asyncClient.getStartIndex() + i);
        backButton.setVisible(true);
        doSearch();
    }

    private void setMaxResults() {
        String maxResults =
                JOptionPane.showInputDialog(null,
                        "Enter the number of maximum results per page (Maximum allowed is 10)",
                        asyncClient.getMaxResults());
        if (maxResults != null) {
            try {
                asyncClient.setMaxResults(Integer.toString(Integer.parseInt(maxResults)));
                asyncClient.setPrevSearch("");
            } catch (NumberFormatException e) {
                return;
            }
        }
    }


    private void doSearch() {
        new ClientThread().run();

    }

    private class ClientThread implements Runnable {
        /**
         * method run
         * check the flag doSearch
         * if it's set, call sendMsg method
         */
        public void run() {
            if (!asyncClient.getSearch().equals(asyncClient.getPrevSearch())) {
                asyncClient.setStartIndex(0);
                // return;
            }
            try {
                asyncClient.sendMsg();
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }
        }
    }
}
