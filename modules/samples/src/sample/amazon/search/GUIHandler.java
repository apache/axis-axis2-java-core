/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package samples.amazon.search;

import org.apache.axis.engine.AxisFault;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;

/**
 * Build and desplay the GUI
 * implements both KeyListner and ActionListner
 * KeyListner is used to detect space or Enter key at textField
 * ActionListner is used for menu cammands
 * Thread is run to isolate the GUI from internal actions
 *
 * @auther Gayan Asanka  (gayan@opensource.lk)
 */
public class GUIHandler implements KeyListener, ActionListener, Runnable {

    /**
     * Results are desplayed here
     */
    private static JEditorPane text;

    /**
     * Query parameters typed here
     */
    private static JTextField textBox;

    /**
     * Menu command to set the key
     */
    private static JMenuItem mnuKey;

    /**
     * menu commant to set the maximum results per page
     */
    private static JMenuItem mnuMaxResults;

    /**
     * Build the GUI using awt and swing components
     */
    public void buildFrame() {
        JFrame frame;
        SpringLayout layout;
        JMenuBar menuBar;
        JMenu setMenu;
        Spring hSpring,wSpring,xSpring,ySpring;

        frame = new JFrame("Amazon Web Search");
        layout = new SpringLayout();
        Container pane = frame.getContentPane();
        pane.setLayout(layout);

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        setMenu = new JMenu("Set"); // Create Set menu
        menuBar.add(setMenu);
        setMenu.addActionListener(this);

        mnuKey = new JMenuItem("Key");
        mnuMaxResults = new JMenuItem("Results per Page");

        setMenu.add(mnuKey);
        setMenu.add(mnuMaxResults);

        mnuKey.addActionListener(this);
        mnuMaxResults.addActionListener(this);

        Toolkit theKit = frame.getToolkit(); // Get the window toolkit
        Dimension wndSize = theKit.getScreenSize(); // Get screen size
        // Set the position to screen center and appropriate size
        frame.setBounds(wndSize.width / 6, wndSize.height / 10, // Position
                wndSize.width * 3 / 5, wndSize.height * 3 / 4); // Size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        text = new JEditorPane();
        text.setEditable(false);
        text.setContentType("text/html");
        text.addHyperlinkListener(new LinkFollower());

        JScrollPane scroll = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.add(scroll);

        textBox = new JTextField();
        textBox.addKeyListener(this);
        pane.add(textBox);

        SpringLayout.Constraints textBoxConstraints = layout.getConstraints(textBox);
        xSpring = Spring.constant(0); // Spring we値l use for X
        ySpring = Spring.constant(0); // Spring we値l use for Y
        wSpring = Spring.constant(frame.getBounds().width); // Spring we値l use for width
        hSpring = Spring.constant(30); // Strut we値l use for height
        textBoxConstraints.setWidth(wSpring); // Set component width constraint
        textBoxConstraints.setHeight(hSpring);
        textBoxConstraints.setX(xSpring); // Set the WEST edge constraint
        textBoxConstraints.setY(ySpring);

        SpringLayout.Constraints scrollConstraints = layout.getConstraints(scroll);
        xSpring = Spring.constant(0); // Spring we値l use for X
        ySpring = Spring.constant(30); // Spring we値l use for Y
        wSpring = Spring.constant(frame.getBounds().width); // Spring we値l use for width
        hSpring = Spring.constant(500); // Strut we値l use for height
        scrollConstraints.setWidth(wSpring); // Set component width constraint
        scrollConstraints.setHeight(hSpring);
        scrollConstraints.setX(xSpring); // Set the WEST edge constraint
        scrollConstraints.setY(ySpring); // Set the NORTH edge constraint

        frame.setVisible(true);
    }

    /**
     * method showResults
     * desplay results by ClientCallbackHandler
     *
     * @param results
     */
    protected static void showResults(String results) {
        text.setText(results);
    }

    /**
     * method setKey
     * Get the key from user via an inputDialog and
     * store it in the properties file
     */
    protected void setKey() {
        AsynchronousClient.amazonkey = JOptionPane.showInputDialog(null, "Enter the license Key");
        if (AsynchronousClient.amazonkey == null) {
            setKey();
        }
        OutputStream propOut;
        try {
            String workingDir = System.getProperty("user.dir");
            File propertyFile = new File(workingDir + File.separator + "samples" + File.separator +
                    "/key.properties");
            propOut = new FileOutputStream(propertyFile);

            AsynchronousClient.prop.setProperty("amazonKey", AsynchronousClient.amazonkey);
            AsynchronousClient.prop.store(propOut, "License Key");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method keyTyped
     * fires when user typing in TextField textBox
     * act when detects space and Enter key only
     *
     * @param e
     */
    public void keyTyped(KeyEvent e) {
        System.out.println("inside");
        int event = e.getKeyChar();

        if (event == KeyEvent.VK_SPACE || event == KeyEvent.VK_ENTER) {
            AsynchronousClient.search = textBox.getText().trim();
            AsynchronousClient.search.trim();
            System.out.println(textBox.getText());
            if (!AsynchronousClient.prevSearch.equals(AsynchronousClient.search)) {
                AsynchronousClient.doSearch = true;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent s) {
    }

    /**
     * method actionPerformed
     * Detects menu click events
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mnuMaxResults) {
            do {
                System.out.println("come to the place");
                AsynchronousClient.maxResults =
                        JOptionPane.showInputDialog(null,
                          "Enter the number of maximum results per page (Maximum allowed is 1000)");
                //JOptionPane.get

            } while (Integer.parseInt(AsynchronousClient.maxResults) > 1000 ||
                    Integer.parseInt(AsynchronousClient.maxResults) < 0);
        }
        if (e.getSource() == mnuKey) {
            setKey();
        }
    }

    /**
     * method run
     * check the flag doSearch
     * if it's set, call sendMsg method
     */
    public void run() {
        while (true) {
            AsynchronousClient.search.toString().trim();
            if (AsynchronousClient.doSearch == true) {

                try {
                    AsynchronousClient.doSearch = false;
                    AsynchronousClient.sendMsg();

                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}