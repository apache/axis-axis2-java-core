package org.apache.axis2.transport.mail.server;

import javax.mail.Address;

/**
 * This is a simple implementation to simplify the usage
 * of the Addresses.
 *
 * @author Chamil Thanthrimudalige
 */
public class MailAddress extends Address {
    String mailAddy = null;

    public MailAddress(String mAddy) {
        this.mailAddy = mAddy;
    }

    public boolean equals(Object addr) {
        return this.mailAddy.equals(addr);
    }

    public String getType() {
        return "text/plain";
    }

    public String toString() {
        return this.mailAddy;
    }
}