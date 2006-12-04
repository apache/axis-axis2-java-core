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

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.builder.XOPAwareStAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class TransportUtils {
    private static final int BOM_SIZE = 4;

    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext, String soapNamespaceURI)
            throws AxisFault {
        InputStream inStream = (InputStream) msgContext.getProperty(MessageContext.TRANSPORT_IN);

        msgContext.setProperty(MessageContext.TRANSPORT_IN, null);

        // this inputstram is set by the TransportSender represents a two way transport or
        // by a Transport Recevier
        if (inStream == null) {
            throw new AxisFault(Messages.getMessage("inputstreamNull"));
        }

        return createSOAPMessage(msgContext, inStream, soapNamespaceURI);
    }

    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext, InputStream inStream,
                                                  String soapNamespaceURI)
            throws AxisFault {
        try {
            Object contentType ;
            OperationContext opContext = msgContext.getOperationContext();

            if (opContext != null) {
                contentType = opContext.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE);
            } else {
                throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
            }

            StAXBuilder builder;
            SOAPEnvelope envelope ;
            String charSetEnc =
                    (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

            if (charSetEnc == null) {
                charSetEnc = (String) opContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            }

            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            if (contentType != null) {
                msgContext.setDoingMTOM(true);
                builder = selectBuilderForMIME(msgContext, inStream,
                        (String) contentType,true);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else if (msgContext.isDoingREST()) {
                XMLStreamReader xmlreader =
                        StAXUtils.createXMLStreamReader(inStream, charSetEnc);
                SOAPFactory soapFactory = new SOAP11Factory();

                builder = new StAXOMBuilder(xmlreader);
                builder.setOMBuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
                
                // We now have the message inside an envolope. However, this is 
                // only an OM; We need to build a SOAP model from it.

                builder = new StAXSOAPModelBuilder(envelope.getXMLStreamReader(), soapNamespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else {
                XMLStreamReader xmlreader =
                        StAXUtils.createXMLStreamReader(inStream, charSetEnc);

                builder = new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }

            return envelope;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
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

	public static StAXBuilder selectBuilderForMIME(MessageContext msgContext,
			InputStream inStream, String contentTypeString, boolean isSOAP)
			throws OMException, XMLStreamException, FactoryConfigurationError {
		StAXBuilder builder = null;

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
		String charSetEncoding = getCharSetEncoding(attachments
				.getSOAPPartContentType());
		XMLStreamReader streamReader;

		if ((charSetEncoding == null)
				|| "null".equalsIgnoreCase(charSetEncoding)) {
			charSetEncoding = MessageContext.UTF_8;
		}

		try {
			streamReader = StAXUtils.createXMLStreamReader(getReader(
					attachments.getSOAPPartInputStream(), charSetEncoding));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}

		msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
				charSetEncoding);

		/*
		 * Put a reference to Attachments Map in to the message context For
		 * backword compatibility with Axis2 1.0
		 */
		msgContext.setProperty(MTOMConstants.ATTACHMENTS, attachments);

		/*
		 * Setting the Attachments map to new SwA API
		 */
		msgContext.setAttachmentMap(attachments);

		String soapEnvelopeNamespaceURI = null;
		if (contentTypeString.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
			soapEnvelopeNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		} else if (contentTypeString
				.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
			soapEnvelopeNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		}

		if (isSOAP) {
			if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.MTOM_TYPE)
					& null != soapEnvelopeNamespaceURI) {

				/*
				 * Creates the MTOM specific MTOMStAXSOAPModelBuilder
				 */
				builder = new MTOMStAXSOAPModelBuilder(streamReader,
						attachments, soapEnvelopeNamespaceURI);

			} else if (attachments.getAttachmentSpecType().equals(
					MTOMConstants.SWA_TYPE)
					& null != soapEnvelopeNamespaceURI) {
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
			}
		}

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
    private static Reader getReader(InputStream is, String charSetEncoding) throws IOException {
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
}
