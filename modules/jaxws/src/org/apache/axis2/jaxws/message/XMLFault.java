package org.apache.axis2.jaxws.message;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPFault;

public interface XMLFault {

	public void setCode(QName code);
	
    public QName getCode();

    public void setString(String str);
    
    public String getString();

    public void setDetailBlocks(Block[] blocks);
    
    public Block[] getDetailBlocks();
	
/*
 * TODO
 * possibly properties to hold the other less common things like role/actor, node, etc.
 * A getter and setter to hold the Block that represents the Detail content).
*/
    
}
