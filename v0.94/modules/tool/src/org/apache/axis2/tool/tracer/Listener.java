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

package org.apache.axis2.tool.tracer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 * this is one of the tabbed panels that acts as the actual proxy
 */
class Listener extends JPanel {

	/**
     * Field inputSocket
     */
    public Socket inputSocket = null;

    /**
     * Field outputSocket
     */
    public Socket outputSocket = null;

    /**
     * Field portField
     */
    public JTextField portField = null;

    /**
     * Field hostField
     */
    public JTextField hostField = null;

    /**
     * Field tPortField
     */
    public JTextField tPortField = null;

    /**
     * Field isProxyBox
     */
    public JCheckBox isProxyBox = null;

    /**
     * Field stopButton
     */
    public JButton stopButton = null;

    /**
     * Field removeButton
     */
    public JButton removeButton = null;

    /**
     * Field removeAllButton
     */
    public JButton removeAllButton = null;

    /**
     * Field xmlFormatBox
     */
    public JCheckBox xmlFormatBox = null;

    /**
     * Field saveButton
     */
    public JButton saveButton = null;

    /**
     * Field resendButton
     */
    public JButton resendButton = null;

    /**
     * Field switchButton
     */
    public JButton switchButton = null;

    /**
     * Field closeButton
     */
    public JButton closeButton = null;

    /**
     * Field connectionTable
     */
    public JTable connectionTable = null;

    /**
     * Field tableModel
     */
    public DefaultTableModel tableModel = null;

    /**
     * Field outPane
     */
    public JSplitPane outPane = null;

    /**
     * Field sSocket
     */
    public ServerSocket sSocket = null;

    /**
     * Field sw
     */
    public SocketWaiter sw = null;

    /**
     * Field leftPanel
     */
    public JPanel leftPanel = null;

    /**
     * Field rightPanel
     */
    public JPanel rightPanel = null;

    /**
     * Field notebook
     */
    public JTabbedPane notebook = null;

    /**
     * Field HTTPProxyHost
     */
    public String HTTPProxyHost = null;

    /**
     * Field HTTPProxyPort
     */
    public int HTTPProxyPort = 80;

    /**
     * Field delayBytes
     */
    public int delayBytes = 0;

    /**
     * Field delayTime
     */
    public int delayTime = 0;

    /**
     * Field slowLink
     */
    public SlowLinkSimulator slowLink;

    /**
     * Field connections
     */
    public final Vector connections = new Vector();

