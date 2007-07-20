package org.apache.axis2.fastinfoset;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

public class FastInfosetPOXMessageFormatter implements MessageFormatter {

	private Log logger = LogFactory.getLog(FastInfosetMessageFormatter.class);
	
	/**
	 * Plain Fast Infoset message formatter doesn't need to handle SOAP. Hence do nothing.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#formatSOAPAction(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.lang.String)
	 */
	public String formatSOAPAction(MessageContext messageContext,
			OMOutputFormat format, String soapAction) {

		return null;
	}

	/**
	 * Retrieves the raw bytes from the SOAP envelop.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getBytes(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat)
	 */
	public byte[] getBytes(MessageContext messageContext, OMOutputFormat format)
			throws AxisFault {
		OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		try {
			//Creates StAX document serializer which actually implements the XMLStreamWriter
			XMLStreamWriter streamWriter = new StAXDocumentSerializer(outStream);
			//streamWriter.writeStartDocument();
			element.serializeAndConsume(streamWriter);
			//TODO Looks like the SOAP envelop doesn't have a end document tag. Find out why?
			streamWriter.writeEndDocument();
			
			return outStream.toByteArray();
			
		} catch (XMLStreamException xmlse) {
			logger.error(xmlse.getMessage());
			throw new AxisFault(xmlse.getMessage(), xmlse);
		}
	}

	/**
	 * Returns the content type
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getContentType(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.lang.String)
	 */
	public String getContentType(MessageContext messageContext,
			OMOutputFormat format, String soapAction) {
		String contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
		String encoding = format.getCharSetEncoding();
		
		//FIXME Is this a right thing to do? Need to clarify with a vetarant
		if (contentType == null) {
			contentType = (String) messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
		}

		if (encoding != null) {
			contentType += "; charset=" + encoding;
		}
	        
		return contentType;
	}

	/**
	 * Returns the target address to send the response
	 * FIXME This is very HTTP specific. What about other transport?
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getTargetAddress(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.net.URL)
	 */
	public URL getTargetAddress(MessageContext messageContext,
			OMOutputFormat format, URL targetURL) throws AxisFault {
        String httpMethod =
            (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);

        URL targetAddress = targetURL; //Let's initialize to this
	    //if the http method is GET, parameters are attached to the target URL
	    if ((httpMethod != null)
	            && Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
	        String param = getParam(messageContext);
	
	        if (param.length() > 0) {
	            String returnURLFile = targetURL.getFile() + "?" + param;
	            try {
	                targetAddress = 
	                	new URL(targetURL.getProtocol(), targetURL.getHost(), targetURL.getPort(), returnURLFile);
	            } catch (MalformedURLException murle) {
	            	logger.error(murle.getMessage());
	                throw new AxisFault(murle.getMessage(), murle);
	            }
	        }
	    }
	    
	    return targetAddress;
	}

	/**
	 * Write the SOAP envelop to the given OutputStream.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#writeTo(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.io.OutputStream, boolean)
	 */
	public void writeTo(MessageContext messageContext, OMOutputFormat format,
			OutputStream outputStream, boolean preserve) throws AxisFault {
		
		OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
//        OMElement element = messageContext.getEnvelope();
		
		try {
			//Create the StAX document serializer
			XMLStreamWriter streamWriter = new StAXDocumentSerializer(outputStream);
			streamWriter.writeStartDocument();
			if (preserve) {
				element.serialize(streamWriter);
			} else {
				element.serializeAndConsume(streamWriter);
			}
//			TODO Looks like the SOAP envelop doesn't have a end document tag. Find out why?
			streamWriter.writeEndDocument();
		} catch (XMLStreamException xmlse) {
			logger.error(xmlse.getMessage());
			throw new AxisFault(xmlse.getMessage(), xmlse);
		}
	}
	
	/**
	 * Construct URL parameters like, "param1=value1&param2=value2"
	 * FIXME This is very HTTP specific. What about other transports
	 * 
	 * @param messageContext
	 * @return Formatted URL parameters
	 */
    private String getParam(MessageContext messageContext) {
        
    	OMElement dataOut = messageContext.getEnvelope().getBody().getFirstElement();
        Iterator it = dataOut.getChildElements();
        StringBuffer paramBuffer = new StringBuffer();
 
        while (it.hasNext()) {
            OMElement element = (OMElement) it.next();
            String parameter = element.getLocalName() + "=" + element.getText();
            paramBuffer.append(parameter);
            paramBuffer.append("&");
        }
        //We don't need a '&' at the end
        paramBuffer.deleteCharAt(paramBuffer.length() - 1);
        
        return paramBuffer.toString();
    }
}
