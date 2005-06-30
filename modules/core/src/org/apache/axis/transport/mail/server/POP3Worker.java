/**
 * @author Chamil Thanthrimudalige
 */
package org.apache.axis.transport.mail.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class POP3Worker extends Thread{
    private Socket socket;
    private Storage st;
    boolean doneProcess = false;
    int numDeleted = 0 ; //This is a small hack to get the deleting working with the ArrayList. To keep it simple.
    public POP3Worker(Socket socket, Storage st) {
    		this.socket = socket;
    		this.st = st;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println(MailConstants.OK+" POP3 server ready");
            String s;
            String ps; //ProcessedString
            while(!doneProcess) {
                s = bufferedReader.readLine();
                processInput(s, printWriter);
            }
            socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    ArrayList messages = new ArrayList();
    private void processInput(String input, PrintWriter printWriter){
        int listLen = (MailConstants.LIST+" ").length();
        byte[] CR_LF = new byte[] {0x0D, 0x0A};
        byte[] CR_LF_DOT_CR_LF = new byte[] { 0x0D, 0x0A, '.', 0x0D, 0x0A };
        String user = "";
        if(input==null) {
            this.doneProcess = true; // This should not be happening
        } else {
        	ArrayList tokens = new ArrayList();
        	StringTokenizer stk = new StringTokenizer(input);
        	while (stk.hasMoreTokens()) {
        	    tokens.add(stk.nextToken());
        	}
	        if(((String)tokens.get(0)).equals(MailConstants.USER)) {
	            user = (String)tokens.get(1);
	            messages = st.popUserMails(user);
	            printWriter.println(MailConstants.OK);
	        } else if(((String)tokens.get(0)).equals(MailConstants.PASS)) {
	            printWriter.println(MailConstants.OK); // Passwords are not checked.
	        } else if(input.equals(MailConstants.QUIT)) {
	            printWriter.println(MailConstants.OK + "POP3 server signing off");
	            doneProcess=true;
	        } else if(input.equals(MailConstants.STAT)) {
	            printWriter.println(MailConstants.OK + messages.size() + " 1"); // We take the maildrop size as one.
	        } else if(((String)tokens.get(0)).equals(MailConstants.LIST)) { // scan listing
	            if(tokens.size() > 1) {
	                try {
	                    int optArg = Integer.parseInt((String)tokens.get(1));
	                    int messageArrayIndex = optArg - 1;
	                    if((messageArrayIndex < messages.size()) && (messageArrayIndex >= 0)) { // that is OK careful with numbering
	                        printWriter.println(MailConstants.OK + messageArrayIndex + 1 + " 120"); // Mail size of 120 is just some number.
	                    } else {
	                        printWriter.println(MailConstants.ERR + "no such message, only " + (messages.size() + 1) + " messages in maildrop");
	                    }
	                } catch(NumberFormatException e) {
	                    e.printStackTrace();
	                    printWriter.println(MailConstants.ERR + "problem passing the index. Index submited was " + (String)tokens.get(1));
	                }
	            } else {
	                printWriter.println(MailConstants.OK+messages.size());
	                for(int i=0; i<messages.size(); i++) {
	                    int messageIndex = i+1;
	                    printWriter.println(messageIndex + " 120"); // List out all the messages with a message size octet of 120
	                }
	                printWriter.println(".");
	            }
	        } else if(((String)tokens.get(0)).equals(MailConstants.RETR)) {
	            String i = (String)tokens.get(1);
	            try {
	                int index = Integer.parseInt(i);
	                printWriter.println(MailConstants.OK);
	                MimeMessage m = (MimeMessage)messages.get(index-1);

	                m.writeTo(socket.getOutputStream());
	                //System.out.println("\n\n\n\n ========This is the mail========");
	                //m.writeTo(System.out);//socket.getOutputStream());
	                //System.out.println("\n\n\n\n ========This is the mail========");
	                
	                socket.getOutputStream().write(CR_LF_DOT_CR_LF);// This is a bit of a hack to get it working. Have to find a bette way to handle this.
	                socket.getOutputStream().flush();
	            } catch(NumberFormatException e) {
	                printWriter.println(MailConstants.ERR);
	            } catch (IOException e1) {
	                printWriter.println(MailConstants.ERR);
	            } catch (MessagingException e2) {
	                printWriter.println(MailConstants.ERR);
	            }
	        } else if(((String)tokens.get(0)).equals(MailConstants.DELE)) {
	            String smIndex = (String)tokens.get(1);
	            try {
	                int mIndex = Integer.parseInt(smIndex)-1 - numDeleted; // When one mail is deleted the index of the other mails will reduce. Asumed that the delete will occure from bottom up.
	                if((mIndex >= 0) && (mIndex < messages.size())) {
	                    messages.remove(mIndex);
	                    numDeleted++;
	                    printWriter.println(MailConstants.OK);
	                } else {
	                    printWriter.println(MailConstants.ERR);
	                }
	            } catch(NumberFormatException e) {
	                printWriter.println(MailConstants.ERR);
	            }
	        } else if(((String)tokens.get(0)).equals(MailConstants.NOOP) || ((String)tokens.get(0)).equals(MailConstants.RSET)) {
	            printWriter.println(MailConstants.OK);
	        } else {
	            printWriter.println(MailConstants.ERR);
	        }
        }
    }
}
