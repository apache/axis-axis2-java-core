/**
 * 
 */
package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockFactoryImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

/** Creates a SOAPEnvelopeBlock */
public class SOAPEnvelopeBlockFactoryImpl extends BlockFactoryImpl implements
        SOAPEnvelopeBlockFactory {

    /** Default Constructor required for Factory */
    public SOAPEnvelopeBlockFactoryImpl() {
        super();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.BlockFactory#createFrom(org.apache.axiom.om.OMElement, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(OMElement omElement, Object context, QName qName)
            throws XMLStreamException {
        return new SOAPEnvelopeBlockImpl(omElement, null, qName, this);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.factory.BlockFactory#createFrom(java.lang.Object, java.lang.Object, javax.xml.namespace.QName)
      */
    public Block createFrom(Object businessObject, Object context, QName qName)
            throws WebServiceException {
        return new SOAPEnvelopeBlockImpl((SOAPEnvelope)businessObject, null, qName, this);
    }

    public boolean isElement() {
        return true;
    }

}
