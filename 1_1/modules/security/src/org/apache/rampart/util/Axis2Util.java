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

package org.apache.rampart.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Utility class for the Axis2-WSS4J Module
 */
public class Axis2Util {
    
    private static ThreadLocal doomTacker = new ThreadLocal();
    
    public static boolean isUseDOOM() {
        Object value = doomTacker.get();
        return (value != null);
    }
    
    public static void useDOOM(boolean isDOOMRequired) {
//TODO Enable this when we have DOOM fixed to be able to flow in and out of Axis2
//        if(isDOOMRequired) {
//            if(!isUseDOOM()) {
//                System.setProperty(OMAbstractFactory.SOAP11_FACTORY_NAME_PROPERTY, SOAP11Factory.class.getName());
//                System.setProperty(OMAbstractFactory.SOAP12_FACTORY_NAME_PROPERTY, SOAP12Factory.class.getName());
//                System.setProperty(OMAbstractFactory.OM_FACTORY_NAME_PROPERTY, OMDOMFactory.class.getName());
//                doomTacker.set(new Object());
//            }
//        } else {
//            System.getProperties().remove(OMAbstractFactory.SOAP11_FACTORY_NAME_PROPERTY);
//            System.getProperties().remove(OMAbstractFactory.SOAP12_FACTORY_NAME_PROPERTY);
//            System.getProperties().remove(OMAbstractFactory.OM_FACTORY_NAME_PROPERTY);
//            doomTacker.set(null);
//        }
    }
    
    
	/**
	 * Creates a DOM Document using the SOAP Envelope.
	 * @param env An org.apache.axiom.soap.SOAPEnvelope instance
	 * @return Returns the DOM Document of the given SOAP Envelope.
	 * @throws Exception
	 */
	public static Document getDocumentFromSOAPEnvelope(SOAPEnvelope env, boolean disableDoom)
			throws WSSecurityException {
		try {
            if(env instanceof Element) {
                return ((Element)env).getOwnerDocument();
            }
            
            if(!disableDoom) {
    			env.build();
                
                //Workaround to prevent a bug in AXIOM where 
                //there can be an incomplete OMElement as the first child body 
                OMElement firstElement = env.getBody().getFirstElement();
                if(firstElement != null) {
                    firstElement.build();
                }
    			
    			//Check the namespace and find SOAP version and factory
    			String nsURI = null;
    			SOAPFactory factory;
    			if(env.getNamespace().getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
    				nsURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    				factory = DOOMAbstractFactory.getSOAP11Factory();
    			} else {
    				nsURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    				factory = DOOMAbstractFactory.getSOAP12Factory();
    			}
    			
    			StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(env.getXMLStreamReader(),factory, nsURI);
    			SOAPEnvelope envelope = (stAXSOAPModelBuilder).getSOAPEnvelope();
    			((OMNode)envelope.getParent()).build();
    			
    			Element envElem = (Element)envelope;
    			return envElem.getOwnerDocument();
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                env.build();
                env.serialize(baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                return factory.newDocumentBuilder().parse(bais);
            }
		} catch (Exception e) {
			throw new WSSecurityException(
					"Error in converting SOAP Envelope to Document", e);
		}
	}

	public static SOAPEnvelope getSOAPEnvelopeFromDOOMDocument(Document doc, boolean dd)
            throws WSSecurityException {

        if(!dd) {
            try {
                XMLStreamReader reader = ((OMElement) doc.getDocumentElement())
                        .getXMLStreamReader();
                StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(
                        reader, null);
                SOAPEnvelope envelope = stAXSOAPModelBuilder.getSOAPEnvelope();
                envelope.build();
                return envelope;
                
            } catch (FactoryConfigurationError e) {
                throw new WSSecurityException(e.getMessage());
            }
        } else {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                XMLUtils.outputDOM(doc.getDocumentElement(), os, true);
                ByteArrayInputStream bais =  new ByteArrayInputStream(os.toByteArray());
                
                StAXSOAPModelBuilder stAXSOAPModelBuilder = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(bais), null);
                return stAXSOAPModelBuilder.getSOAPEnvelope();
            } catch (Exception e) {
                throw new WSSecurityException(e.getMessage());
            }
        }

    }
	
	
	/**
	 * Provides the appropriate key to pickup config params from the message context.
	 * This is acutally used when the outflow handler (WSDoAllSender)
	 * is repeated n number of times.
	 * @param originalKey The default key
	 * @param inHandler Whether the handler is the inflow handler or not
	 * @param repetition The current repetition number
	 * @return Returns the key to be used internally in the security module to pick
	 * up the config params.
	 */
	public static String getKey(String originalKey, boolean inHandler, int repetition) {
		
		if(repetition > 0 && !inHandler && 
				!originalKey.equals(WSSHandlerConstants.OUTFLOW_SECURITY)&&	
				!originalKey.equals(WSSHandlerConstants.SENDER_REPEAT_COUNT)) {
			
				return originalKey + repetition;
		}
		return originalKey;
	}
	
    /**
     * This will build a DOOM Element that is of the same <code>Document</code>
     * @param factory
     * @param element
     * @return
     */
    public static OMElement toDOOM(OMFactory factory, OMElement element){
        StAXOMBuilder builder = new StAXOMBuilder(factory, element.getXMLStreamReader());
        OMElement elem = builder.getDocumentElement();
        elem.build();
        return elem;
    }
    
}
