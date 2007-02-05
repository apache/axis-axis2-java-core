/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.apache.axis2.transport;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.OMBuilder;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.transport.http.TransportHeaders;
import org.apache.axis2.util.Builder;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Loader;

public class TransportUtils {

    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
			String soapNamespaceURI) throws AxisFault {
		try {
			InputStream inStream = (InputStream) msgContext
					.getProperty(MessageContext.TRANSPORT_IN);

			msgContext.setProperty(MessageContext.TRANSPORT_IN, null);

			// this inputstram is set by the TransportSender represents a two
			// way transport or by a Transport Recevier
			if (inStream == null) {
				throw new AxisFault(Messages.getMessage("inputstreamNull"));
			}
			Object contentTypeObject;
			boolean isMIME = false;
			
			contentTypeObject = msgContext.getProperty(HTTPConstants.CONTENT_TYPE);

			String contentType=null;
			if(contentTypeObject!=null){
				contentType =(String) contentTypeObject;
				if (JavaUtils.indexOfIgnoreCase(contentType,
						HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {
					isMIME = true;
				}
			}

			String charSetEnc = (String) msgContext
					.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
			if (charSetEnc == null) {
				charSetEnc = (String) msgContext
						.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
			}
			if (charSetEnc == null) {
				charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
			}
			return createSOAPMessage(msgContext, inStream, soapNamespaceURI,
					isMIME, (String) contentType, charSetEnc);
		} catch (AxisFault e) {
			throw e;
		} catch (OMException e) {
			throw new AxisFault(e);
		} catch (XMLStreamException e) {
			throw new AxisFault(e);
		} catch (FactoryConfigurationError e) {
			throw new AxisFault(e);
		}
//    	boolean isMIME=false;
//		try {
//			InputStream inStream = (InputStream) msgContext
//					.getProperty(MessageContext.TRANSPORT_IN);
//
//			msgContext.setProperty(MessageContext.TRANSPORT_IN, null);
//
//			// this inputstram is set by the TransportSender represents a two
//			// way transport or by a Transport Recevier
//			if (inStream == null) {
//				throw new AxisFault(Messages.getMessage("inputstreamNull"));
//			}
//			
//			Map transportHeaders = (Map)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
//			
//			// This causes the transport headers to be initialized. We've been
//			// anyway iterating through the headers. So this is not bad
//			// for the moment.But there is a possibility to improve.
//			Object contentTypeObject = transportHeaders.get(HTTPConstants.CONTENT_TYPE);
//			if (contentTypeObject==null)
//			{
//				contentTypeObject = transportHeaders.get(HTTPConstants.CONTENT_TYPE.toLowerCase());
//			}
//			String contentType;
//			if(contentTypeObject!=null)
//			  contentType =(String) contentTypeObject;
//			else
//				throw new AxisFault("Content Type Cannot be null");
//			
//			if (JavaUtils.indexOfIgnoreCase(contentType,
//					HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {
//				// It is MIME (MTOM or SwA)
//				isMIME = true;
//			}
//			
//            // get the type of char encoding &  setting the value in msgCtx
//            String charSetEnc = Builder.getCharSetEncoding(contentType);
//            if(charSetEnc == null){
//                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
//            }
//            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
//            
//            String soapNS = null;
//            if (contentType != null) {
//    			if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
//    				soapNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
//    			} else if (contentType
//    					.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
//    				soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
//    			}
////    			if (JavaUtils.indexOfIgnoreCase(contentType,
////    					HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {
////    				// It is MIME (MTOM or SwA)
////    				isMIME = true;
////    			}
////    			else if (soapVersion == VERSION_SOAP11) {
////    				// Deployment configuration parameter
////    				Parameter enableREST = msgContext
////    						.getParameter(Constants.Configuration.ENABLE_REST);
////    				if ((msgContext.getSoapAction() == null) && (enableREST != null)) {
////    					if (Constants.VALUE_TRUE.equals(enableREST.getValue())) {
////    						// If the content Type is text/xml (BTW which is the
////    						// SOAP 1.1 Content type ) and the SOAP Action is
////    						// absent it is rest !!
////    						msgContext.setDoingREST(true);
////    					}
////    				}
////    			}
//    		}
//
//			return createSOAPMessage(msgContext, inStream, soapNS,
//					isMIME, (String) contentType, charSetEnc);
//		} catch (AxisFault e) {
//			throw e;
//		} catch (OMException e) {
//			throw new AxisFault(e);
//		} catch (XMLStreamException e) {
//			throw new AxisFault(e);
//		} catch (FactoryConfigurationError e) {
//			throw new AxisFault(e);
//		}
	}

    /**
	 * Objective of this method is to capture the SOAPEnvelope creation logic
	 * and make it a common for all the transports and to in/out flows.
	 * 
	 * @param msgContext
	 * @param inStream
	 * @param soapNamespaceURI
	 * @param isMIME
	 * @param contentType
	 * @param charSetEnc
	 * @return the SOAPEnvelope
	 * @throws AxisFault
	 * @throws OMException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
			InputStream inStream, String soapNamespaceURI, boolean isMIME,
			String contentType, String charSetEnc) throws AxisFault,
			OMException, XMLStreamException, FactoryConfigurationError {
    	OMBuilder builder=null;
		OMElement documentElement;
		if (isMIME) {
			builder = Builder.getAttachmentsBuilder(
					msgContext, inStream, (String) contentType, !(msgContext
							.isDoingREST()));
		} else if (msgContext.isDoingREST()) {
			builder = Builder.getPOXBuilder(inStream,
					charSetEnc, soapNamespaceURI);
//		} else if (soapNamespaceURI!=null){
//				builder = Builder.getBuilder(inStream, charSetEnc,soapNamespaceURI);
		}else if (contentType!=null)
		{
			builder = Builder.getBuilderFromSelector(contentType, inStream, msgContext);
		}
		if (builder==null)
		{
			//FIXME making soap defualt for the moment..might effect the performance
			builder = Builder.getBuilder(inStream, charSetEnc,soapNamespaceURI);
//			throw new AxisFault("Cannot find a matching builder for the message. Unsupported Content Type.");
		}
		
		documentElement = builder.getDocumentElement();
		SOAPEnvelope envelope;
		//Check whether we have received a SOAPEnvelope or not
		if (documentElement instanceof SOAPEnvelope) {
			envelope = (SOAPEnvelope) documentElement;
		} else {
			//If it is not a SOAPEnvelope we wrap that with a fake SOAPEnvelope.
			SOAPFactory soapFactory = new SOAP11Factory();
			SOAPEnvelope intermediateEnvelope = soapFactory
					.getDefaultEnvelope();
			intermediateEnvelope.getBody().addChild(
					builder.getDocumentElement());

			// We now have the message inside an envelope. However, this is
			// only an OM; We need to build a SOAP model from it.
			builder = new StAXSOAPModelBuilder(intermediateEnvelope
					.getXMLStreamReader(), SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
			envelope = (SOAPEnvelope) builder.getDocumentElement();
		}

		String charsetEncoding = builder.getCharsetEncoding();
		if ((charsetEncoding != null)
				&& !"".equals(charsetEncoding)
				&& (msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING) != null)
				&& !charsetEncoding.equalsIgnoreCase((String) msgContext
								.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING))) {
			String faultCode;

            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                    envelope.getNamespace().getNamespaceURI())) {
                faultCode = SOAP12Constants.FAULT_CODE_SENDER;
            } else {
                faultCode = SOAP11Constants.FAULT_CODE_SENDER;
            }

            throw new AxisFault(
                    "Character Set Encoding from " + "transport information do not match with "
                    + "character set encoding in the received SOAP message", faultCode);
        }
		return envelope;
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
    
    public static void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if ((envelope != null) && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();

                // Pick the char set encoding from the msgContext
                String charSetEnc =
                        (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

                format.setDoOptimize(false);
                format.setDoingSWA(false);
                format.setCharSetEncoding(charSetEnc);
                outputMessage.serializeAndConsume(out, format);
                out.flush();
            } catch (Exception e) {
                throw new AxisFault(e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("outMessageNull"));
        }
    }
    
    /**
     * Initial work for a builder selector which selects the builder for a given message format based on the the content type of the recieved message.
     * content-type to builder mapping can be specified through the Axis2.xml.
     * @param msgContext
     * @return the builder registered against the given content-type
     * @throws AxisFault
     */
    public static MessageFormatter getMessageFormatter(MessageContext msgContext) 
    				throws AxisFault {
		MessageFormatter messageFormatter = null;
		String messageFormatString = getMessageFormatterProperty(msgContext);
		if (messageFormatString != null) {
			messageFormatter = msgContext.getConfigurationContext()
					.getAxisConfiguration().getMessageFormatter(messageFormatString);
			
			}
		if (messageFormatter == null) {
			// Lets default to SOAP formatter
			//TODO need to improve this to use the stateless nature
			messageFormatter = new SOAPMessageFormatter();
		}
		return messageFormatter;
	}
    
    private static String getMessageFormatterProperty(MessageContext msgContext) {
		String messageFormatterProperty = null;
		Object property = msgContext
				.getProperty(Constants.Configuration.MESSAGE_TYPE);
		if (property != null) {
			messageFormatterProperty = (String) property;
		}
		if (messageFormatterProperty == null) {
			Parameter parameter = msgContext
					.getParameter(Constants.Configuration.MESSAGE_TYPE);
			if (parameter != null) {
				messageFormatterProperty = (String) parameter.getValue();
			}
		}
		return messageFormatterProperty;
    }
}
