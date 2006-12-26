package org.apache.axis2.saaj;

import javax.xml.soap.SAAJMetaFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

public class SAAJMetaFactoryImpl extends SAAJMetaFactory {
    protected MessageFactory newMessageFactory(String s) throws SOAPException {
        MessageFactoryImpl factory = new  MessageFactoryImpl();
        factory.setSOAPVersion(s);
        return factory;
    }

    protected SOAPFactory newSOAPFactory(String s) throws SOAPException {
        SOAPFactoryImpl factory = new SOAPFactoryImpl();
        factory.setSOAPVersion(s);
        return factory;
    }
}
