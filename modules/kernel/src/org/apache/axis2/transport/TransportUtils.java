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

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.OMBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.util.Builder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TransportUtils {

    private static final Log log = LogFactory.getLog(TransportUtils.class);
    
    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext) throws AxisFault {
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
            
            String soapNamespaceURI = getSOAPNamespaceFromContentType(contentType, null);

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
                    isMIME, contentType, charSetEnc);
        } catch (AxisFault e) {
            throw e;
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
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
        OMElement documentElement=null;
        String charsetEncoding=null;
        if (isMIME) {
            StAXBuilder builder = Builder.getAttachmentsBuilder(
                    msgContext, inStream, contentType, !(msgContext
                            .isDoingREST()));
            documentElement = builder.getDocumentElement();
            charsetEncoding = builder.getDocument().getCharsetEncoding();
        } else if (msgContext.isDoingREST()) {
            StAXBuilder builder = Builder.getPOXBuilder(inStream,
                    charSetEnc, soapNamespaceURI);
            documentElement = builder.getDocumentElement();
            charsetEncoding = builder.getDocument().getCharsetEncoding();
//        } else if (soapNamespaceURI!=null){
//                builder = Builder.getBuilder(inStream, charSetEnc,soapNamespaceURI);
        }else if (contentType!=null)
        {
            OMBuilder builder = Builder.getBuilderFromSelector(contentType, msgContext);
            if (builder != null) {
                documentElement = builder.processDocument(inStream, msgContext);
//                charsetEncoding = builder.getCharsetEncoding();
            }
        }
        if (documentElement==null)
        {
            //FIXME making soap defualt for the moment..might effect the performance
            StAXBuilder builder = Builder.getSOAPBuilder(inStream, charSetEnc,soapNamespaceURI);
            documentElement = builder.getDocumentElement();
            charsetEncoding = builder.getDocument().getCharsetEncoding();
//            throw new AxisFault("Cannot find a matching builder for the message. Unsupported Content Type.");
        }
        
        SOAPEnvelope envelope;
        //Check whether we have received a SOAPEnvelope or not
        if (documentElement instanceof SOAPEnvelope) {
            envelope = (SOAPEnvelope) documentElement;
        } else {
            //If it is not a SOAPEnvelope we wrap that with a fake SOAPEnvelope.
            SOAPFactory soapFactory = new SOAP11Factory();
            envelope= soapFactory.getDefaultEnvelope();
            envelope.getBody().addChild(documentElement);
        }

    /*    if ((charsetEncoding != null)
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
        }*/
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

            // If we are doing rest better default to Application/xml formatter
            if (msgContext.isDoingREST()) {
                messageFormatter = new ApplicationXMLFormatter();
            } else {
                // Lets default to SOAP formatter
                //TODO need to improve this to use the stateless nature
                messageFormatter = new SOAPMessageFormatter();
            }
        }
        return messageFormatter;
    }
    
    
    /**
     * @param contentType The contentType of the incoming message.  It may be null
     * @param defaultNamespace Usually set the version that is expected.  This a fallback if the contentType is unavailable or 
     * does not match our expectations
     * @return null or the soap namespace.  A null indicates that the message will be interpretted as a non-SOAP (i.e. REST) message 
     */
   private static String getSOAPNamespaceFromContentType(String contentType, String defaultSOAPNamespace) {
         
         String returnNS = defaultSOAPNamespace;
         // Discriminate using the content Type
         if (contentType != null) {
             
             /*
              * SOAP11 content-type is "text/xml"
              * SOAP12 content-type is "application/soap+xml"
              * 
              * What about other content-types?
              * 
              * TODO: I'm not fully convinced this method is complete, given the media types
              * listed in HTTPConstants.  Should we assume all application/* is SOAP12?
              * Should we assume all text/* is SOAP11?
              * 
              * So, we'll follow this pattern:
              * 1)  find the content-type main setting
              * 2)  if (1) not understood, find the "type=" param
              * Thilina: I merged (1) & (2)
              */
             
             if (JavaUtils.indexOfIgnoreCase(contentType,SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                 returnNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
             }
             // search for "type=text/xml"
             else if (JavaUtils.indexOfIgnoreCase(contentType,SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                 returnNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
             }
         }
         
         if (returnNS == null) {
             if (log.isDebugEnabled()) {
                 log.debug("No content-type or \"type=\" parameter was found in the content-type header and no default was specified, thus defaulting to SOAP 1.1.");
             }
             returnNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
         }
         
         if (log.isDebugEnabled()) {
             log.debug("content-type: " + contentType);
             log.debug("defaultSOAPNamespace: " + defaultSOAPNamespace);
             log.debug("Returned namespace: " + returnNS);
         }
         return returnNS;
         
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
