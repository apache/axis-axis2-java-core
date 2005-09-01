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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
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
                    true);
            call.setTo(
                    new EndpointReference(url.toString()));
            String axisOp = request.getSOAPBody().getFirstChild().getNodeName();
            org.apache.axis2.soap.SOAPEnvelope responseEnv = (org.apache.axis2.soap.SOAPEnvelope) call.invokeBlocking(
                    axisOp, envelope);
            SOAPEnvelopeImpl response = new SOAPEnvelopeImpl(responseEnv);
            return new SOAPMessageImpl(response);

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
    			/*
    			//Get a handle to this element's parent and next sibling for later use.
    			OMElement parent = (OMElement)child.getParent();
    			OMNode nextSibling = child.getNextSibling();
    			OMNode prevSibling = child.getPreviousSibling();
    			
    			OMText omText = getOMTextForReferencedAttachment(hrefContentId, soapMsg);
    			
    			child.build();
    			child.detach();
    			//We should now detach the element which referenced the attachment
    			//and in its place put an OMText node created out of the attachment's
    			//data handler, of course, preserving the order of attachments
    			if(nextSibling!=null) {
    				nextSibling.insertSiblingBefore(omText); //preserving the order of attachments
    			} else if (prevSibling!=null) {
    				prevSibling.insertSiblingAfter(omText);
    			} else {//only child for its parent, so needn't bother about order
    				parent.addChild(omText);
    			}
    			*/
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
