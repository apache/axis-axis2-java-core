package org.apache.axis2.jaxws.message.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.DescriptionUtils;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.SoapUtils;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;

public class XMLFaultConvertor {

	/**
	 * createXMLFault create an XMLFault object from a SOAPFault and the detailblocks
	 * @param soapfault
	 * @param detailblocks
	 * @return
	 */
	public static XMLFault createXMLFault(SOAPFault soapfault, List<Block> detailblocks) {
		QName faultcode = soapfault.getCode().getValue().getTextAsQName();
		String reason = soapfault.getReason().getFirstElement().getText();

		return new XMLFaultImpl(faultcode, reason, detailblocks.toArray(new Block[0]), null);
	}

	
    /**
     * Make an XMLFaultImpl based on a passed Exception.  If the Exception is an
     * InvocationTargetException (which already wraps another Exception), get the
     * wrapped Exception out from there and use that instead of the passed one.
     * @param e an exception
     * @return a XMLFaultImpl object
     */
	public static XMLFault createXMLFault(Throwable throwable, String actor, Block[] detailBlocks, Protocol proto) {

        // TODO right qname?  We should probably receive it from the caller in an additional method param
        QName faultCode = new QName(Constants.ELEM_FAULT_CODE, Constants.FAULT_SERVER_GENERAL);

        // TODO right faultString initialization?
        String faultString = throwable.getMessage();
        if (faultString == null) {
            faultString = throwable.toString(); 
        }

        return new XMLFaultImpl(faultCode, faultString, detailBlocks, throwable);
	}
	
    public static OMElement toOMElement(XMLFault xmlfault) throws MessageException {
    	// TODO assume soap11 for testing
    	try {
    		return toSOAPEnvelope(xmlfault, SoapUtils.getSoapFactory(null));
    	} catch (XMLStreamException e) {
    		// TODO I'm throwing MessageException because that's what XMLPart caller method throws
    		throw ExceptionFactory.makeMessageException(e.toString());
    	}
    }
    
    private static SOAPEnvelope toSOAPEnvelope(XMLFault xmlfault, SOAPFactory soapfact) throws XMLStreamException, MessageException {
    	SOAPEnvelope env = soapfact.createSOAPEnvelope();
    	SOAPBody body = soapfact.createSOAPBody(env);
    	body.addFault(toSOAPFault(xmlfault, soapfact));
    	return env;
    }
    
    private static SOAPFault toSOAPFault(XMLFault xmlfault, SOAPFactory soapfact) throws XMLStreamException, MessageException {
		// TODO I'm not fully sure if this while method is correct or complete

    	SOAPFault soapfault = soapfact.createSOAPFault();

		SOAPFaultReason soapreason = soapfact.createSOAPFaultReason(soapfault);
		soapreason.setText(xmlfault.getString());
		SOAPFaultText soaptext = soapfact.createSOAPFaultText(soapreason);
		soaptext.setText(xmlfault.getString());

		SOAPFaultCode soapcode = soapfact.createSOAPFaultCode(soapfault);

		soapcode.setText(xmlfault.getCode());

		SOAPFaultValue soapvalue = soapfact.createSOAPFaultValue(soapcode);
		soapvalue.setText(xmlfault.getCode());
		soapcode.setValue(soapvalue);
		
		SOAPFaultDetail soapdetail = soapfact.createSOAPFaultDetail(soapfault);
		Block[] dblocks = xmlfault.getDetailBlocks();
		for (int i = 0; (dblocks != null) && (i < dblocks.length); i++)
			soapdetail.addDetailEntry(dblocks[i].getOMElement());

		return soapfault;

    }
    
    /*
     * used by MethodMarshallerImpl, hence public
     */
    public static Throwable getRootCause(Throwable e) {
		Throwable t = null;

		if (e != null) {
			if (e instanceof InvocationTargetException) {
				t = ((InvocationTargetException) e).getTargetException();
			} else {
				t = null;
			}

			if (t != null) {
				e = getRootCause(t);
			}
		}
		return e;
    }


}
