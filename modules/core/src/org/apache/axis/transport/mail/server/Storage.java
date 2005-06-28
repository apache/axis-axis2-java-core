package org.apache.axis.transport.mail.server;

/**
 * Inmemory storage to used to store the mails.
 * @author Chamil Thanthrimudalige.
 */

import java.util.ArrayList;
import java.util.Hashtable;

import javax.mail.internet.MimeMessage;

public class Storage {
	private ArrayList mails = new ArrayList();
	
	private Hashtable users = new Hashtable(); 
	
	
	public Storage(){
	}

	public void addMail(String user, MimeMessage mail){
		ArrayList curMailBox = null;
		if (users.containsKey(user)){
			curMailBox = (ArrayList)users.get(user);
		} else {
			curMailBox = new ArrayList();
			users.put(user, curMailBox);
		}
		curMailBox.add(mail);
	}

	public ArrayList popUserMails(String user){
		ArrayList usrMailBox = new ArrayList(); // This will return a emty list when the user has no mails or no mail box.
		if (users.containsKey(user)){
			usrMailBox = (ArrayList)users.get(user);
		}
		return usrMailBox;
	}
}