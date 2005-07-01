package org.apache.axis.transport.mail.server;

import org.apache.axis.context.ConfigurationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author Chamil Thanthrimudalige
 * @author Chamikara Jayalath
 */

public class SMTPServer extends Thread{
	private Storage st;
    private ConfigurationContext configurationContext;
    private int port;
	public SMTPServer(Storage st,ConfigurationContext configurationContext,int port){
		this.st = st;
        this.configurationContext =configurationContext;
        this.port = port;
	}
	public void run(){
        runServer();
    }

    public void runServer() {
        ServerSocket ss = null;
        try {
        	ss = new ServerSocket(port);
        	System.out.println("SMTP Server started on port " + port);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        while (true) {
            try {
                //wait for a client
                Socket socket = ss.accept();
               
                SMTPWorker thread = new SMTPWorker(socket, st,configurationContext);
                thread.start();
            
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}