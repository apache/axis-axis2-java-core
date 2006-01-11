package org.apache.axis2.transport.mail.server;

import javax.mail.Address;

/**
 * This is a simple implementation to simplify the usage
 * of the Addresses.
 */
public class MailAddress extends Address {
	
    private static final long serialVersionUID = 3033256355495000819L;
    
	String mailAddy = null;

    public MailAddress(String mAddy) {
        this.mailAddy = mAddy;
    }

    public boolean equals(Object addr) {
        return this.mailAddy.equals(addr);
    }

    public String toString() {
        return this.mailAddy;
    }

    public String getType() {
        return "text/plain";
    }
}
