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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * a connection listens to a single current connection
 */
class Connection extends Thread {

	/**
     * Field listener
     */
    Listener listener;

    /**
     * Field active
     */
    boolean active;

    /**
     * Field fromHost
     */
    String fromHost;

    /**
     * Field time
     */
    String time;

    /**
     * Field elapsed time
     */
    long elapsedTime;
    
    /**
     * Field inputText
     */
    JTextArea inputText = null;

    /**
     * Field inputScroll
     */
    JScrollPane inputScroll = null;

    /**
     * Field outputText
     */
    JTextArea outputText = null;

    /**
     * Field outputScroll
     */
    JScrollPane outputScroll = null;

    /**
     * Field inSocket
     */
    Socket inSocket = null;

    /**
     * Field outSocket
     */
    Socket outSocket = null;

    /**
     * Field clientThread
     */
    Thread clientThread = null;

    /**
     * Field serverThread
     */
    Thread serverThread = null;

    /**
     * Field rr1
     */
    SocketRR rr1 = null;

    /**
     * Field rr2
     */
    SocketRR rr2 = null;

    /**
     * Field inputStream
     */
    InputStream inputStream = null;

    /**
     * Field HTTPProxyHost
     */
    String HTTPProxyHost = null;

    /**
     * Field HTTPProxyPort
     */
    int HTTPProxyPort = 80;

    /**
     * Field slowLink
     */
    private SlowLinkSimulator slowLink;

    /**
     * Constructor Connection
     *
     * @param l
     */
    public Connection(Listener l) {
        listener = l;
        HTTPProxyHost = l.HTTPProxyHost;
        HTTPProxyPort = l.HTTPProxyPort;
        slowLink = l.slowLink;
    }

    /**
     * Constructor Connection
     *
     * @param l
     * @param s
     */
    public Connection(Listener l, Socket s) {
        this(l);
        inSocket = s;
        start();
    }

    /**
     * Constructor Connection
     *
     * @param l
     * @param in
     */
    public Connection(Listener l, InputStream in) {
        this(l);
        inputStream = in;
        start();
    }

