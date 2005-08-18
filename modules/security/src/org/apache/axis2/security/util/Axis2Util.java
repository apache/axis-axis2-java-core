/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.axis2.security.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.ws.security.WSSecurityException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;

/**
 * Utility class for the Axis2-WSS4J Module
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class Axis2Util {

	/**
	 * Create a DOM Document using the SOAP Envelope
	 * @param env An org.apache.axis2.soap.SOAPEnvelope instance 
	 * @return the DOM Document of the given SOAP Envelope
	 * @throws Exception
	 */
	public static Document getDocumentFromSOAPEnvelope(SOAPEnvelope env)
			throws WSSecurityException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			/**
			 * There are plans to deprecate the OmNode.serialize(XMLStreamWriter)
			 * method therefore using OMOutoutImpl to serialize the env
			 */
			OMOutputImpl output = new OMOutputImpl(baos, false);
			env.serialize(output);
			output.flush();

			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());

			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(bais);
		} catch (Exception e) {
			throw new WSSecurityException(
					"Error in converting SOAP Envelope to Document", e);
		}
	}

	/**
	 * Covert a DOM Document containing a SOAP Envelope in to a
	 * org.apache.axis2.soap.SOAPEnvelope 
	 * @param doc DOM Document
	 * @param envelopeNS SOAP Namespace of the the given Envelope
	 * @return
	 * @throws Exception
	 */
	public static SOAPEnvelope getSOAPEnvelopeFromDocument(Document doc,
			String envelopeNS) throws WSSecurityException {
		try {
			//Set the new SOAPEnvelope
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			XMLUtils.outputDOM(doc, os, true);

			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			XMLStreamReader reader = XMLInputFactory.newInstance()
					.createXMLStreamReader(is);

			StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader,
					envelopeNS);

			return builder.getSOAPEnvelope();

		} catch (Exception e) {
			throw new WSSecurityException(
					"Error in converting document to SOAPEnvelope", e);
		}

	}

}