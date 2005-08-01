package org.apache.axis2.transport.mail.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.AxisFault;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Chamil Thanthrimudalige
 */

public class POP3Server extends Thread {
    protected static Log log = LogFactory.getLog(POP3Server.class.getName());
    private ServerSocket serverSocket;
    private Storage st = null;
    private boolean running = false;

    public POP3Server(Storage st, int port) throws AxisFault {
        this.st = st;
        try {
            synchronized (this) {
                running = true;
                serverSocket = new ServerSocket(port);
                log.info("Server started on port " + port);
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                POP3Worker thread = new POP3Worker(socket, st);
                thread.start();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void stopServer() throws AxisFault {
        try {
            synchronized (this) {
                running = false;
                serverSocket.close();
            }

        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }
}