    /**
     * Method run
     */
    public void run() {
        try {
            active = true;
            HTTPProxyHost = System.getProperty("http.proxyHost");
            if ((HTTPProxyHost != null) && HTTPProxyHost.equals("")) {
                HTTPProxyHost = null;
            }
            if (HTTPProxyHost != null) {
                String tmp = System.getProperty("http.proxyPort");
                if ((tmp != null) && tmp.equals("")) {
                    tmp = null;
                }
                if (tmp == null) {
                    HTTPProxyPort = 80;
                } else {
                    HTTPProxyPort = Integer.parseInt(tmp);
                }
            }
            if (inSocket != null) {
                fromHost = (inSocket.getInetAddress()).getHostName();
            } else {
                fromHost = "resend";
            }
            String dateformat = HTTPTracer.getMessage("dateformat00", "yyyy-MM-dd HH:mm:ss");
            DateFormat df = new SimpleDateFormat(dateformat);
            time = df.format(new Date());
            int count = listener.connections.size();
            listener.tableModel.insertRow(count + 1,
                    new Object[]{
                        HTTPTracer.getMessage("active00","Active"),
                        time,
                        fromHost,
                        listener.hostField.getText(),
                        ""});
            listener.connections.add(this);
            inputText = new JTextArea(null, null, 20, 80);
            inputScroll = new JScrollPane(inputText);
            outputText = new JTextArea(null, null, 20, 80);
            outputScroll = new JScrollPane(outputText);
            ListSelectionModel lsm = listener.connectionTable.getSelectionModel();
            if ((count == 0) || (lsm.getLeadSelectionIndex() == 0)) {
                listener.outPane.setVisible(false);
                int divLoc = listener.outPane.getDividerLocation();
                listener.setLeft(inputScroll);
                listener.setRight(outputScroll);
                listener.removeButton.setEnabled(false);
                listener.removeAllButton.setEnabled(true);
                listener.saveButton.setEnabled(true);
                listener.resendButton.setEnabled(true);
                listener.outPane.setDividerLocation(divLoc);
                listener.outPane.setVisible(true);
            }
            String targetHost = listener.hostField.getText();
            int targetPort = Integer.parseInt(listener.tPortField.getText());
            int listenPort = Integer.parseInt(listener.portField.getText());
            InputStream tmpIn1 = inputStream;
            OutputStream tmpOut1 = null;
            InputStream tmpIn2 = null;
            OutputStream tmpOut2 = null;
            if (tmpIn1 == null) {
                tmpIn1 = inSocket.getInputStream();
            }
            if (inSocket != null) {
                tmpOut1 = inSocket.getOutputStream();
            }
            String bufferedData = null;
            StringBuffer buf = null;
            int index = listener.connections.indexOf(this);
            if (listener.isProxyBox.isSelected() || (HTTPProxyHost != null)) {

                // Check if we're a proxy
                byte[] b = new byte[1];
                buf = new StringBuffer();
                String s;
                for (; ;) {
                    int len;
                    len = tmpIn1.read(b, 0, 1);
                    if (len == -1) {
                        break;
                    }
                    s = new String(b);
                    buf.append(s);
                    if (b[0] != '\n') {
                        continue;
                    }
                    break;
                }
                bufferedData = buf.toString();
                inputText.append(bufferedData);
                if (bufferedData.startsWith("GET ")
                        || bufferedData.startsWith("POST ")
                        || bufferedData.startsWith("PUT ")
                        || bufferedData.startsWith("DELETE ")) {
                    int start, end;
                    URL url;
                    start = bufferedData.indexOf(' ') + 1;
                    while (bufferedData.charAt(start) == ' ') {
                        start++;
                    }
                    end = bufferedData.indexOf(' ', start);
                    String urlString = bufferedData.substring(start, end);
                    if (urlString.charAt(0) == '/') {
                        urlString = urlString.substring(1);
                    }
                    if (listener.isProxyBox.isSelected()) {
                        url = new URL(urlString);
                        targetHost = url.getHost();
                        targetPort = url.getPort();
                        if (targetPort == -1) {
                            targetPort = 80;
                        }
                        listener.tableModel.setValueAt(targetHost,
                                index + 1,
                                HTTPTracer.OUTHOST_COLUMN);
                        bufferedData = bufferedData.substring(0, start)
                                + url.getFile()
                                + bufferedData.substring(end);
                    } else {
                        url = new URL("http://" + targetHost + ":"
                                + targetPort + "/" + urlString);
                        listener.tableModel.setValueAt(targetHost,
                                index + 1,
                                HTTPTracer.OUTHOST_COLUMN);
                        bufferedData = bufferedData.substring(0, start)
                                + url.toExternalForm()
                                + bufferedData.substring(end);
                        targetHost = HTTPProxyHost;
                        targetPort = HTTPProxyPort;
                    }
                }
            } else {

                // 
                // Change Host: header to point to correct host
                // 
                byte[] b1 = new byte[1];
                buf = new StringBuffer();
                String s1;
                String lastLine = null;
                for (; ;) {
                    int len;
                    len = tmpIn1.read(b1, 0, 1);
                    if (len == -1) {
                        break;
                    }
                    s1 = new String(b1);
                    buf.append(s1);
                    if (b1[0] != '\n') {
                        continue;
                    }

                    // we have a complete line
                    String line = buf.toString();
                    buf.setLength(0);

                    // check to see if we have found Host: header
                    if (line.startsWith("Host: ")) {

                        // we need to update the hostname to target host
                        String newHost = "Host: " + targetHost + ":"
                                + listenPort + "\r\n";
                        bufferedData = bufferedData.concat(newHost);
                        break;
                    }

                    // add it to our headers so far
                    if (bufferedData == null) {
                        bufferedData = line;
                    } else {
                        bufferedData = bufferedData.concat(line);
                    }

                    // failsafe
                    if (line.equals("\r\n")) {
                        break;
                    }
                    if ("\n".equals(lastLine) && line.equals("\n")) {
                        break;
                    }
                    lastLine = line;
                }
                if (bufferedData != null) {
                    inputText.append(bufferedData);
                    int idx = (bufferedData.length() < 50)
                            ? bufferedData.length()
                            : 50;
                    s1 = bufferedData.substring(0, idx);
                    int i = s1.indexOf('\n');
                    if (i > 0) {
                        s1 = s1.substring(0, i - 1);
                    }
                    s1 = s1 + "                           "
                            + "                       ";
                    s1 = s1.substring(0, 51);
                    listener.tableModel.setValueAt(s1, index + 1,
                    		HTTPTracer.REQ_COLUMN);
                }
            }
            if (targetPort == -1) {
                targetPort = 80;
            }
            outSocket = new Socket(targetHost, targetPort);
            tmpIn2 = outSocket.getInputStream();
            tmpOut2 = outSocket.getOutputStream();
            if (bufferedData != null) {
                byte[] b = bufferedData.getBytes();
                tmpOut2.write(b);
                slowLink.pump(b.length);
            }
            boolean format = listener.xmlFormatBox.isSelected();

            // this is the channel to the endpoint
            rr1 = new SocketRR(this, inSocket, tmpIn1, outSocket, tmpOut2,
                    inputText, format, listener.tableModel,
                    index + 1, "request:", slowLink);

            // create the response slow link from the inbound slow link
            SlowLinkSimulator responseLink =
                    new SlowLinkSimulator(slowLink);

            // this is the channel from the endpoint
            rr2 = new SocketRR(this, outSocket, tmpIn2, inSocket, tmpOut1,
                    outputText, format, null, 0, "response:",
                    responseLink);
            
            while ((rr1 != null) || (rr2 != null)) {

            		if (rr2 != null) {
            			listener.tableModel.setValueAt(rr2.getElapsed(), 1 + index, HTTPTracer.ELAPSED_COLUMN);
            		}
            		
                // Only loop as long as the connection to the target
                // machine is available - once that's gone we can stop.
                // The old way, loop until both are closed, left us
                // looping forever since no one closed the 1st one.
            	
                if ((null != rr1) && rr1.isDone()) {
                    if ((index >= 0) && (rr2 != null)) {
                        listener.tableModel.setValueAt(
                                HTTPTracer.getMessage("resp00", "Resp"), 1 + index,
                                HTTPTracer.STATE_COLUMN);
                    }
                    rr1 = null;
                }

                if ((null != rr2) && rr2.isDone()) {
                    if ((index >= 0) && (rr1 != null)) {
                        listener.tableModel.setValueAt(
                                HTTPTracer.getMessage("req00", "Req"), 1 + index,
                                HTTPTracer.STATE_COLUMN);
                    }
                    rr2 = null;
                }

                synchronized (this) {
                    this.wait(100);    // Safety just incase we're not told to wake up.
                }
            }

            active = false;

            if (index >= 0) {
                listener.tableModel.setValueAt(
                        HTTPTracer.getMessage("done00", "Done"),
                        1 + index, HTTPTracer.STATE_COLUMN);
            }

        } catch (Exception e) {
            StringWriter st = new StringWriter();
            PrintWriter wr = new PrintWriter(st);
            int index = listener.connections.indexOf(this);
            if (index >= 0) {
                listener.tableModel.setValueAt(
                        HTTPTracer.getMessage("error00", "Error"), 1 + index,
                        HTTPTracer.STATE_COLUMN);
            }
            e.printStackTrace(wr);
            wr.close();
            if (outputText != null) {
                outputText.append(st.toString());
            } else {
                // something went wrong before we had the output area
                System.out.println(st.toString());
            }
            halt();
        }
    }

    /**
     * Method wakeUp
     */
    synchronized void wakeUp() {
        this.notifyAll();
    }

    /**
     * Method halt
     */
    public void halt() {
        try {
            if (rr1 != null) {
                rr1.halt();
            }
            if (rr2 != null) {
                rr2.halt();
            }
            if (inSocket != null) {
                inSocket.close();
            }
            inSocket = null;
            if (outSocket != null) {
                outSocket.close();
            }
            outSocket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method remove
     */
    public void remove() {
        int index = -1;
        try {
            halt();
            index = listener.connections.indexOf(this);
            listener.tableModel.removeRow(index + 1);
            listener.connections.remove(index);
        } catch (Exception e) {
            System.err.println("index:=" + index + this);
            e.printStackTrace();
        }
    }
}
