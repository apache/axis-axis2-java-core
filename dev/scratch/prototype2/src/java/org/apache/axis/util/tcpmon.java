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
package org.apache.axis.util ;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * TCP monitor to log http messages and responses, both SOAP and plain HTTP.
 * If you want to choose a different Swing look and feel, set the property
 * tcpmon.laf to the classname of the new look and feel
 * @author Doug Davis (dug@us.ibm.com)
 * @author Steve Loughran
 */

public class tcpmon extends JFrame {
    private JTabbedPane  notebook = null ;

    private static final int STATE_COLUMN    = 0 ;
    private static final int TIME_COLUMN     = 1 ;
    private static final int INHOST_COLUMN   = 2 ;
    private static final int OUTHOST_COLUMN  = 3 ;
    private static final int REQ_COLUMN      = 4 ;


    private static final String DEFAULT_HOST="127.0.0.1";
    private static final int    DEFAULT_PORT=8080;

    /**
     * this is the admin page
     */
    class AdminPage extends JPanel {
        public JRadioButton  listenerButton, proxyButton ;
        public JLabel        hostLabel, tportLabel;
        public NumberField  port;
        public HostnameField host;
        public NumberField  tport ;
        public JTabbedPane   noteb ;
        public JCheckBox     HTTPProxyBox ;
        public HostnameField    HTTPProxyHost;
        public NumberField HTTPProxyPort ;
        public JLabel        HTTPProxyHostLabel, HTTPProxyPortLabel ;
        public JLabel        delayTimeLabel, delayBytesLabel;
        public NumberField delayTime, delayBytes;
        public JCheckBox     delayBox;

