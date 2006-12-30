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
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Builder;

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
			Object contentType;
			boolean isMIME = false;
			OperationContext opContext = msgContext.getOperationContext();

			if (opContext != null) {
				contentType = opContext
						.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE);
			} else {
				throw new AxisFault(Messages
						.getMessage("cannotBeNullOperationContext"));
			}
			//TODO: we can improve this logic
			if (contentType!=null){
				isMIME=true;
			}
			
			String charSetEnc = (String) msgContext
					.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
			if (charSetEnc == null) {
				charSetEnc = (String) opContext
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
    	OMBuilder builder;
		OMElement documentElement;
		if (isMIME) {
			msgContext.setDoingMTOM(true);
			builder = Builder.getAttachmentsBuilder(
					msgContext, inStream, (String) contentType, !(msgContext
							.isDoingREST()));
		} else if (msgContext.isDoingREST()) {
			builder = Builder.getPOXBuilder(inStream,
					charSetEnc, soapNamespaceURI);
		} else if (soapNamespaceURI!=null){
				builder = Builder.getBuilder(inStream, charSetEnc,soapNamespaceURI);
		}else
		{
			builder = Builder.getBuilderFromSelector(contentType, inStream, msgContext);
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
}
