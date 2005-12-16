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

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.ResourceBundle;

/**
 * Proxy that sniffs and shows HTTP messages and responses, both SOAP and plain HTTP.
 */

public class HTTPTracer extends JFrame {

    /**
     * Field notebook
     */
    private JTabbedPane notebook = null;

    /**
     * Field STATE_COLUMN
     */
    static final int STATE_COLUMN = 0;

    /**
     * Field OUTHOST_COLUMN
     */
    static final int OUTHOST_COLUMN = 3;

    /**
     * Field REQ_COLUMN
     */
    static final int REQ_COLUMN = 4;

    /**
     * Field ELAPSED_COLUMN
     */
    static final int ELAPSED_COLUMN = 5;
    
    /**
     * Field DEFAULT_HOST
     */
    static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Field DEFAULT_PORT
     */
    static final int DEFAULT_PORT = 8888;

    /**
     * Constructor
     *
     * @param listenPort
     * @param targetHost
     * @param targetPort
     * @param embedded
     */
    public HTTPTracer(int listenPort, String targetHost, int targetPort, boolean embedded) {
        super(getMessage("httptracer00","HTTPTracer"));
        notebook = new JTabbedPane();
        this.getContentPane().add(notebook);
        new AdminPane(notebook, getMessage("admin00", "Admin"));
        if (listenPort != 0) {
            Listener l = null;
            if (targetHost == null) {
                l = new Listener(notebook, null, listenPort, targetHost, targetPort, true, null);
            } else {
                l = new Listener(notebook, null, listenPort, targetHost, targetPort, false, null);
            }
            notebook.setSelectedIndex(1);
            l.HTTPProxyHost = System.getProperty("http.proxyHost");
            if ((l.HTTPProxyHost != null) && l.HTTPProxyHost.equals("")) {
                l.HTTPProxyHost = null;
            }
            if (l.HTTPProxyHost != null) {
                String tmp = System.getProperty("http.proxyPort");
                if ((tmp != null) && tmp.equals("")) {
                    tmp = null;
                }
                if (tmp == null) {
                    l.HTTPProxyPort = 80;
                } else {
                    l.HTTPProxyPort = Integer.parseInt(tmp);
                }
            }
        }
        if (!embedded) {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        this.pack();
        this.setSize(1000, 700);
        this.setVisible(true);
    }

    /**
     * Constructor
     *
     * @param listenPort
     * @param targetHost
     * @param targetPort
     */
    public HTTPTracer(int listenPort, String targetHost, int targetPort) {
        this(listenPort, targetHost, targetPort, false);
    }

    /**
     * set up the L&F
     *
     * @param nativeLookAndFeel
     * @throws Exception
     */
    private static void setupLookAndFeel(boolean nativeLookAndFeel) throws Exception {
        String classname = UIManager.getCrossPlatformLookAndFeelClassName();
        if (nativeLookAndFeel) {
            classname = UIManager.getSystemLookAndFeelClassName();
        }
        String lafProperty = System.getProperty("httptracer.laf", "");
        if (lafProperty.length() > 0) {
            classname = lafProperty;
        }
        try {
            UIManager.setLookAndFeel(classname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    /**
     * this is our main method
     *
     * @param args
     */
    public static void main(String[] args) {
        try {

            // switch between swing L&F here
            setupLookAndFeel(true);
            if (args.length == 3) {
                int p1 = Integer.parseInt(args[0]);
                int p2 = Integer.parseInt(args[2]);
                new HTTPTracer(p1, args[1], p2);
            } else if (args.length == 1) {
                int p1 = Integer.parseInt(args[0]);
                new HTTPTracer(p1, null, 0);
            } else if (args.length != 0) {
                System.err.println(
                        getMessage("usage00", "Usage:")
                        + " HTTPTracer [listenPort targetHost targetPort]\n");
            } else {
                new HTTPTracer(0, null, 0);
            }
        } catch (Throwable exp) {
            exp.printStackTrace();
        }
    }

    /**
     * Field messages
     */
    private static ResourceBundle messages = null;

    /**
     * Get the message with the given key.  There are no arguments for this message.
     *
     * @param key
     * @param defaultMsg
     * @return string
     */
    public static String getMessage(String key, String defaultMsg) {
        try {
            if (messages == null) {
                initializeMessages();
            }
            return messages.getString(key);
        } catch (Throwable t) {

            // If there is any problem whatsoever getting the internationalized
            // message, return the default.
            return defaultMsg;
        }
    }

    /**
     * Load the resource bundle messages from the properties file.  This is ONLY done when it is
     * needed.  If no messages are printed (for example, only Wsdl2java is being run in non-
     * verbose mode) then there is no need to read the properties file.
     */
    private static void initializeMessages() {
        messages = ResourceBundle.getBundle("org.apache.axis2.tool.tracer.httptracer");
    }

}