        public AdminPage( JTabbedPane notebook, String name ) {
            JPanel     mainPane  = null ;
            JButton    addButton = null ;

            this.setLayout( new BorderLayout() );
            noteb = notebook ;

            GridBagLayout       layout        = new GridBagLayout();
            GridBagConstraints  c             = new GridBagConstraints();

            mainPane = new JPanel(layout);

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPane.add( new JLabel(getMessage("newTCP00", "Create a new TCP/IP Monitor...") + " "), c );

            // Add some blank space
            mainPane.add( Box.createRigidArea(new Dimension(1, 5)), c );

            // The listener info
            ///////////////////////////////////////////////////////////////////
            JPanel   tmpPanel = new JPanel(new GridBagLayout());

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = 1 ;
            tmpPanel.add( new JLabel(getMessage("listenPort00", "Listen Port #") + " "), c );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            tmpPanel.add( port = new NumberField(4), c );

            mainPane.add( tmpPanel, c );

            mainPane.add( Box.createRigidArea(new Dimension(1, 5)), c );

            // Group for the radio buttons
            ButtonGroup btns = new ButtonGroup();

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            mainPane.add( new JLabel(getMessage("actAs00", "Act as a...") ), c );

            // Target Host/Port section
            ///////////////////////////////////////////////////////////////////
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;

            final String listener = getMessage("listener00", "Listener");

            mainPane.add( listenerButton  = new JRadioButton( listener ), c );
            btns.add( listenerButton );
            listenerButton.setSelected( true );

            listenerButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (listener.equals(event.getActionCommand())) {
                            boolean state = listenerButton.isSelected();

                            tport.setEnabled( state );
                            host.setEnabled( state );
                            hostLabel.setForeground(state ? Color.black : Color.gray);
                            tportLabel.setForeground(state ? Color.black : Color.gray);
                        }
                    }
                }
            );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = 1 ;
            mainPane.add( Box.createRigidArea(new Dimension(25, 0)) );
            mainPane.add( hostLabel = new JLabel(getMessage("targetHostname00", "Target Hostname") + " "), c );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            host = new HostnameField(30);
            mainPane.add( host, c );
            host.setText(DEFAULT_HOST);

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = 1 ;
            mainPane.add( Box.createRigidArea(new Dimension(25, 0)) );
            mainPane.add( tportLabel = new JLabel(getMessage("targetPort00", "Target Port #") + " "), c );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            tport = new NumberField(4);
            mainPane.add( tport, c );
            tport.setValue(DEFAULT_PORT);

            // Act as proxy section
            ///////////////////////////////////////////////////////////////////
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            final String proxy = getMessage("proxy00", "Proxy");

            mainPane.add( proxyButton = new JRadioButton( proxy ), c);
            btns.add( proxyButton );

            proxyButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (proxy.equals(event.getActionCommand())) {
                            boolean state = proxyButton.isSelected();

                            tport.setEnabled( !state );
                            host.setEnabled( !state );
                            hostLabel.setForeground(state ? Color.gray : Color.black);
                            tportLabel.setForeground(state ? Color.gray : Color.black);
                        }
                    }
                }
            );

            // Spacer
            /////////////////////////////////////////////////////////////////
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            mainPane.add( Box.createRigidArea(new Dimension(1, 10)), c );

            // Options section
            ///////////////////////////////////////////////////////////////////
            JPanel       opts = new JPanel(new GridBagLayout());

            opts.setBorder( new TitledBorder(getMessage("options00", "Options")) );
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            mainPane.add( opts, c );

            // HTTP Proxy Support section
            ///////////////////////////////////////////////////////////////////
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            final String proxySupport = getMessage("proxySupport00", "HTTP Proxy Support");

            opts.add(HTTPProxyBox = new JCheckBox(proxySupport), c);

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = 1 ;
            opts.add( HTTPProxyHostLabel = new JLabel(getMessage("hostname00", "Hostname") + " "), c );
            HTTPProxyHostLabel.setForeground( Color.gray );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            opts.add( HTTPProxyHost = new HostnameField(30), c );
            HTTPProxyHost.setEnabled( false );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = 1 ;
            opts.add( HTTPProxyPortLabel = new JLabel(getMessage("port00", "Port #") + " "), c );
            HTTPProxyPortLabel.setForeground( Color.gray );

            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            opts.add( HTTPProxyPort = new NumberField(4), c );
            HTTPProxyPort.setEnabled( false );

            HTTPProxyBox.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (proxySupport.equals(event.getActionCommand())) {
                            boolean b = HTTPProxyBox.isSelected();
                            Color   color = b ? Color.black : Color.gray ;

                            HTTPProxyHost.setEnabled( b );
                            HTTPProxyPort.setEnabled( b );
                            HTTPProxyHostLabel.setForeground( color );
                            HTTPProxyPortLabel.setForeground( color );
                        }
                    }
                }
            );

            // Set default proxy values...
            String tmp = System.getProperty( "http.proxyHost" );

            if ( tmp != null && tmp.equals("") ) {
                tmp = null ;
            }

            HTTPProxyBox.setSelected( tmp != null );
            HTTPProxyHost.setEnabled( tmp != null );
            HTTPProxyPort.setEnabled( tmp != null );
            HTTPProxyHostLabel.setForeground( tmp != null ? Color.black : Color.gray);
            HTTPProxyPortLabel.setForeground( tmp != null ? Color.black : Color.gray);

            if ( tmp != null ) {
                HTTPProxyBox.setSelected( true );
                HTTPProxyHost.setText( tmp );
                tmp = System.getProperty( "http.proxyPort" );
                if ( tmp != null && tmp.equals("") ) {
                    tmp = null ;
                }
                if ( tmp == null ) {
                    tmp = "80" ;
                }
                HTTPProxyPort.setText( tmp );
            }

            //add byte delay fields
            opts.add(Box.createRigidArea(new Dimension(1, 10)), c);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            final String delaySupport = getMessage("delay00", "Simulate Slow Connection");
            opts.add(delayBox = new JCheckBox(delaySupport), c);

            //bytes per pause
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = 1;
            delayBytesLabel=new JLabel(getMessage("delay01",  "Bytes per Pause"));
            opts.add(delayBytesLabel, c);
            delayBytesLabel.setForeground(Color.gray);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            opts.add(delayBytes = new NumberField(6), c);
            delayBytes.setEnabled(false);

            //delay interval
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = 1;
            delayTimeLabel = new JLabel(getMessage("delay02", "Delay in Milliseconds"));
            opts.add(delayTimeLabel, c);
            delayTimeLabel.setForeground(Color.gray);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            opts.add(delayTime = new NumberField(6), c);
            delayTime.setEnabled(false);

            //enabler callback
            delayBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (delaySupport.equals(event.getActionCommand())) {
                        boolean b = delayBox.isSelected();
                        Color color = b ? Color.black : Color.gray;

                        delayBytes.setEnabled(b);
                        delayTime.setEnabled(b);
                        delayBytesLabel.setForeground(color);
                        delayTimeLabel.setForeground(color);
                    }
                }
            }
            );

            // Spacer
            //////////////////////////////////////////////////////////////////
            mainPane.add( Box.createRigidArea(new Dimension(1, 10)), c );

            // ADD Button
            ///////////////////////////////////////////////////////////////////
            c.anchor    = GridBagConstraints.WEST ;
            c.gridwidth = GridBagConstraints.REMAINDER ;
            final String add = getMessage("add00", "Add");

            mainPane.add( addButton = new JButton( add ), c );


            this.add( new JScrollPane( mainPane ), BorderLayout.CENTER );

            // addButton.setEnabled( false );
            addButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( add.equals(event.getActionCommand()) ) {
                            String   text ;
                            Listener l = null ;
                            int      lPort;
                            lPort=port.getValue(0);
                            if(lPort==0) {
                                //no port, button does nothing
                                return;
                            }
                            String   tHost = host.getText();
                            int      tPort = 0 ;
                            tPort=tport.getValue(0);
                            SlowLinkSimulator slowLink=null;
                            if(delayBox.isSelected()) {
                                int bytes= delayBytes.getValue(0);
                                int time = delayTime.getValue(0);
                                slowLink=new SlowLinkSimulator(bytes,time);
                            }
                            try {
                            l = new Listener( noteb, null, lPort, tHost, tPort,
                                           proxyButton.isSelected(), slowLink);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            // Pick-up the HTTP Proxy settings
                            ///////////////////////////////////////////////////
                            text = HTTPProxyHost.getText();
                            if ( "".equals(text) ) {
                                text = null ;
                            }
                            l.HTTPProxyHost = text ;
                            text = HTTPProxyPort.getText();
                            int proxyPort=HTTPProxyPort.getValue(-1);
                            if(proxyPort!=-1) {
                                l.HTTPProxyPort = Integer.parseInt(text);
                            }
                            //reset the port
                            port.setText(null);

                            /* but not, any more, the target port and host
                               values
                            host.setText(null);
                            tport.setText(null);
                            */
                        }
                    }
                }
            );

            notebook.addTab( name, this );
            notebook.repaint();
            notebook.setSelectedIndex( notebook.getTabCount() - 1 );
        }


    }

    /**
     * wait for incoming connections, spawn a connection thread when
     * stuff comes in.
     */
    class SocketWaiter extends Thread {
        ServerSocket  sSocket = null ;
        Listener      listener ;
        int           port ;
        boolean       pleaseStop = false ;

        public SocketWaiter(Listener l, int p) {
            listener = l ;
            port = p ;
            start();
        }

        public void run() {
            try {
                listener.setLeft( new JLabel(getMessage("wait00", " Waiting for Connection...") ) );
                listener.repaint();
                sSocket = new ServerSocket( port );
                for (; ; ) {
                    Socket inSocket = sSocket.accept();

                    if ( pleaseStop ) {
                        break ;
                    }
                    new Connection( listener, inSocket );
                    inSocket = null ;
                }
            } catch ( Exception exp ) {
                if ( !"socket closed".equals(exp.getMessage()) ) {
                    JLabel tmp = new JLabel( exp.toString() );

                    tmp.setForeground( Color.red );
                    listener.setLeft( tmp );
                    listener.setRight( new JLabel("") );
                    listener.stop();
                }
            }
        }

        /**
         * force a halt by connecting to self and then closing the server socket
         */
        public void halt() {
            try {
                pleaseStop = true ;
                new Socket( "127.0.0.1", port );
                if ( sSocket != null ) {
                    sSocket.close();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    /**
     * class to simulate slow connections by slowing down the system
     */
    static class SlowLinkSimulator {
        private int delayBytes;
        private int delayTime;
        private int currentBytes;
        private int totalBytes;

        /**
         * construct
         * @param delayBytes bytes per delay; set to 0 for no delay
         * @param delayTime delay time per delay in milliseconds
         */
        public SlowLinkSimulator(int delayBytes, int delayTime) {
            this.delayBytes = delayBytes;
            this.delayTime = delayTime;
        }

        /**
         * construct by copying delay bytes and time, but not current
         * count of bytes
         * @param that source of data
         */
        public SlowLinkSimulator(SlowLinkSimulator that) {
            this.delayBytes=that.delayBytes;
            this.delayTime=that.delayTime;
        }

        /**
         * how many bytes have gone past?
         * @return
         */
        public int getTotalBytes() {
            return totalBytes;
        }

        /**
         * log #of bytes pumped. Will pause when necessary. This method is not
         * synchronized
         * @param bytes
         */
        public void pump(int bytes) {
            totalBytes+=bytes;
            if(delayBytes==0) {
                //when not delaying, we are just a byte counter
                return;
            }
            currentBytes += bytes;
            if(currentBytes>delayBytes) {
                //we have overshot. lets find out how far
                int delaysize=currentBytes/delayBytes;
                long delay=delaysize*(long)delayTime;
                //move byte counter down to the remainder of bytes
                currentBytes=currentBytes%delayBytes;
                //now wait
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    ; //ignore the exception
                }
            }
        }

        /**
         * get the current byte count
         * @return
         */
        public int getCurrentBytes() {
            return currentBytes;
        }

        /**
         * set the current byte count
         * @param currentBytes
         */
        public void setCurrentBytes(int currentBytes) {
            this.currentBytes = currentBytes;
        }

    }

    /**
     * this class handles the pumping of data from the incoming socket to the
     * outgoing socket
     */
    class SocketRR extends Thread {
        Socket        inSocket  = null ;
        Socket        outSocket  = null ;
        JTextArea     textArea ;
        InputStream   in = null ;
        OutputStream  out = null ;
        boolean       xmlFormat ;
        volatile boolean       done = false ;
        TableModel    tmodel = null ;
        int           tableIndex = 0 ;
        String type = null;
        Connection    myConnection = null;
        SlowLinkSimulator slowLink;

        public SocketRR(Connection c, Socket inputSocket, InputStream inputStream,
            Socket outputSocket, OutputStream outputStream,
            JTextArea _textArea, boolean format,
            TableModel tModel, int index, final String type, SlowLinkSimulator slowLink) {
            inSocket = inputSocket ;
            in       = inputStream ;
            outSocket = outputSocket ;
            out       = outputStream ;
            textArea  = _textArea ;
            xmlFormat = format ;
            tmodel    = tModel ;
            tableIndex = index ;
            this.type = type;
            myConnection = c;
            this.slowLink= slowLink;
            start();
        }

        public boolean isDone() {
            return ( done );
        }

        public void run() {
            try {
                byte[]      buffer = new byte[4096];
                byte[]      tmpbuffer = new byte[8192];
                int         saved = 0 ;
                int         len ;
                int         i1, i2 ;
                int         i ;
                int         reqSaved = 0 ;
                int         tabWidth = 3 ;
                boolean     atMargin = true ;
                int         thisIndent = -1,
                    nextIndent = -1,
                    previousIndent = -1;

                //if ( inSocket  != null ) inSocket.setSoTimeout( 10 );
                //if ( outSocket != null ) outSocket.setSoTimeout( 10 );

                if ( tmodel != null ) {
                    String tmpStr = (String) tmodel.getValueAt(tableIndex,
                            REQ_COLUMN);

                    if ( !"".equals(tmpStr) ) {
                        reqSaved = tmpStr.length();
                    }
                }

            a:
                for ( ; ; ) {
                    if ( done ) {
                        break;
                    }
                    //try{
                    //len = in.available();
                    //}catch(Exception e){len=0;}
                    len = buffer.length ;
                    // Used to be 1, but if we block it doesn't matter
                    // however 1 will break with some servers, including apache
                    if ( len == 0 ) {
                        len = buffer.length;
                    }
                    if ( saved + len > buffer.length) {
                        len = buffer.length - saved ;
                    }
                    int len1 = 0;

                    while ( len1 == 0 ) {
                        try {
                            len1 = in.read(buffer, saved, len);
                        }
                        catch ( Exception ex ) {
                            if ( done && saved == 0  ) {
                                break a;
                            }
                            len1 = -1;
                            break;
                        }
                    }
                    len = len1;

                    if ( len == -1 && saved == 0 ) {
                        break ;
                    }
                    if ( len == -1) {
                        done = true;
                    }

                    // No matter how we may (or may not) format it, send it
                    // on unformatted - we don't want to mess with how its
                    // sent to the other side, just how its displayed
                    if ( out != null && len > 0 ) {
                        slowLink.pump(len);
                        out.write( buffer, saved, len );
                    }

                    if ( tmodel != null && reqSaved < 50 ) {
                        String old = (String) tmodel.getValueAt( tableIndex,
                                REQ_COLUMN);

                        old = old + new String(buffer, saved, len);
                        if ( old.length() > 50 ) {
                            old = old.substring(0, 50);
                        }

                        reqSaved = old.length();

                        if ( (i = old.indexOf('\n')) > 0 ) {
                            old = old.substring(0, i - 1);
                            reqSaved = 50 ;
                        }

                        tmodel.setValueAt( old, tableIndex, REQ_COLUMN );
                    }

                    if ( xmlFormat ) {
                        // Do XML Formatting
                        boolean inXML = false ;
                        int     bufferLen = saved ;

                        if ( len != -1 ) {
                            bufferLen += len ;
                        }
                        i1 = 0 ;
                        i2 = 0 ;
                        saved = 0 ;
                        for ( ; i1 < bufferLen ; i1++ ) {
                            // Except when we're at EOF, saved last char
                            if ( len != -1 && i1 + 1 == bufferLen ) {
                                saved = 1;
                                break;
                            }
                            thisIndent = -1;
                            if ( buffer[i1] == '<' && buffer[i1 + 1] != '/' ) {
                                previousIndent = nextIndent++;
                                thisIndent = nextIndent;
                                inXML = true ;
                            }
                            if ( buffer[i1] == '<' && buffer[i1 + 1] == '/' ) {
                                if (previousIndent > nextIndent) {
                                    thisIndent = nextIndent;
                                }
                                previousIndent = nextIndent--;
                                inXML = true ;
                            }
                            if ( buffer[i1] == '/' && buffer[i1 + 1] == '>' ) {
                                previousIndent = nextIndent--;
                                inXML = true ;
                            }
                            if ( thisIndent != -1 ) {
                                if ( thisIndent > 0 ) {
                                    tmpbuffer[i2++] = (byte) '\n';
                                }
                                for ( i = tabWidth * thisIndent; i > 0; i-- ) {
                                    tmpbuffer[i2++] = (byte) ' ';
                                }
                            }
                            atMargin = ( buffer[i1] == '\n' || buffer[i1] == '\r');

                            if ( !inXML || !atMargin ) {
                                tmpbuffer[i2++] = buffer[i1];
                            }
                        }

                        textArea.append( new String( tmpbuffer, 0, i2 ) );

                        // Shift saved bytes to the beginning
                        for ( i = 0 ; i < saved ; i++ ) {
                            buffer[i] = buffer[bufferLen - saved + i];
                        }
                    }
                    else {
                        textArea.append( new String( buffer, 0, len ) );
                    }
                // this.sleep(3);  // Let other threads have a chance to run
                }
            // this.sleep(3);  // Let other threads have a chance to run
            // halt();
            // Only set the 'done' flag if we were reading from a
            // Socket - if we were reading from an input stream then
            // we'll let the other side control when we're done
            //      if ( inSocket != null ) done = true ;
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
            finally {
                done = true ;
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
                }
                catch (Exception e) {
                    ;
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
                }
                catch (Exception e) {
                    ;
                }
                myConnection.wakeUp();
            }
        }

        public  void halt() {
            try {
                if ( inSocket != null ) {
                    inSocket.close();
                }
                if ( outSocket != null ) {
                    outSocket.close();
                }
                inSocket  = null ;
                outSocket = null ;
                if ( in != null ) {
                    in.close();
                }
                if ( out != null ) {
                    out.close();
                }
                in = null ;
                out = null ;
                done = true;
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    /**
     * a connection listens to a single current connection
     */
    class Connection extends Thread {
        Listener     listener ;
        boolean      active ;
        String       fromHost ;
        String       time ;
        JTextArea    inputText    = null ;
        JScrollPane  inputScroll  = null ;
        JTextArea    outputText   = null ;
        JScrollPane  outputScroll = null ;
        Socket       inSocket     = null ;
        Socket       outSocket    = null ;
        Thread       clientThread = null ;
        Thread       serverThread = null ;
        SocketRR     rr1          = null ;
        SocketRR     rr2          = null ;
        InputStream  inputStream  = null ;

        String       HTTPProxyHost = null ;
        int          HTTPProxyPort = 80 ;
        private SlowLinkSimulator slowLink;

        public Connection(Listener l) {
            listener = l ;
            HTTPProxyHost = l.HTTPProxyHost ;
            HTTPProxyPort = l.HTTPProxyPort ;
            slowLink =l.slowLink;
        }

        public Connection(Listener l, Socket s ) {
            this (l);
            inSocket = s ;
            start();
        }

        public Connection(Listener l, InputStream in ) {
            this (l);
            inputStream = in ;
            start();
        }

        public void run() {
            try {
                active        = true ;

                HTTPProxyHost = System.getProperty( "http.proxyHost" );
                if ( HTTPProxyHost != null && HTTPProxyHost.equals("") ) {
                    HTTPProxyHost = null ;
                }

                if ( HTTPProxyHost != null ) {
                    String tmp = System.getProperty( "http.proxyPort" );

                    if ( tmp != null && tmp.equals("") ) {
                        tmp = null ;
                    }
                    if ( tmp == null ) {
                        HTTPProxyPort = 80 ;
                    } else {
                        HTTPProxyPort = Integer.parseInt( tmp );
                    }
                }

                if ( inSocket != null ) {
                    fromHost = (inSocket.getInetAddress()).getHostName();
                } else {
                    fromHost = "resend" ;
                }


                String dateformat=getMessage("dateformat00", "yyyy-MM-dd HH:mm:ss");
                DateFormat   df = new SimpleDateFormat(dateformat);

                time = df.format( new Date() );

                int count = listener.connections.size();

                listener.tableModel.insertRow(count + 1, new Object[] {
                        getMessage("active00", "Active"),
                        time,
                        fromHost,
                        listener.hostField.getText(), ""
                    }
                );
                listener.connections.add( this );
                inputText  = new JTextArea( null, null, 20, 80 );
                inputScroll = new JScrollPane( inputText );
                outputText = new JTextArea( null, null, 20, 80 );
                outputScroll = new JScrollPane( outputText );

                ListSelectionModel lsm = listener.connectionTable.getSelectionModel();

                if ( count == 0 || lsm.getLeadSelectionIndex() == 0 ) {
                    listener.outPane.setVisible( false );
                    int divLoc = listener.outPane.getDividerLocation();

                    listener.setLeft( inputScroll );
                    listener.setRight( outputScroll );

                    listener.removeButton.setEnabled(false);
                    listener.removeAllButton.setEnabled(true);
                    listener.saveButton.setEnabled(true);
                    listener.resendButton.setEnabled(true);
                    listener.outPane.setDividerLocation(divLoc);
                    listener.outPane.setVisible( true );
                }

                String targetHost = listener.hostField.getText();
                int    targetPort = Integer.parseInt(listener.tPortField.getText());
                int    listenPort = Integer.parseInt(listener.portField.getText());

                InputStream  tmpIn1  = inputStream ;
                OutputStream tmpOut1 = null ;

                InputStream  tmpIn2  = null ;
                OutputStream tmpOut2 = null ;

                if ( tmpIn1 == null ) {
                    tmpIn1  = inSocket.getInputStream();
                }

                if ( inSocket != null ) {
                    tmpOut1 = inSocket.getOutputStream();
                }

                String         bufferedData = null ;
                StringBuffer   buf = null ;

                int index = listener.connections.indexOf( this );

                if (listener.isProxyBox.isSelected() || HTTPProxyHost != null) {
                    // Check if we're a proxy
                    byte[]       b = new byte[1];

                    buf = new StringBuffer();
                    String       s ;

                    for ( ; ; ) {
                        int len ;

                        len = tmpIn1.read(b, 0, 1);
                        if ( len == -1 ) {
                            break ;
                        }
                        s = new String( b );
                        buf.append( s );
                        if ( b[0] != '\n' ) {
                            continue ;
                        }
                        break ;
                    }

                    bufferedData = buf.toString();
                    inputText.append( bufferedData );

                    if ( bufferedData.startsWith( "GET " ) ||
                        bufferedData.startsWith( "POST " ) ||
                        bufferedData.startsWith( "PUT " ) ||
                        bufferedData.startsWith( "DELETE " ) ) {
                        int  start, end ;
                        URL  url ;

                        start = bufferedData.indexOf( ' ' ) + 1;
                        while ( bufferedData.charAt(start) == ' ' ) {
                            start++ ;
                        }
                        end   = bufferedData.indexOf( ' ', start );
                        String urlString = bufferedData.substring( start, end );

                        if ( urlString.charAt(0) == '/' ) {
                            urlString = urlString.substring(1);
                        }
                        if ( listener.isProxyBox.isSelected() ) {
                            url = new URL( urlString );
                            targetHost = url.getHost();
                            targetPort = url.getPort();
                            if ( targetPort == -1 ) {
                                targetPort = 80 ;
                            }

                            listener.tableModel.setValueAt( targetHost, index + 1,
                                OUTHOST_COLUMN );
                            bufferedData = bufferedData.substring( 0, start) +
                            url.getFile() +
                            bufferedData.substring( end );
                        }
                        else {
                            url = new URL( "http://" + targetHost + ":" +
                                targetPort + "/" + urlString );

                            listener.tableModel.setValueAt( targetHost, index + 1,
                                OUTHOST_COLUMN );
                            bufferedData = bufferedData.substring( 0, start) +
                                url.toExternalForm() +
                                bufferedData.substring( end );

                            targetHost = HTTPProxyHost ;
                            targetPort = HTTPProxyPort ;
                        }

                    }
                }
                else {
                    //
                    // Change Host: header to point to correct host
                    //
                    byte[] b1 = new byte[1];

                    buf = new StringBuffer();
                    String s1;
                    String lastLine = null ;

                    for ( ; ; ) {
                        int len ;

                        len = tmpIn1.read(b1, 0, 1);
                        if ( len == -1 ) {
                            break ;
                        }
                        s1 = new String( b1 );
                        buf.append( s1 );
                        if ( b1[0] != '\n' ) {
                            continue ;
                        }
                        // we have a complete line
                        String line = buf.toString();

                        buf.setLength(0);
                        // check to see if we have found Host: header
                        if (line.startsWith("Host: ")) {
                            // we need to update the hostname to target host
                            String newHost = "Host: " + targetHost + ":" + listenPort + "\r\n";

                            bufferedData = bufferedData.concat(newHost);
                            break ;
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
                            break ;
                        }
                        lastLine = line ;
                    }
                    if ( bufferedData != null ) {
                        inputText.append( bufferedData );
                        int idx = bufferedData.length() < 50 ? bufferedData.length() : 50;
                        s1 = bufferedData.substring( 0, idx );
                        int i = s1.indexOf('\n');

                        if ( i > 0 ) {
                            s1 = s1.substring(0, i - 1);
                        }
                        s1 = s1 + "                           " +
                                  "                       ";
                        s1 = s1.substring(0, 51);
                        listener.tableModel.setValueAt( s1, index + 1,
                            REQ_COLUMN );
                    }
                }

                if ( targetPort == -1 ) {
                    targetPort = 80 ;
                }
                outSocket = new Socket(targetHost, targetPort );

                tmpIn2  = outSocket.getInputStream();
                tmpOut2 = outSocket.getOutputStream();

                if ( bufferedData != null ) {
                    byte[] b = bufferedData.getBytes();
                    tmpOut2.write( b );
                    slowLink.pump(b.length);
                }

                boolean format = listener.xmlFormatBox.isSelected();


                //this is the channel to the endpoint
                rr1 = new SocketRR(this, inSocket, tmpIn1, outSocket,
                    tmpOut2, inputText, format,
                    listener.tableModel, index + 1, "request:", slowLink);
                //create the response slow link from the inbound slow link
                SlowLinkSimulator responseLink = new SlowLinkSimulator(slowLink);
                //this is the channel from the endpoint
                rr2 = new SocketRR( this, outSocket, tmpIn2, inSocket,
                    tmpOut1, outputText, format,
                    null, 0, "response:", responseLink);

                while ( rr1 != null || rr2 != null ) {
                    // Only loop as long as the connection to the target
                    // machine is available - once that's gone we can stop.
                    // The old way, loop until both are closed, left us
                    // looping forever since no one closed the 1st one.
                    // while( !rr2.isDone() )
                    if (null != rr1 && rr1.isDone()) {
                        if ( index >= 0  && rr2 != null) {
                            listener.tableModel.setValueAt(getMessage("resp00", "Resp"),
                                1 + index, STATE_COLUMN );
                        }
                       rr1 = null;
                    }
                    if (null != rr2 && rr2.isDone()) {
                        if ( index >= 0 && rr1 != null ) {
                            listener.tableModel.setValueAt(getMessage("req00", "Req"),
                                1 + index, STATE_COLUMN );
                        }
                        rr2 = null;
                    }

                    //  Thread.sleep( 10 );
                    synchronized ( this) {
                        this.wait(1000); //Safety just incase we're not told to wake up.
                    }
                }

                //  System.out.println("Done ");
                // rr1.halt();
                // rr2.halt();


                active = false ;

                /*
                 if ( inSocket != null ) {
                 inSocket.close();
                 inSocket = null ;
                 }
                 outSocket.close();
                 outSocket = null ;
                 */

                if ( index >= 0 ) {
                    listener.tableModel.setValueAt(getMessage("done00", "Done"),
                        1 + index, STATE_COLUMN );

                }
            }
            catch ( Exception e ) {
                StringWriter st = new StringWriter();
                PrintWriter  wr = new PrintWriter(st);
                int index = listener.connections.indexOf( this );

                if ( index >= 0 ) {
                    listener.tableModel.setValueAt( getMessage("error00", "Error"), 1 + index, STATE_COLUMN );
                }
                e.printStackTrace(wr);
                wr.close();
                if(outputText!=null) {
                    outputText.append( st.toString() );
                } else {
                    //something went wrong before we had the output area
                    System.out.println(st.toString());
                }
                halt();
            }
        }

        synchronized void wakeUp() {
            this.notifyAll();
        }

        public void halt() {
            try {
                if ( rr1 != null ) {
                    rr1.halt();
                }
                if ( rr2 != null ) {
                    rr2.halt();
                }
                if ( inSocket  != null ) {
                    inSocket.close();
                }
                inSocket = null ;
                if ( outSocket != null ) {
                    outSocket.close();
                }
                outSocket = null ;
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        public void remove() {
            int index = -1;

            try {
                halt();
                index = listener.connections.indexOf( this );
                listener.tableModel.removeRow( index + 1 );
                listener.connections.remove( index );
            }
            catch ( Exception e ) {
                System.err.println("index:=" + index + this );
                e.printStackTrace();
            }
        }
    }


    /**
     * this is one of the tabbed panels that acts as the actual proxy
     */
    class Listener extends JPanel {
        public  Socket      inputSocket     = null ;
        public  Socket      outputSocket    = null ;
        public  JTextField  portField       = null ;
        public  JTextField  hostField       = null ;
        public  JTextField  tPortField      = null ;
        public  JCheckBox   isProxyBox      = null ;
        public  JButton     stopButton      = null ;
        public  JButton     removeButton    = null ;
        public  JButton     removeAllButton = null ;
        public  JCheckBox   xmlFormatBox    = null ;
        public  JButton     saveButton      = null ;
        public  JButton     resendButton    = null ;
        public  JButton     switchButton    = null ;
        public  JButton     closeButton     = null ;
        public  JTable      connectionTable = null ;
        public  DefaultTableModel  tableModel      = null ;
        public  JSplitPane  outPane         = null ;
        public  ServerSocket sSocket        = null ;
        public  SocketWaiter sw = null ;
        public  JPanel      leftPanel       = null ;
        public  JPanel      rightPanel      = null ;
        public  JTabbedPane notebook        = null ;
        public  String      HTTPProxyHost   = null ;
        public  int         HTTPProxyPort   = 80 ;
        public  int         delayBytes      = 0;
        public  int         delayTime       = 0;
        public SlowLinkSimulator slowLink;

        public final Vector connections = new Vector();

        /**
         * create a listener
         * @param _notebook
         * @param name
         * @param listenPort
         * @param host
         * @param targetPort
         * @param isProxy
         * @param slowLink optional reference to a slow connection
         */
        public Listener(JTabbedPane _notebook, String name,
            int listenPort, String host, int targetPort,
            boolean isProxy, SlowLinkSimulator slowLink) {
            notebook = _notebook ;
            if ( name == null ) {
                name = getMessage("port01", "Port") + " " + listenPort ;
            }
            //set the slow link to the passed down link
            if(slowLink!=null) {
                this.slowLink=slowLink;
            } else {
                //or make up a no-op one.
                this.slowLink=new SlowLinkSimulator(0,0);
            }
            this.setLayout( new BorderLayout() );

            // 1st component is just a row of labels and 1-line entry fields
            /////////////////////////////////////////////////////////////////////
            JPanel top = new JPanel();

            top.setLayout( new BoxLayout(top, BoxLayout.X_AXIS) );
            top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            final String start = getMessage("start00", "Start");

            top.add( stopButton = new JButton( start ) );
            top.add( Box.createRigidArea(new Dimension(5, 0)) );
            top.add( new JLabel( "  " + getMessage("listenPort01", "Listen Port:") + " ", SwingConstants.RIGHT ) );
            top.add( portField = new JTextField( "" + listenPort, 4 ) );
            top.add( new JLabel( "  " + getMessage("host00", "Host:"), SwingConstants.RIGHT ) );
            top.add( hostField = new JTextField( host, 30 ) );
            top.add( new JLabel( "  " + getMessage("port02", "Port:") + " ", SwingConstants.RIGHT ) );
            top.add( tPortField = new JTextField( "" + targetPort, 4 ) );
            top.add( Box.createRigidArea(new Dimension(5, 0)) );
            top.add( isProxyBox = new JCheckBox(getMessage("proxy00", "Proxy")) );

            isProxyBox.addChangeListener( new BasicButtonListener(isProxyBox) {
                    public void stateChanged(ChangeEvent event) {
                        JCheckBox box = (JCheckBox) event.getSource();
                        boolean state = box.isSelected();

                        tPortField.setEnabled( !state );
                        hostField.setEnabled( !state );
                    }
                }
            );

            isProxyBox.setSelected(isProxy);

            portField.setEditable(false);
            portField.setMaximumSize(new Dimension(50, Short.MAX_VALUE) );
            hostField.setEditable(false);
            hostField.setMaximumSize(new Dimension(85, Short.MAX_VALUE) );
            tPortField.setEditable(false);
            tPortField.setMaximumSize(new Dimension(50, Short.MAX_VALUE) );

            stopButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( getMessage("stop00", "Stop").equals(event.getActionCommand()) ) {
                            stop();
                        }
                        if ( start.equals(event.getActionCommand()) ) {
                            start();
                        }
                    }
                }
            );

            this.add( top, BorderLayout.NORTH );

            // 2nd component is a split pane with a table on the top
            // and the request/response text areas on the bottom
            /////////////////////////////////////////////////////////////////////

            tableModel = new DefaultTableModel(new String[] {
                    getMessage("state00", "State"),
                    getMessage("time00", "Time"),
                    getMessage("requestHost00", "Request Host"),
                    getMessage("targetHost", "Target Host"),
                    getMessage("request00", "Request...")
                } , 0 );

            tableModel.addRow( new Object[] {
                    "---", getMessage("mostRecent00", "Most Recent"), "---", "---", "---"
                }
            );

            connectionTable = new JTable(1, 2);
            connectionTable.setModel( tableModel );
            connectionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            // Reduce the STATE column and increase the REQ column
            TableColumn col ;

            col = connectionTable.getColumnModel().getColumn(STATE_COLUMN);
            col.setMaxWidth( col.getPreferredWidth() / 2 );
            col = connectionTable.getColumnModel().getColumn(REQ_COLUMN);
            col.setPreferredWidth( col.getPreferredWidth() * 2 );


            ListSelectionModel sel = connectionTable.getSelectionModel();

            sel.addListSelectionListener( new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        if (event.getValueIsAdjusting()) {
                            return ;
                        }
                        ListSelectionModel m = (ListSelectionModel) event.getSource();
                        int divLoc = outPane.getDividerLocation();

                        if (m.isSelectionEmpty()) {
                            setLeft( new JLabel(" " + getMessage("wait00", "Waiting for Connection...") ) );
                            setRight( new JLabel("") );
                            removeButton.setEnabled(false);
                            removeAllButton.setEnabled(false);
                            saveButton.setEnabled(false);
                            resendButton.setEnabled(false);
                        }
                        else {
                            int row = m.getLeadSelectionIndex();

                            if ( row == 0 ) {
                                if ( connections.size() == 0 ) {
                                    setLeft(new JLabel(" " + getMessage("wait00", "Waiting for connection...")));
                                    setRight(new JLabel(""));
                                    removeButton.setEnabled(false);
                                    removeAllButton.setEnabled(false);
                                    saveButton.setEnabled(false);
                                    resendButton.setEnabled(false);
                                }
                                else {
                                    Connection conn = (Connection) connections.lastElement();

                                    setLeft( conn.inputScroll );
                                    setRight( conn.outputScroll );
                                    removeButton.setEnabled(false);
                                    removeAllButton.setEnabled(true);
                                    saveButton.setEnabled(true);
                                    resendButton.setEnabled(true);
                                }
                            }
                            else {
                                Connection conn = (Connection) connections.get(row - 1);

                                setLeft( conn.inputScroll );
                                setRight( conn.outputScroll );
                                removeButton.setEnabled(true);
                                removeAllButton.setEnabled(true);
                                saveButton.setEnabled(true);
                                resendButton.setEnabled(true);
                            }
                        }
                        outPane.setDividerLocation(divLoc);
                    }
                }
            );

            JPanel  tablePane = new JPanel();

            tablePane.setLayout( new BorderLayout() );

            JScrollPane tableScrollPane = new JScrollPane( connectionTable );

            tablePane.add( tableScrollPane, BorderLayout.CENTER );
            JPanel buttons = new JPanel();

            buttons.setLayout( new BoxLayout(buttons, BoxLayout.X_AXIS) );
            buttons.setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5) );
            final String removeSelected = getMessage("removeSelected00", "Remove Selected");

            buttons.add( removeButton = new JButton(removeSelected) );
            buttons.add( Box.createRigidArea(new Dimension(5, 0)) );
            final String removeAll = getMessage("removeAll00", "Remove All");

            buttons.add( removeAllButton = new JButton(removeAll) );
            tablePane.add( buttons, BorderLayout.SOUTH );

            removeButton.setEnabled( false );
            removeButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( removeSelected.equals(event.getActionCommand()) ) {
                            remove();
                        }
                    }
                }
            );

            removeAllButton.setEnabled( false );
            removeAllButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( removeAll.equals(event.getActionCommand()) ) {
                            removeAll();
                        }
                    }
                }
            );

            // Add Response Section
            /////////////////////////////////////////////////////////////////////
            JPanel     pane2     = new JPanel();

            pane2.setLayout( new BorderLayout() );

            leftPanel = new JPanel();
            leftPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
            leftPanel.setLayout( new BoxLayout(leftPanel, BoxLayout.Y_AXIS) );
            leftPanel.add( new JLabel("  " + getMessage("request01", "Request")) );
            leftPanel.add( new JLabel(" " + getMessage("wait01", "Waiting for connection") ));

            rightPanel = new JPanel();
            rightPanel.setLayout( new BoxLayout(rightPanel, BoxLayout.Y_AXIS) );
            rightPanel.add( new JLabel("  " + getMessage("response00", "Response")) );
            rightPanel.add( new JLabel("") );

            outPane = new JSplitPane(0, leftPanel, rightPanel );
            outPane.setDividerSize(4);
            pane2.add( outPane, BorderLayout.CENTER );

            JPanel bottomButtons = new JPanel();

            bottomButtons.setLayout( new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
            bottomButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            bottomButtons.add( xmlFormatBox = new JCheckBox( getMessage("xmlFormat00", "XML Format") ) );
            bottomButtons.add( Box.createRigidArea(new Dimension(5, 0)) );
            final String save = getMessage("save00", "Save");

            bottomButtons.add( saveButton = new JButton( save ) );
            bottomButtons.add( Box.createRigidArea(new Dimension(5, 0)) );
            final String resend = getMessage("resend00", "Resend");

            bottomButtons.add( resendButton = new JButton( resend ) );
            bottomButtons.add( Box.createRigidArea(new Dimension(5, 0)) );
            final String switchStr = getMessage("switch00", "Switch Layout");

            bottomButtons.add( switchButton = new JButton( switchStr ) );
            bottomButtons.add( Box.createHorizontalGlue() );
            final String close = getMessage("close00", "Close");

            bottomButtons.add( closeButton = new JButton( close ) );
            pane2.add( bottomButtons, BorderLayout.SOUTH );

            saveButton.setEnabled( false );
            saveButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( save.equals(event.getActionCommand()) ) {
                            save();
                        }
                    }
                }
            );

            resendButton.setEnabled( false );
            resendButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if ( resend.equals(event.getActionCommand()) ) {
                            resend();
                        }
                    }
                }
            );

            switchButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (switchStr.equals(event.getActionCommand()) ) {
                            int v = outPane.getOrientation();

                            if ( v == 0 ) {
                                // top/bottom
                                outPane.setOrientation(1);
                            }
                            else  {
                                // left/right
                                outPane.setOrientation(0);
                            }
                            outPane.setDividerLocation(0.5);
                        }
                    }
                }
            );

            closeButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (close.equals(event.getActionCommand()) ) {
                            close();
                        }
                    }
                }
            );

            JSplitPane  pane1 = new JSplitPane( 0 );

            pane1.setDividerSize(4);
            pane1.setTopComponent( tablePane );
            pane1.setBottomComponent( pane2 );
            pane1.setDividerLocation( 150 );
            this.add( pane1, BorderLayout.CENTER );

            //
            ////////////////////////////////////////////////////////////////////
            sel.setSelectionInterval(0, 0);
            outPane.setDividerLocation( 150 );
            notebook.addTab( name, this );
            start();
        }

        public void setLeft(Component left) {
            leftPanel.removeAll();
            leftPanel.add(left);
        }

        public void setRight(Component right) {
            rightPanel.removeAll();
            rightPanel.add(right);
        }

        public void start() {
            int  port = Integer.parseInt( portField.getText() );

            portField.setText( "" + port );
            int i = notebook.indexOfComponent( this );

            notebook.setTitleAt( i, getMessage("port01", "Port") + " " + port );

            int  tmp = Integer.parseInt( tPortField.getText() );

            tPortField.setText( "" + tmp );

            sw = new SocketWaiter( this, port );
            stopButton.setText( getMessage("stop00", "Stop") );

            portField.setEditable(false);
            hostField.setEditable(false);
            tPortField.setEditable(false);
            isProxyBox.setEnabled(false);
        }

        public void close() {
            stop();
            notebook.remove( this );
        }

        public void stop() {
            try {
                for ( int i = 0 ; i < connections.size() ; i++ ) {
                    Connection conn = (Connection) connections.get( i );

                    conn.halt();
                }
                sw.halt();
                stopButton.setText( getMessage("start00", "Start") );
                portField.setEditable(true);
                hostField.setEditable(true);
                tPortField.setEditable(true);
                isProxyBox.setEnabled(true);
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        public void remove() {
            ListSelectionModel lsm = connectionTable.getSelectionModel();
            int bot = lsm.getMinSelectionIndex();
            int top = lsm.getMaxSelectionIndex();

            for ( int i = top ; i >= bot ; i-- ) {
                ((Connection) connections.get(i - 1)).remove();
            }
            if ( bot > connections.size() ) {
                bot = connections.size();
            }
            lsm.setSelectionInterval(bot, bot);
        }

        public void removeAll() {
            ListSelectionModel lsm = connectionTable.getSelectionModel();
            lsm.clearSelection();
            while ( connections.size() > 0 ) {
                ((Connection) connections.get(0)).remove();
            }

            lsm.setSelectionInterval(0, 0);
        }

        public void save() {
            JFileChooser  dialog = new JFileChooser( "." );
            int rc = dialog.showSaveDialog( this );

            if ( rc == JFileChooser.APPROVE_OPTION ) {
                try {
                    File             file = dialog.getSelectedFile();
                    FileOutputStream out  = new FileOutputStream( file );

                    ListSelectionModel lsm = connectionTable.getSelectionModel();

                    rc = lsm.getLeadSelectionIndex();

                    int n = 0;
                    for (Iterator i = connections.iterator();i.hasNext();n++) {
                      Connection conn = (Connection)i.next();
                      if (lsm.isSelectedIndex(n + 1) ||
                                   (!(i.hasNext()) && lsm.getLeadSelectionIndex() == 0)) {
                        rc = Integer.parseInt( portField.getText() );
                        out.write("\n==============\n".getBytes());
                        out.write( ((getMessage("listenPort01", "Listen Port:") + " " + rc + "\n" )).getBytes() );
                        out.write( (getMessage("targetHost01", "Target Host:") + " " + hostField.getText() +
                                    "\n" ).getBytes() );
                        rc = Integer.parseInt( tPortField.getText() );
                        out.write( ((getMessage("targetPort01", "Target Port:") + " " + rc + "\n" )).getBytes() );

                        out.write( (("==== " + getMessage("request01", "Request") + " ====\n" )).getBytes() );
                        out.write( conn.inputText.getText().getBytes() );

                        out.write( (("==== " + getMessage("response00", "Response") + " ====\n" )).getBytes() );
                        out.write( conn.outputText.getText().getBytes() );
                        out.write("\n==============\n".getBytes());
                      }
                    }

                    out.close();
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }

        public void resend() {
            int rc ;

            try {
                ListSelectionModel lsm = connectionTable.getSelectionModel();

                rc = lsm.getLeadSelectionIndex();
                if ( rc == 0 ) {
                    rc = connections.size();
                }
                Connection conn = (Connection) connections.get( rc - 1 );

                if ( rc > 0 ) {
                    lsm.clearSelection();
                    lsm.setSelectionInterval(0, 0);
                }

                InputStream in = null ;
                String      text = conn.inputText.getText();

                // Fix Content-Length HTTP headers
                if ( text.startsWith("POST ") || text.startsWith("GET ") ) {
                    // System.err.println("IN CL" );
                    int         pos1, pos2, pos3 ;
                    String      body, headers, headers1, header2 ;

                    pos3 = text.indexOf( "\n\n" );
                    if ( pos3 == -1 ) {
                        pos3 = text.indexOf( "\r\n\r\n" );
                        if ( pos3 != -1 ) {
                            pos3 = pos3 + 4 ;
                        }
                    }
                    else {
                        pos3 += 2 ;
                    }

                    headers = text.substring( 0, pos3 );

                    pos1 = headers.indexOf( "Content-Length:" );
                    // System.err.println("pos1: " + pos1 );
                    // System.err.println("pos3: " + pos3 );
                    if ( pos1 != -1 ) {
                        int  newLen = text.length() - pos3 ;

                        pos2 = headers.indexOf( "\n", pos1 );

                        System.err.println("CL: " + newLen );
                        System.err.println("Hdrs: '" + headers + "'" );
                        System.err.println("subTEXT: '" +
                            text.substring(pos3, pos3 + newLen) + "'");
                        text = headers.substring(0, pos1) +
                        "Content-Length: " + newLen + "\n" +
                        headers.substring(pos2 + 1) +
                        text.substring(pos3) ;
                        System.err.println("\nTEXT: '" + text + "'" );
                    }
                }

                in = new ByteArrayInputStream( text.getBytes() );
                new Connection( this, in );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    public tcpmon(int listenPort, String targetHost, int targetPort, boolean embedded) {
        super ( getMessage("tcpmon00", "TCPMonitor") );

        notebook = new JTabbedPane();
        this.getContentPane().add( notebook );

        new AdminPage( notebook, getMessage("admin00", "Admin") );

        if ( listenPort != 0 ) {
            Listener l = null ;

            if ( targetHost == null ) {
                l = new Listener( notebook, null, listenPort,
                    targetHost, targetPort, true, null);
            } else {
                l = new Listener( notebook, null, listenPort,
                    targetHost, targetPort, false, null);
            }
            notebook.setSelectedIndex( 1 );

            l.HTTPProxyHost = System.getProperty( "http.proxyHost" );
            if ( l.HTTPProxyHost != null && l.HTTPProxyHost.equals("") ) {
                l.HTTPProxyHost = null ;
            }

            if ( l.HTTPProxyHost != null ) {
                String tmp = System.getProperty( "http.proxyPort" );

                if ( tmp != null && tmp.equals("") ) {
                    tmp = null ;
                }
                if ( tmp == null ) {
                    l.HTTPProxyPort = 80 ;
                } else {
                    l.HTTPProxyPort = Integer.parseInt( tmp );
                }
            }
        }
        
        if(!embedded) {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        this.pack();
        this.setSize( 600, 600 );
        this.setVisible( true );
    }
    
    public tcpmon(int listenPort, String targetHost, int targetPort) {
        this(listenPort, targetHost, targetPort, false);
    }

    /**
     * set up the L&F
     */
    private static void setupLookAndFeel(boolean nativeLookAndFeel) throws Exception {
        String classname= UIManager.getCrossPlatformLookAndFeelClassName();
        if(nativeLookAndFeel) {
            classname= UIManager.getSystemLookAndFeelClassName();
        }
        String lafProperty= System.getProperty("tcpmon.laf", "");
        if(lafProperty.length()>0) {
            classname=lafProperty;
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
     * @param args
     */
    public static void main(String[] args) {
        try {
            //switch between swing L&F here
            setupLookAndFeel(true);
            if ( args.length == 3 ) {
                int p1 = Integer.parseInt( args[0] );
                int p2 = Integer.parseInt( args[2] );

                new tcpmon( p1, args[1], p2 );
            }
            else if ( args.length == 1 ) {
                int p1 = Integer.parseInt( args[0] );

                new tcpmon( p1, null, 0 );
            }
            else if ( args.length != 0 ) {
                System.err.println( getMessage("usage00", "Usage:")
                        + " tcpmon [listenPort targetHost targetPort]\n");
            }
            else {
                new tcpmon(0, null, 0);
            }
        }
        catch ( Throwable exp ) {
            exp.printStackTrace();
        }
    }

    // Message resource bundle.
    private static ResourceBundle messages = null;

    /**
     * Get the message with the given key.  There are no arguments for this message.
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
    } // getMessage

    /**
     * Load the resource bundle messages from the properties file.  This is ONLY done when it is
     * needed.  If no messages are printed (for example, only Wsdl2java is being run in non-
     * verbose mode) then there is no need to read the properties file.
     */
    private static void initializeMessages() {
        messages = ResourceBundle.getBundle("org.apache.axis.utils.tcpmon");
    } // initializeMessages

    /**
     * a text field with a restricted set of characters
     */
    static class RestrictedTextField extends JTextField {
        protected String validText;

        public RestrictedTextField(String validText) {
            setValidText(validText);
        }

        public RestrictedTextField(int columns, String validText) {
            super(columns);
            setValidText(validText);
        }

        public RestrictedTextField(String text, String validText) {
            super(text);
            setValidText(validText);
        }

        public RestrictedTextField(String text, int columns, String validText) {
            super(text, columns);
            setValidText(validText);
        }

        private void setValidText(String validText) {
            this.validText = validText;
        }

        /**
         * fascinatingly, this method is called in the super() constructor,
         * meaning before we are fully initialized. C++ doesnt actually permit
         * such a situation, but java clearly does...
         * @return a new document
         */
        public Document createDefaultModel() {
            return new RestrictedDocument();
        }

        /**
         * this class strips out invaid chars
         */
        class RestrictedDocument extends PlainDocument {


            /**
             * Constructs a plain text document.  A default model using
             * <code>GapContent</code> is constructed and set.
             */
            public RestrictedDocument() {

            }

            /**
             * add a string; only those chars in the valid text list are allowed
             * @param offset
             * @param string
             * @param attributes
             * @throws BadLocationException
             */
            public void insertString(int offset, String string, AttributeSet attributes)
                    throws BadLocationException {

                if (string == null) {
                    return;
                }
                int len = string.length();
                StringBuffer buffer = new StringBuffer(string.length());
                for (int i = 0; i < len; i++) {
                    char ch = string.charAt(i);
                    if (validText.indexOf(ch) >= 0) {
                        buffer.append(ch);
                    }
                }
                super.insertString(offset, new String(buffer), attributes);
            }
        } //end class NumericDocument
    }

    /**
     * because we cant use Java1.4's JFormattedTextField, here is
     * a class that accepts numbers only
     */
    static class NumberField extends RestrictedTextField {

        private static final String VALID_TEXT = "0123456789";

        /**
         * Constructs a new <code>TextField</code>.  A default model is created,
         * the initial string is <code>null</code>,
         * and the number of columns is set to 0.
         */
        public NumberField() {
            super(VALID_TEXT);
        }

        /**
         * Constructs a new empty <code>TextField</code> with the specified
         * number of columns.
         * A default model is created and the initial string is set to
         * <code>null</code>.
         *
         * @param columns  the number of columns to use to calculate
         *   the preferred width; if columns is set to zero, the
         *   preferred width will be whatever naturally results from
         *   the component implementation
         */
        public NumberField(int columns) {
            super(columns, VALID_TEXT);
        }


        /**
         * get the int value of a field, any invalid (non int) field returns
         * the default
         * @param def default value
         * @return the field contents
         */
        public int getValue(int def) {
            int result = def;
            String text = getText();
            if (text != null && text.length() != 0) {
                try {
                    result = Integer.parseInt(text);
                } catch (NumberFormatException e) {

                }
            }
            return result;
        }

        /**
         * set the text to a numeric value
         * @param value number to assign
         */
        public void setValue(int value) {
            setText(Integer.toString(value));
        }

    } //end class NumericTextField

    /**
     * hostname fields
     */
    static class HostnameField extends RestrictedTextField {
        //list of valid chars in a hostname
        private static final String VALID_TEXT =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-.";

        public HostnameField(int columns) {
            super(columns, VALID_TEXT);
        }

        public HostnameField() {
            super(VALID_TEXT);
        }
    }

}
