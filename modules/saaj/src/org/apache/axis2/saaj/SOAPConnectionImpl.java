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
package org.apache.axis2.saaj;

import org.w3c.dom.Node;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.util.SessionUtils;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.AttachmentPart;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

/**
 * Class SOAPConnectionImpl
 *
 * @author Ashutosh Shahi (ashutosh.shahi@gmail.com)
 */
public class SOAPConnectionImpl extends SOAPConnection {

    /* (non-Javadoc)
     * @see javax.xml.soap.SOAPConnection#call(javax.xml.soap.SOAPMessage, java.lang.Object)
     */
    public SOAPMessage call(SOAPMessage request, Object endpoint)
            throws SOAPException {
        try {
            OMElement envelope = ((SOAPEnvelopeImpl) request.getSOAPPart()
                    .getEnvelope()).getOMEnvelope();
            
            //parse the omEnvelope element and stuff it with the attachment
            //specific omText nodes
            insertAttachmentNodes(envelope, request);

            Call call = new Call();
            URL url = new URL(endpoint.toString());
            call.set(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    false);
            call.setTo(
                    new EndpointReference(url.toString()));
            String axisOp = request.getSOAPBody().getFirstChild().getNodeName();
            NodeImpl bodyContentNode = (NodeImpl)request.getSOAPBody().getFirstChild();
            OMElement bodyContent = (OMElement)bodyContentNode.getOMNode();
            OMElement result = call.invokeBlocking(axisOp, bodyContent);
            org.apache.axis2.soap.SOAPEnvelope responseEnv = (org.apache.axis2.soap.SOAPEnvelope) ((OMElement)result.getParent()).getParent(); 
            SOAPEnvelopeImpl response = new SOAPEnvelopeImpl(responseEnv);
            
            SOAPMessageImpl sMsg = new SOAPMessageImpl(response);
            extractAttachmentNodes(responseEnv, sMsg);
            return sMsg;

        } catch (MalformedURLException mue) {
            throw new SOAPException(mue);
        } catch (AxisFault af) {
            throw new SOAPException(af);
        }
    }


    /* (non-Javadoc)
     * @see javax.xml.soap.SOAPConnection#close()
     */
    public void close() throws SOAPException {
        // TODO Auto-generated method stub

    }
    
    /**
     * This method recursively stuffs the OMElement with appropriate OMText nodes
     * that are prepared out of attachment contents whereever those attachments are referenced
     * @param element
     * @param soapMsg
     */
    private void insertAttachmentNodes(OMElement element, SOAPMessage soapMsg) throws SOAPException {
    	Iterator childIter = element.getChildElements();
    	while(childIter.hasNext()) {
    		OMElement child = (OMElement)childIter.next();
    		//check if there is an href attribute
    		OMAttribute hrefAttr = (OMAttribute)child.getFirstAttribute(new QName("href"));
    		String hrefContentId = validateHref(hrefAttr);
    		
    		if (hrefContentId!=null) {//This is an element referencing an attachment!
    			OMText omText = getOMTextForReferencedAttachment(hrefContentId, soapMsg);
    			child.build();
    			child.removeAttribute(hrefAttr);
    			child.addChild(omText);
    			
    		} else { //possibly there can be references in the children of this element
    				 //so recurse through.
    			insertAttachmentNodes(child, soapMsg);
    		}
    	}
    }
    
    /**
     * The method recursively looks for the binary text nodes and adds them as attachment
     * to soapMessage at the same time removing them from soapEnv and putting appropriate
     * contentId
     * @param element
     * @param soapMsg
     */
    private void extractAttachmentNodes(OMElement element, SOAPMessage soapMsg){
    	Iterator childIter = element.getChildElements();
    	while(childIter.hasNext()) {
    		OMNode child = (OMNode)childIter.next();
    		if(child instanceof OMText){
    			OMText binaryNode = (OMText)child;
    			DataHandler actualDH = binaryNode.getDataHandler();
    			if(actualDH != null){
    				AttachmentPart ap = soapMsg.createAttachmentPart(actualDH);
    				String contentId = SessionUtils.generateSessionId();
    				ap.setContentId(contentId);
    				ap.setContentType(actualDH.getContentType());
    				OMElement parent = (OMElement)child.getParent();
    				OMAttribute attr = org.apache.axis2.om.OMAbstractFactory.getOMFactory().createOMAttribute("href", null,"cid:"+contentId);
    				parent.addAttribute(attr);
    				binaryNode.detach();
    				soapMsg.addAttachmentPart(ap);
    			}
    		} else{
				if(child instanceof OMElement) {
					OMElement childElement = (OMElement)child;
					extractAttachmentNodes(childElement, soapMsg);
				}
    		}
    	}
    }
    /**
     * This method checks the value of attribute and if it is a valid CID then
     * returns the contentID (with cid: prefix stripped off) or else returns null.
     * A null return value can be assumed that this attribute is not an attachment
     * referencing attribute
     */
    private String validateHref(OMAttribute attr) {
    	String contentId;
    	if(attr!=null) {
    		contentId = attr.getValue();
    	} else {
    		return null;
    	}
    	
    	if (contentId.startsWith("cid:")) {
    		contentId = contentId.substring(4);
    		return contentId;
    	}
    	return null;
    }
    
    /**
     * This method looks up the attachment part corresponding to the given contentId and
     * returns the OMText node thta has the content of the attachment.
     * @param contentId
     * @param soapMsg
     * @return
     */
    private OMText getOMTextForReferencedAttachment(String contentId, SOAPMessage soapMsg) throws SOAPException{
    	Iterator attachIter = soapMsg.getAttachments();
		while(attachIter.hasNext()) {
			AttachmentPart attachment = (AttachmentPart)attachIter.next();
			if(attachment.getContentId().equals(contentId)) {
				try {
					return ((AttachmentPartImpl)attachment).getOMText();
				} catch (Exception e) {
					throw new SOAPException(e);
				}
			}
		}
    	throw new SOAPException("No attachment found with the given contentID");
    }
}
