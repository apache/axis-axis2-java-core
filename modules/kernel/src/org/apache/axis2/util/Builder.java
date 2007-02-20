package org.apache.axis2.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.builder.OMBuilder;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.builder.XOPAwareStAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;

public class Builder {
    public static final int BOM_SIZE = 4;

    public static StAXBuilder getPOXBuilder(InputStream inStream, String charSetEnc, String soapNamespaceURI) throws XMLStreamException {
        StAXBuilder builder;
        XMLStreamReader xmlreader =
                StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        builder = new StAXOMBuilder(xmlreader);
        return builder;
    }

    /**
	 * Use the BOM Mark to identify the encoding to be used. Fall back to
	 * default encoding specified
	 *
	 * @param is
	 * @param charSetEncoding
	 * @throws java.io.IOException
	 */
    public static Reader getReader(InputStream is, String charSetEncoding) throws IOException {
        PushbackInputStream is2 = new PushbackInputStream(is, BOM_SIZE);
        String encoding;
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;

        n = is2.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE)
                && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00)
                && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {

            // Unicode BOM mark not found, unread all bytes
            encoding = charSetEncoding;
            unread = n;
        }

        if (unread > 0) {
            is2.unread(bom, (n - unread), unread);
        }

        return new BufferedReader(new InputStreamReader(is2, encoding));
    }


    public static String getEnvelopeNamespace(String contentType) {
        String soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        if(contentType != null) {
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                // it is SOAP 1.2
                soapNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                // SOAP 1.1
                soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }
        }
        return soapNS;
    }

    /**
     * Extracts and returns the character set encoding from the
     * Content-type header
     * Example:
     * Content-Type: text/xml; charset=utf-8
     *
     * @param contentType
     */
    public static String getCharSetEncoding(String contentType) {
        if (contentType == null) {
            // Using the default UTF-8
            return MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        int index = contentType.indexOf(HTTPConstants.CHAR_SET_ENCODING);

        if (index == -1) {    // Charset encoding not found in the content-type header
            // Using the default UTF-8
            return MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        // If there are spaces around the '=' sign
        int indexOfEq = contentType.indexOf("=", index);

        // There can be situations where "charset" is not the last parameter of the Content-Type header
        int indexOfSemiColon = contentType.indexOf(";", indexOfEq);
        String value;

        if (indexOfSemiColon > 0) {
            value = (contentType.substring(indexOfEq + 1, indexOfSemiColon));
        } else {
            value = (contentType.substring(indexOfEq + 1, contentType.length())).trim();
        }

        // There might be "" around the value - if so remove them
        if(value.indexOf('\"')!=-1){
            value = value.replaceAll("\"", "");
        }

        return value.trim();
    }

    public static StAXBuilder getAttachmentsBuilder(MessageContext msgContext,
			InputStream inStream, String contentTypeString, boolean isSOAP)
			throws OMException, XMLStreamException, FactoryConfigurationError {
		StAXBuilder builder = null;
		XMLStreamReader streamReader;

        Attachments attachments = createAttachmentsMap(msgContext, inStream, contentTypeString);
		String charSetEncoding = getCharSetEncoding(attachments.getSOAPPartContentType());

		if ((charSetEncoding == null)
				|| "null".equalsIgnoreCase(charSetEncoding)) {
			charSetEncoding = MessageContext.UTF_8;
		}
		msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
				charSetEncoding);

		try {
			streamReader = StAXUtils.createXMLStreamReader(getReader(
					attachments.getSOAPPartInputStream(), charSetEncoding));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}

		
		//  Put a reference to Attachments Map in to the message context For
		// backword compatibility with Axis2 1.0 
		msgContext.setProperty(MTOMConstants.ATTACHMENTS, attachments);

		// Setting the Attachments map to new SwA API
		msgContext.setAttachmentMap(attachments);

		String soapEnvelopeNamespaceURI = getEnvelopeNamespace(contentTypeString);

		if (isSOAP) {
			if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.MTOM_TYPE)) {
				//Creates the MTOM specific MTOMStAXSOAPModelBuilder
				builder = new MTOMStAXSOAPModelBuilder(streamReader,
						attachments, soapEnvelopeNamespaceURI);
				msgContext.setDoingMTOM(true);
			} else if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.SWA_TYPE)) {
				builder = new StAXSOAPModelBuilder(streamReader,
						soapEnvelopeNamespaceURI);
			} else if (attachments.getAttachmentSpecType().equals(
                    MTOMConstants.SWA_TYPE_12) ) {
                builder = new StAXSOAPModelBuilder(streamReader,
                        soapEnvelopeNamespaceURI);
            }

		}
		// To handle REST XOP case
		else {
			if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.MTOM_TYPE)) {
				XOPAwareStAXOMBuilder stAXOMBuilder = new XOPAwareStAXOMBuilder(
						streamReader, attachments);
				builder = stAXOMBuilder;

			} else if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.SWA_TYPE)) {
				builder = new StAXOMBuilder(streamReader);
			} else if (attachments.getAttachmentSpecType().equals(
                    MTOMConstants.SWA_TYPE_12) ) {
                builder = new StAXOMBuilder(streamReader);
            }
		}

		return builder;
	}

    private static Attachments createAttachmentsMap(MessageContext msgContext, InputStream inStream, String contentTypeString) {
        Object cacheAttachmentProperty = msgContext
                .getProperty(Constants.Configuration.CACHE_ATTACHMENTS);
        String cacheAttachmentString = null;
        boolean fileCacheForAttachments;

        if (cacheAttachmentProperty != null
                && cacheAttachmentProperty instanceof String) {
            cacheAttachmentString = (String) cacheAttachmentProperty;
            fileCacheForAttachments = (Constants.VALUE_TRUE
                    .equals(cacheAttachmentString));
        } else {
            Parameter parameter_cache_attachment = msgContext
                    .getParameter(Constants.Configuration.CACHE_ATTACHMENTS);
            cacheAttachmentString = (parameter_cache_attachment != null) ? (String) parameter_cache_attachment
                    .getValue()
                    : null;
        }
        fileCacheForAttachments = (Constants.VALUE_TRUE
                .equals(cacheAttachmentString));

        String attachmentRepoDir = null;
        String attachmentSizeThreshold = null;

        if (fileCacheForAttachments) {
            Object attachmentRepoDirProperty = msgContext
                    .getProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR);

            if (attachmentRepoDirProperty != null) {
                attachmentRepoDir = (String) attachmentRepoDirProperty;
            } else {
                Parameter attachmentRepoDirParameter = msgContext
                        .getParameter(Constants.Configuration.ATTACHMENT_TEMP_DIR);
                attachmentRepoDir = (attachmentRepoDirParameter != null) ? (String) attachmentRepoDirParameter
                        .getValue()
                        : null;
            }

            Object attachmentSizeThresholdProperty = msgContext
                    .getProperty(Constants.Configuration.FILE_SIZE_THRESHOLD);
            if (attachmentSizeThresholdProperty != null
                    && attachmentSizeThresholdProperty instanceof String) {
                attachmentSizeThreshold = (String) attachmentSizeThresholdProperty;
            } else {
                Parameter attachmentSizeThresholdParameter = msgContext
                        .getParameter(Constants.Configuration.FILE_SIZE_THRESHOLD);
                attachmentSizeThreshold = attachmentSizeThresholdParameter
                        .getValue().toString();
            }
        }

        Attachments attachments = new Attachments(inStream, contentTypeString,
                fileCacheForAttachments, attachmentRepoDir,
                attachmentSizeThreshold);
        return attachments;
    }

    /**
     * @deprecated If some one really need this method, please shout.
     * 
     * @param in
     * @return
     * @throws XMLStreamException
     */
    public static StAXBuilder getBuilder(Reader in) throws XMLStreamException {
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in);
        StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader, null);
        return builder;
    }

    /**
     * Creates an OMBuilder for a plain XML message. Default character set encording is used.
     * 
     * @param inStream InputStream for a XML message
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static OMBuilder getBuilder(InputStream inStream) throws XMLStreamException {
    	XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream);
    	return new StAXOMBuilder(xmlReader);
    }
    
    /**
     * Creates an OMBuilder for a plain XML message.
     * 
     * @param inStream InputStream for a XML message
     * @param charSetEnc Character set encoding to be used
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static OMBuilder getBuilder(InputStream inStream, String charSetEnc) throws XMLStreamException {
    	XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
    	return new StAXOMBuilder(xmlReader);
    }
    
    /**
     * Creates an OMBuilder for a SOAP message. Default character set encording is used.
     * 
     * @param inStream InputStream for a SOAP message
     * @param soapNamespaceURI Specifies which SOAP version to use, 
     *              {@link SOAP11Constants#SOAP_11_CONTENT_TYPE} or 
     *              {@link SOAP12Constants#SOAP_12_CONTENT_TYPE}
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static OMBuilder getSOAPBuilder(InputStream inStream, String soapNamespaceURI) throws XMLStreamException {
    	XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(inStream);
        return new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
    }
    
    /**
     * Creates an OMBuilder for a SOAP message.
     * 
     * @param inStream InputStream for a SOAP message
     * @param charSetEnc Character set encoding to be used
     * @param soapNamespaceURI Specifies which SOAP version to use, 
     *              {@link SOAP11Constants#SOAP_11_CONTENT_TYPE} or 
     *              {@link SOAP12Constants#SOAP_12_CONTENT_TYPE}
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static OMBuilder getSOAPBuilder(InputStream inStream, String charSetEnc, String soapNamespaceURI) throws XMLStreamException {
       	XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        return new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
    }

    public static OMBuilder getBuilder(SOAPFactory soapFactory, InputStream in, String charSetEnc) throws XMLStreamException {
        StAXBuilder builder;
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in, charSetEnc);
        builder = new StAXOMBuilder(soapFactory, xmlreader);
        return builder;
    }
    
    /**
     * Initial work for a builder selector which selects the builder for a given message format based on the the content type of the recieved message.
     * content-type to builder mapping can be specified through the Axis2.xml.
     * @param contentType
     * @param msgContext
     * @return the builder registered against the given content-type
     * @throws AxisFault
     */
    public static OMBuilder getBuilderFromSelector(String contentType,
			InputStream inputStream, MessageContext msgContext,String charSetEncoding) throws AxisFault {
    	String type;
    	int index = contentType.indexOf(';');
		if (index>0)
    	{
    		type = contentType.substring(0,index);
    	}else{
    		type = contentType;
    	}
		Class builderClass = msgContext.getConfigurationContext()
				.getAxisConfiguration().getMessageBuilder(type);
		if (builderClass != null) {
			try {
				OMBuilder builder = (OMBuilder) builderClass.newInstance();
				builder.init(inputStream, charSetEncoding,msgContext.getTo().getAddress(), contentType);
				// Setting the received content-type as the messageType to make
				// sure that we respond using the received message serialisation
				// format.
				msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, type);
				return builder;
			} catch (InstantiationException e) {
				throw new AxisFault("Cannot instantiate the specified Builder Class  : "
								+ builderClass.getName() + ".", e);
			} catch (IllegalAccessException e) {
				throw new AxisFault("Cannot instantiate the specified Builder Class : "
								+ builderClass.getName() + ".", e);
			}
		}
		return null;
	}
}
