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

import javax.swing.JTextArea;
import javax.swing.table.TableModel;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * this class handles the pumping of data from the incoming socket to the
 * outgoing socket
 */
class SocketRR extends Thread {

	/**
     * Field inSocket
     */
    Socket inSocket = null;

    /**
     * Field outSocket
     */
    Socket outSocket = null;

    /**
     * Field textArea
     */
    JTextArea textArea;

    /**
     * Field in
     */
    InputStream in = null;

    /**
     * Field out
     */
    OutputStream out = null;

    /**
     * Field xmlFormat
     */
    boolean xmlFormat;

    /**
     * Field done
     */
    volatile boolean done = false;

    /**
     * Field tmodel
     */
    volatile long elapsed = 0;
    
    /**
     * Field tmodel
     */
    TableModel tmodel = null;

    /**
     * Field tableIndex
     */
    int tableIndex = 0;

    /**
     * Field type
     */
    String type = null;

    /**
     * Field myConnection
     */
    Connection myConnection = null;

    /**
     * Field slowLink
     */
    SlowLinkSimulator slowLink;

    /**
     * Constructor SocketRR
     *
     * @param c
     * @param inputSocket
     * @param inputStream
     * @param outputSocket
     * @param outputStream
     * @param _textArea
     * @param format
     * @param tModel
     * @param index
     * @param type
     * @param slowLink
     */
    public SocketRR(Connection c, Socket inputSocket,
                    InputStream inputStream, Socket outputSocket,
                    OutputStream outputStream, JTextArea _textArea,
                    boolean format, TableModel tModel, int index,
                    final String type, SlowLinkSimulator slowLink) {
        inSocket = inputSocket;
        in = inputStream;
        outSocket = outputSocket;
        out = outputStream;
        textArea = _textArea;
        xmlFormat = format;
        tmodel = tModel;
        tableIndex = index;
        this.type = type;
        myConnection = c;
        this.slowLink = slowLink;
        start();
    }

    /**
     * Method isDone
     *
     * @return boolean
     */
    public boolean isDone() {
        return done;
    }

    public String getElapsed() {
    		return String.valueOf(elapsed);
    }
    
    /**
     * Method run
     */
    public void run() {
        try {
            byte[] buffer = new byte[4096];
            byte[] tmpbuffer = new byte[8192];
            int saved = 0;
            int len;
            int i1, i2;
            int i;
            int reqSaved = 0;
            int tabWidth = 3;
            boolean atMargin = true;
            int thisIndent = -1, nextIndent = -1, previousIndent = -1;
            if (tmodel != null) {
                String tmpStr = (String) tmodel.getValueAt(tableIndex,
                		HTTPTracer.REQ_COLUMN);
                if (!"".equals(tmpStr)) {
                    reqSaved = tmpStr.length();
                }
            }
            long start = System.currentTimeMillis();
            a:
            for (; ;) {
            	
                elapsed = System.currentTimeMillis() - start;
            	
                if (done) {
                    break;
                }
                
                // try{
                // len = in.available();
                // }catch(Exception e){len=0;}
                len = buffer.length;

                // Used to be 1, but if we block it doesn't matter
                // however 1 will break with some servers, including apache
                if (len == 0) {
                    len = buffer.length;
                }
                if (saved + len > buffer.length) {
                    len = buffer.length - saved;
                }
                int len1 = 0;
                while (len1 == 0) {
                    try {
                        len1 = in.read(buffer, saved, len);
                    } catch (Exception ex) {
                        if (done && (saved == 0)) {
                            break a;
                        }
                        len1 = -1;
                        break;
                    }
                }
                len = len1;
                if ((len == -1) && (saved == 0)) {
                    break;
                }
                if (len == -1) {
                    done = true;
                }

                // No matter how we may (or may not) format it, send it
                // on unformatted - we don't want to mess with how its
                // sent to the other side, just how its displayed
                if ((out != null) && (len > 0)) {
                    slowLink.pump(len);
                    out.write(buffer, saved, len);
                }
                
                if ((tmodel != null) && (reqSaved < 50)) {
                    String old = (String) tmodel.getValueAt(tableIndex,
                    		HTTPTracer.REQ_COLUMN);
                    old = old + new String(buffer, saved, len);
                    if (old.length() > 50) {
                        old = old.substring(0, 50);
                    }
                    reqSaved = old.length();
                    if ((i = old.indexOf('\n')) > 0) {
                        old = old.substring(0, i - 1);
                        reqSaved = 50;
                    }
                    tmodel.setValueAt(old, tableIndex, HTTPTracer.REQ_COLUMN);
                }
                
                
               if (xmlFormat) {

                    // Do XML Formatting
                    boolean inXML = false;
                    int bufferLen = saved;
                    if (len != -1) {
                        bufferLen += len;
                    }
                    i1 = 0;
                    i2 = 0;
                    saved = 0;
                    for (; i1 < bufferLen; i1++) {

                        // Except when we're at EOF, saved last char
                        if ((len != -1) && (i1 + 1 == bufferLen)) {
                            saved = 1;
                            break;
                        }
                        thisIndent = -1;
                        if ((buffer[i1] == '<')
                                && (buffer[i1 + 1] != '/')) {
                            previousIndent = nextIndent++;
                            thisIndent = nextIndent;
                            inXML = true;
                        }
                        if ((buffer[i1] == '<')
                                && (buffer[i1 + 1] == '/')) {
                            if (previousIndent > nextIndent) {
                                thisIndent = nextIndent;
                            }
                            previousIndent = nextIndent--;
                            inXML = true;
                        }
                        if ((buffer[i1] == '/')
                                && (buffer[i1 + 1] == '>')) {
                            previousIndent = nextIndent--;
                            inXML = true;
                        }
                        if (thisIndent != -1) {
                            if (thisIndent > 0) {
                                tmpbuffer[i2++] = (byte) '\n';
                            }
                            for (i = tabWidth * thisIndent; i > 0; i--) {
                                tmpbuffer[i2++] = (byte) ' ';
                            }
                        }
                        atMargin = ((buffer[i1] == '\n')
                                || (buffer[i1] == '\r'));
                        if (!inXML || !atMargin) {
                            tmpbuffer[i2++] = buffer[i1];
                        }
                    }
                    textArea.append(new String(tmpbuffer, 0, i2));

                    // Shift saved bytes to the beginning
                    for (i = 0; i < saved; i++) {
                        buffer[i] = buffer[bufferLen - saved + i];
                    }
                } else {
                    textArea.append(new String(buffer, 0, len));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            done = true;
            try {
                if (out != null) {
                    out.flush();
                    if (null != outSocket) {
                        outSocket.shutdownOutput();
                    } else {
                        out.close();
                    }
                    out = null;
                }
            } catch (Exception e) {
            }
            try {
                if (in != null) {
                    if (inSocket != null) {
                        inSocket.shutdownInput();
                    } else {
                        in.close();
                    }
                    in = null;
                }
            } catch (Exception e) {
            }
            myConnection.wakeUp();
        }
    }

    /**
     * Method halt
     */
    public void halt() {
        try {
            if (inSocket != null) {
                inSocket.close();
            }
            if (outSocket != null) {
                outSocket.close();
            }
            inSocket = null;
            outSocket = null;
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            in = null;
            out = null;
            done = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
