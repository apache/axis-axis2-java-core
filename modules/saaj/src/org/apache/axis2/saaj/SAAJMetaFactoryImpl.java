package org.apache.axis2.saaj;

import javax.xml.soap.SAAJMetaFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

public class SAAJMetaFactoryImpl extends SAAJMetaFactory {
    protected MessageFactory newMessageFactory(String soapVersion) throws SOAPException {
    	if(!(SOAPConstants.SOAP_1_1_PROTOCOL.equals(soapVersion) ||
    			SOAPConstants.SOAP_1_2_PROTOCOL.equals(soapVersion) ||
    				SOAPConstants.DYNAMIC_SOAP_PROTOCOL.equals(soapVersion))){
    		throw new SOAPException("Invalid SOAP Protocol Version");
    	}    	
        MessageFactoryImpl factory = new  MessageFactoryImpl();
        factory.setSOAPVersion(soapVersion);
        return factory;
    }

    protected SOAPFactory newSOAPFactory(String soapVersion) throws SOAPException {
    	if(!(SOAPConstants.SOAP_1_1_PROTOCOL.equals(soapVersion) ||
    			SOAPConstants.SOAP_1_2_PROTOCOL.equals(soapVersion) ||
    				SOAPConstants.DYNAMIC_SOAP_PROTOCOL.equals(soapVersion))){
    		throw new SOAPException("Invalid SOAP Protocol Version");
    	}    	
        SOAPFactoryImpl factory = new SOAPFactoryImpl();
        factory.setSOAPVersion(soapVersion);
        return factory;
    }
}
