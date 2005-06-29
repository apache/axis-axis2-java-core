/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Runtime state of the engine
 */
package org.apache.axis.transport.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.attachments.MIMEHelper;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis.util.Utils;

public class HTTPTransportUtils {

	public static void processHTTPPostRequest(MessageContext msgContext,
			InputStream in, OutputStream out, String contentType,
			String soapAction, String requestURI,
			ConfigurationContext configurationContext) throws AxisFault {
		try {
			msgContext.setWSAAction(soapAction);
			msgContext.setSoapAction(soapAction);
			msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO,
					requestURI));
			msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
			msgContext.setServerSide(true);

			SOAPEnvelope envelope = null;
			StAXBuilder builder = null;


			if (contentType.indexOf(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) >= 0){
				builder = selectBuilderForMIME(msgContext, in, contentType);
				envelope = (SOAPEnvelope) builder.getDocumentElement();
			} else if (contentType != null
					&& contentType.indexOf(Constants.SOAP.SOAP_11_CONTENT_TYPE) > -1) {
				if ((soapAction == null || soapAction.length() == 0)
						&& Constants.VALUE_TRUE
								.equals(msgContext
										.getProperty(Constants.Configuration.ENABLE_REST))) {
					msgContext.setProperty(Constants.Configuration.DO_REST,
							Constants.VALUE_TRUE);
					SOAPFactory soapFactory = new SOAP11Factory();
                    Reader reader = new InputStreamReader(in);
                    XMLStreamReader xmlreader = XMLInputFactory.newInstance()
                            .createXMLStreamReader(reader);
					builder = new StAXOMBuilder(xmlreader);
					builder.setOmbuilderFactory(soapFactory);
					envelope = soapFactory.getDefaultEnvelope();
					envelope.getBody().addChild(builder.getDocumentElement());
				}
			}

			if (envelope == null) {
                Reader reader = new InputStreamReader(in);
                XMLStreamReader xmlreader = XMLInputFactory.newInstance()
                        .createXMLStreamReader(reader);
				builder = new StAXSOAPModelBuilder(xmlreader);
				envelope = (SOAPEnvelope) builder.getDocumentElement();
			}

			msgContext.setEnvelope(envelope);
			AxisEngine engine = new AxisEngine(configurationContext);
			engine.receive(msgContext);
		} catch (SOAPProcessingException e) {
			throw new AxisFault(e);
		} catch (OMException e) {
			throw new AxisFault(e);
		} catch (XMLStreamException e) {
			throw new AxisFault(e);
		}
	}

	public static boolean processHTTPGetRequest(MessageContext msgContext,
			InputStream in, OutputStream out, String contentType,
			String soapAction, String requestURI,
			ConfigurationContext configurationContext, Map requestParameters)
			throws AxisFault {
		msgContext.setWSAAction(soapAction);
		msgContext.setSoapAction(soapAction);
		msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO,
				requestURI));
		msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
		msgContext.setServerSide(true);
		try {
			SOAPEnvelope envelope = HTTPTransportUtils
					.createEnvelopeFromGetRequest(requestURI, requestParameters);
			if (envelope == null) {
				return false;
			} else {
				msgContext.setProperty(Constants.Configuration.DO_REST,
						Constants.VALUE_TRUE);
				msgContext.setEnvelope(envelope);
				AxisEngine engine = new AxisEngine(configurationContext);
				engine.receive(msgContext);
				return true;
			}
		} catch (IOException e) {
			throw new AxisFault(e);
		}
	}

	public static final SOAPEnvelope createEnvelopeFromGetRequest(
			String requestUrl, Map map) {
		String[] values = Utils
				.parseRequestURLForServiceAndOperation(requestUrl);

		if (values[1] != null && values[0] != null) {
			String operation = values[1];
			SOAPFactory soapFactory = new SOAP11Factory();
			SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();

			OMNamespace omNs = soapFactory.createOMNamespace(values[0],
					"services");
			OMNamespace defualtNs = new OMNamespaceImpl("", null);

			OMElement opElement = soapFactory.createOMElement(operation, omNs);

			Iterator it = map.keySet().iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				String value = (String) map.get(name);
				OMElement omEle = soapFactory.createOMElement(name, defualtNs);
				omEle.setText(value);
				opElement.addChild(omEle);
			}

			envelope.getBody().addChild(opElement);
			return envelope;
		} else {
			return null;
		}
	}

	public static StAXBuilder selectBuilderForMIME(MessageContext msgContext,
			InputStream inStream, String contentTypeString) throws OMException,
			XMLStreamException, FactoryConfigurationError {
		StAXBuilder builder = null;

		MIMEHelper mimeHelper = new MIMEHelper(inStream, contentTypeString);
		XMLStreamReader reader = XMLInputFactory.newInstance()
				.createXMLStreamReader(
						new BufferedReader(new InputStreamReader(mimeHelper
								.getSOAPPartInputStream())));
		/*
		 * put a reference to Attachments in to the message context
		 */
		msgContext.setProperty("Attachments", mimeHelper);
		if (mimeHelper.getAttachmentSpecType().equals(MIMEHelper.MTOM_TYPE)) {
			/*
			 * Creates the MTOM specific MTOMStAXSOAPModelBuilder
			 */
			builder = new MTOMStAXSOAPModelBuilder(reader, mimeHelper);
		} else if (mimeHelper.getAttachmentSpecType().equals(
				MIMEHelper.SWA_TYPE)) {
			builder = new StAXSOAPModelBuilder(reader);
		}
		return builder;
	}

	public static boolean checkEnvelopeForOptimise(SOAPEnvelope envelope) {
		return isOptimised(envelope);
	}

	private static boolean isOptimised(OMElement element) {
		Iterator childrenIter = element.getChildren();
        boolean isOptimized = false;
		while (childrenIter.hasNext()) {
			OMNode node = (OMNode) childrenIter.next();
			if (OMNode.TEXT_NODE == node.getType()
					&& ((OMText) node).isOptimized()) {
                        isOptimized =  true;
			} else if (OMNode.ELEMENT_NODE == node.getType()) {
                isOptimized = isOptimised((OMElement) node);
			}
		}
		return isOptimized;
	}

	public static boolean doWriteMTOM(MessageContext msgContext) {
		boolean enableMTOM = false;
		if (msgContext.getProperty(Constants.Configuration.ENABLE_MTOM) != null) {
			enableMTOM = Constants.VALUE_TRUE.equals(msgContext
					.getProperty(Constants.Configuration.ENABLE_MTOM));
		}
		boolean envelopeContainsOptimise = HTTPTransportUtils
				.checkEnvelopeForOptimise(msgContext.getEnvelope());
		boolean doMTOM = enableMTOM && envelopeContainsOptimise;
		msgContext.setDoMTOM(doMTOM);
		return doMTOM;
	}
}