    /**
     * create a listener
     *
     * @param _notebook
     * @param name
     * @param listenPort
     * @param host
     * @param targetPort
     * @param isProxy
     * @param slowLink   optional reference to a slow connection
     */
    public Listener(JTabbedPane _notebook, String name, int listenPort,
                    String host, int targetPort, boolean isProxy,
                    SlowLinkSimulator slowLink) {
        notebook = _notebook;
        if (name == null) {
            name = HTTPTracer.getMessage("port01", "Port") + " " + listenPort;
        }

        // set the slow link to the passed down link
        if (slowLink != null) {
            this.slowLink = slowLink;
        } else {

            // or make up a no-op one.
            this.slowLink = new SlowLinkSimulator(0, 0);
        }
        this.setLayout(new BorderLayout());

        // 1st component is just a row of labels and 1-line entry fields
        // ///////////////////////////////////////////////////////////////////
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final String start = HTTPTracer.getMessage("start00", "Start");
        top.add(stopButton = new JButton(start));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(new JLabel("  "
                + HTTPTracer.getMessage("listenPort01", "Listen Port:")
                + " ", SwingConstants.RIGHT));
        top.add(portField = new JTextField("" + listenPort, 4));
        top.add(new JLabel("  " + HTTPTracer.getMessage("host00", "Host:"),
                SwingConstants.RIGHT));
        top.add(hostField = new JTextField(host, 30));
        top.add(new JLabel("  " + HTTPTracer.getMessage("port02", "Port:") + " ",
                SwingConstants.RIGHT));
        top.add(tPortField = new JTextField("" + targetPort, 4));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(isProxyBox = new JCheckBox(HTTPTracer.getMessage("proxy00", "Proxy")));
        isProxyBox.addChangeListener(new BasicButtonListener(isProxyBox) {
            public void stateChanged(ChangeEvent event) {
                JCheckBox box = (JCheckBox) event.getSource();
                boolean state = box.isSelected();
                tPortField.setEnabled(!state);
                hostField.setEnabled(!state);
            }
        });
        isProxyBox.setSelected(isProxy);
        portField.setEditable(false);
        portField.setMaximumSize(new Dimension(50, Short.MAX_VALUE));
        hostField.setEditable(false);
        hostField.setMaximumSize(new Dimension(85, Short.MAX_VALUE));
        tPortField.setEditable(false);
        tPortField.setMaximumSize(new Dimension(50, Short.MAX_VALUE));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (HTTPTracer.getMessage("stop00",
                        "Stop").equals(event.getActionCommand())) {
                    stop();
                }
                if (start.equals(event.getActionCommand())) {
                    start();
                }
            }
        });
        this.add(top, BorderLayout.NORTH);

        // 2nd component is a split pane with a table on the top
        // and the request/response text areas on the bottom
        // ///////////////////////////////////////////////////////////////////
        tableModel = new DefaultTableModel(new String[]{
            HTTPTracer.getMessage("state00", "State"),
            HTTPTracer.getMessage("time00", "Time"),
            HTTPTracer.getMessage("requestHost00", "Request Host"),
            HTTPTracer.getMessage("targetHost", "Target Host"),
            HTTPTracer.getMessage("request00", "Request..."),
            HTTPTracer.getMessage("elapsed00", "Elapsed Time")}, 0);
        tableModel.addRow(new Object[]{"---",
                                       HTTPTracer.getMessage("mostRecent00",
                                               "Most Recent"),
                                       "---", "---", "---", "---"});
        connectionTable = new JTable(1, 2);
        connectionTable.setModel(tableModel);
        connectionTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Reduce the STATE column and increase the REQ column
        TableColumn col;
        col = connectionTable.getColumnModel().getColumn(HTTPTracer.STATE_COLUMN);
        col.setMaxWidth(col.getPreferredWidth() / 2);
        col = connectionTable.getColumnModel().getColumn(HTTPTracer.REQ_COLUMN);
        col.setPreferredWidth(col.getPreferredWidth() * 2);
        ListSelectionModel sel = connectionTable.getSelectionModel();
        sel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel m =
                        (ListSelectionModel) event.getSource();
                int divLoc = outPane.getDividerLocation();
                if (m.isSelectionEmpty()) {
                    setLeft(
                            new JLabel(
                                    " "
                            +
                            HTTPTracer.getMessage("wait00",
                                    "Waiting for Connection...")));
                    setRight(new JLabel(""));
                    removeButton.setEnabled(false);
                    removeAllButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    resendButton.setEnabled(false);
                } else {
                    int row = m.getLeadSelectionIndex();
                    if (row == 0) {
                        if (connections.size() == 0) {
                            setLeft(
                                    new JLabel(
                                            " "
                                    +
                                    HTTPTracer.getMessage("wait00",
                                            "Waiting for connection...")));
                            setRight(new JLabel(""));
                            removeButton.setEnabled(false);
                            removeAllButton.setEnabled(false);
                            saveButton.setEnabled(false);
                            resendButton.setEnabled(false);
                        } else {
                            Connection conn =
                                    (Connection) connections.lastElement();
                            setLeft(conn.inputScroll);
                            setRight(conn.outputScroll);
                            removeButton.setEnabled(false);
                            removeAllButton.setEnabled(true);
                            saveButton.setEnabled(true);
                            resendButton.setEnabled(true);
                        }
                    } else {
                        Connection conn = (Connection) connections.get(row
                                - 1);
                        setLeft(conn.inputScroll);
                        setRight(conn.outputScroll);
                        removeButton.setEnabled(true);
                        removeAllButton.setEnabled(true);
                        saveButton.setEnabled(true);
                        resendButton.setEnabled(true);
                    }
                }
                outPane.setDividerLocation(divLoc);
            }
        });
        JPanel tablePane = new JPanel();
        tablePane.setLayout(new BorderLayout());
        JScrollPane tableScrollPane = new JScrollPane(connectionTable);
        tablePane.add(tableScrollPane, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final String removeSelected = HTTPTracer.getMessage("removeSelected00",
                "Remove Selected");
        buttons.add(removeButton = new JButton(removeSelected));
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        final String removeAll = HTTPTracer.getMessage("removeAll00", "Remove All");
        buttons.add(removeAllButton = new JButton(removeAll));
        tablePane.add(buttons, BorderLayout.SOUTH);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (removeSelected.equals(event.getActionCommand())) {
                    remove();
                }
            }
        });
        removeAllButton.setEnabled(false);
        removeAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (removeAll.equals(event.getActionCommand())) {
                    removeAll();
                }
            }
        });

        // Add Response Section
        // ///////////////////////////////////////////////////////////////////
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BorderLayout());
        leftPanel = new JPanel();
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(new JLabel("  "
                + HTTPTracer.getMessage("request01", "Request")));
        leftPanel.add(new JLabel(" "
                + HTTPTracer.getMessage("wait01",
                        "Waiting for connection")));
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JLabel("  "
                + HTTPTracer.getMessage("response00", "Response")));
        rightPanel.add(new JLabel(""));
        outPane = new JSplitPane(0, leftPanel, rightPanel);
        outPane.setDividerSize(4);
        pane2.add(outPane, BorderLayout.CENTER);
        JPanel bottomButtons = new JPanel();
        bottomButtons.setLayout(new BoxLayout(bottomButtons,
                BoxLayout.X_AXIS));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                5));
        bottomButtons.add(
                xmlFormatBox =
                new JCheckBox(HTTPTracer.getMessage("xmlFormat00", "XML Format")));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        final String save = HTTPTracer.getMessage("save00", "Save");
        bottomButtons.add(saveButton = new JButton(save));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        final String resend = HTTPTracer.getMessage("resend00", "Resend");
        bottomButtons.add(resendButton = new JButton(resend));
        bottomButtons.add(Box.createRigidArea(new Dimension(5, 0)));
        final String switchStr = HTTPTracer.getMessage("switch00", "Switch Layout");
        bottomButtons.add(switchButton = new JButton(switchStr));
        bottomButtons.add(Box.createHorizontalGlue());
        final String close = HTTPTracer.getMessage("close00", "Close");
        bottomButtons.add(closeButton = new JButton(close));
        pane2.add(bottomButtons, BorderLayout.SOUTH);
        saveButton.setEnabled(false);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (save.equals(event.getActionCommand())) {
                    save();
                }
            }
        });
        resendButton.setEnabled(false);
        resendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (resend.equals(event.getActionCommand())) {
                    resend();
                }
            }
        });
        switchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (switchStr.equals(event.getActionCommand())) {
                    int v = outPane.getOrientation();
                    if (v == 0) {

                        // top/bottom
                        outPane.setOrientation(1);
                    } else {

                        // left/right
                        outPane.setOrientation(0);
                    }
                    outPane.setDividerLocation(0.5);
                }
            }
        });
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (close.equals(event.getActionCommand())) {
                    close();
                }
            }
        });
        JSplitPane pane1 = new JSplitPane(0);
        pane1.setDividerSize(4);
        pane1.setTopComponent(tablePane);
        pane1.setBottomComponent(pane2);
        pane1.setDividerLocation(150);
        this.add(pane1, BorderLayout.CENTER);

        // 
        // //////////////////////////////////////////////////////////////////
        sel.setSelectionInterval(0, 0);
        outPane.setDividerLocation(150);
        notebook.addTab(name, this);
        start();
    }

    /**
     * Method setLeft
     *
     * @param left
     */
    public void setLeft(Component left) {
        leftPanel.removeAll();
        leftPanel.add(left);
    }

    /**
     * Method setRight
     *
     * @param right
     */
    public void setRight(Component right) {
        rightPanel.removeAll();
        rightPanel.add(right);
    }

    /**
     * Method start
     */
    public void start() {
        int port = Integer.parseInt(portField.getText());
        portField.setText("" + port);
        int i = notebook.indexOfComponent(this);
        notebook.setTitleAt(i, HTTPTracer.getMessage("port01", "Port") + " " + port);
        int tmp = Integer.parseInt(tPortField.getText());
        tPortField.setText("" + tmp);
        sw = new SocketWaiter(this, port);
        stopButton.setText(HTTPTracer.getMessage("stop00", "Stop"));
        portField.setEditable(false);
        hostField.setEditable(false);
        tPortField.setEditable(false);
        isProxyBox.setEnabled(false);
    }

    /**
     * Method close
     */
    public void close() {
        stop();
        notebook.remove(this);
    }

    /**
     * Method stop
     */
    public void stop() {
        try {
            for (int i = 0; i < connections.size(); i++) {
                Connection conn = (Connection) connections.get(i);
                conn.halt();
            }
            sw.halt();
            stopButton.setText(HTTPTracer.getMessage("start00", "Start"));
            portField.setEditable(true);
            hostField.setEditable(true);
            tPortField.setEditable(true);
            isProxyBox.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method remove
     */
    public void remove() {
        ListSelectionModel lsm = connectionTable.getSelectionModel();
        int bot = lsm.getMinSelectionIndex();
        int top = lsm.getMaxSelectionIndex();
        for (int i = top; i >= bot; i--) {
            ((Connection) connections.get(i - 1)).remove();
        }
        if (bot > connections.size()) {
            bot = connections.size();
        }
        lsm.setSelectionInterval(bot, bot);
    }

    /**
     * Method removeAll
     */
    public void removeAll() {
        ListSelectionModel lsm = connectionTable.getSelectionModel();
        lsm.clearSelection();
        while (connections.size() > 0) {
            ((Connection) connections.get(0)).remove();
        }
        lsm.setSelectionInterval(0, 0);
    }

    /**
     * Method save
     */
    public void save() {
        JFileChooser dialog = new JFileChooser(".");
        int rc = dialog.showSaveDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            try {
                File file = dialog.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                ListSelectionModel lsm =
                        connectionTable.getSelectionModel();
                rc = lsm.getLeadSelectionIndex();
                int n = 0;
                for (Iterator i = connections.iterator(); i.hasNext();
                     n++) {
                    Connection conn = (Connection) i.next();
                    if (lsm.isSelectedIndex(n + 1)
                            || (!(i.hasNext())
                            && (lsm.getLeadSelectionIndex() == 0))) {
                        rc = Integer.parseInt(portField.getText());
                        out.write("\n==============\n".getBytes());
                        out.write(((HTTPTracer.getMessage("listenPort01",
                                "Listen Port:")
                                + " " + rc + "\n")).getBytes());
                        out.write((HTTPTracer.getMessage("targetHost01",
                                "Target Host:")
                                + " " + hostField.getText()
                                + "\n").getBytes());
                        rc = Integer.parseInt(tPortField.getText());
                        out.write(((HTTPTracer.getMessage("targetPort01",
                                "Target Port:")
                                + " " + rc + "\n")).getBytes());
                        out.write((("==== "
                                + HTTPTracer.getMessage("request01", "Request")
                                + " ====\n")).getBytes());
                        out.write(conn.inputText.getText().getBytes());
                        out.write((("==== "
                                + HTTPTracer.getMessage("response00", "Response")
                                + " ====\n")).getBytes());
                        out.write(conn.outputText.getText().getBytes());
                        out.write("\n==============\n".getBytes());
                    }
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method resend
     */
    public void resend() {
        int rc;
        try {
            ListSelectionModel lsm = connectionTable.getSelectionModel();
            rc = lsm.getLeadSelectionIndex();
            if (rc == 0) {
                rc = connections.size();
            }
            Connection conn = (Connection) connections.get(rc - 1);
            if (rc > 0) {
                lsm.clearSelection();
                lsm.setSelectionInterval(0, 0);
            }
            InputStream in = null;
            String text = conn.inputText.getText();

            // Fix Content-Length HTTP headers
            if (text.startsWith("POST ") || text.startsWith("GET ")) {

                // System.err.println("IN CL" );
                int pos1, pos2, pos3;
                String headers;
                pos3 = text.indexOf("\n\n");
                if (pos3 == -1) {
                    pos3 = text.indexOf("\r\n\r\n");
                    if (pos3 != -1) {
                        pos3 = pos3 + 4;
                    }
                } else {
                    pos3 += 2;
                }
                headers = text.substring(0, pos3);
                pos1 = headers.indexOf("Content-Length:");

                // System.err.println("pos1: " + pos1 );
                // System.err.println("pos3: " + pos3 );
                if (pos1 != -1) {
                    int newLen = text.length() - pos3;
                    pos2 = headers.indexOf("\n", pos1);
                    System.err.println("CL: " + newLen);
                    System.err.println("Hdrs: '" + headers + "'");
                    System.err.println("subTEXT: '"
                            + text.substring(pos3, pos3 + newLen)
                            + "'");
                    text = headers.substring(0, pos1) + "Content-Length: "
                            + newLen + "\n" + headers.substring(pos2 + 1)
                            + text.substring(pos3);
                    System.err.println("\nTEXT: '" + text + "'");
                }
            }
            in = new ByteArrayInputStream(text.getBytes());
            new Connection(this, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
