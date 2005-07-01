package org.apache.axis2.transport.mail.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * @author Chamil Thanthrimudalige
 */

public class POP3Server extends Thread {
    private ServerSocket serverSocket;
    private Storage st = null;

    
    public POP3Server(Storage st,int port) {
    	this.st = st;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                POP3Worker thread = new POP3Worker(socket, st);
                thread.start();
            } catch(Exception e) {
            	e.printStackTrace();
            }
        }
    }
}