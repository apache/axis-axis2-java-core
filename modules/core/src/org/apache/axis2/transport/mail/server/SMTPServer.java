package org.apache.axis2.transport.mail.server;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Chamil Thanthrimudalige
 * @author Chamikara Jayalath
 */

public class SMTPServer extends Thread {
    private Storage st;

    private ConfigurationContext configurationContext;

    private int port;

    private boolean actAsMailet = false;
    private ServerSocket ss;
    private boolean running = false;

    public SMTPServer(
        Storage st,
        ConfigurationContext configurationContext,
        int port) {
        this.st = st;
        this.configurationContext = configurationContext;
        this.port = port;
        actAsMailet = true;
    }

    public SMTPServer(Storage st, int port) {
        this.st = st;
        this.port = port;
        actAsMailet = false;
    }

    public void run() {
        runServer();
    }

    public void runServer() {

        try {
            synchronized (this) {
                running = true;
                ss = new ServerSocket(port);
                System.out.println("SMTP Server started on port " + port);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        while (running) {
            try {
                //wait for a client
                Socket socket = ss.accept();
                SMTPWorker thread = null;
                if (actAsMailet)
                    thread = new SMTPWorker(socket, st, configurationContext);
                else {
                    thread = new SMTPWorker(socket, st);
                }
                thread.start();

            } catch (IOException ex) {
                if (running) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void stopServer() throws AxisFault {
        try {
            synchronized (this) {
                running = false;
                ss.close();
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }
}
