/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;

/**
 * Class SOAPConnectionFactoryImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class SOAPConnectionFactoryImpl extends SOAPConnectionFactory {

    /**
     * Create a new <CODE>SOAPConnection</CODE>.
     * @return the new <CODE>SOAPConnection</CODE> object.
     * @throws  SOAPException if there was an exception
     * creating the <CODE>SOAPConnection</CODE> object.
     */
	public SOAPConnection createConnection() throws SOAPException {
		
		return new SOAPConnectionImpl();
	}

}
