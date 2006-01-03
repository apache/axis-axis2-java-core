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
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.DOOMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.util.SessionUtils2;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * 
 */
public class SOAPConnectionImpl extends SOAPConnection {
    /**
     * Sends the given message to the specified endpoint and
     * blocks until it has returned the response.
     *
     * @param request  the <CODE>SOAPMessage</CODE>
     *                 object to be sent
     * @param endpoint an <code>Object</code> that identifies
     *                 where the message should be sent. It is required to
     *                 support Objects of type
     *                 <code>java.lang.String</code>,
     *                 <code>java.net.URL</code>, and when JAXM is present
     *                 <code>javax.xml.messaging.URLEndpoint</code>
     * @return the <CODE>SOAPMessage</CODE> object that is the
     *         response to the message that was sent
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public SOAPMessage call(SOAPMessage request, Object endpoint) throws SOAPException {

        // initialize URL
        URL url;
        try {
            url = (endpoint instanceof URL) ? (URL) endpoint : new URL(endpoint.toString());
        } catch (MalformedURLException e) {
            throw new SOAPException(e);
        }

        // initialize the Call
        Call call;
        try {
            call = new Call();
        } catch (AxisFault e) {
            throw new SOAPException(e);
        }

        // initialize and set Options
        Options options = new Options();
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setTo(new EndpointReference(url.toString()));
        call.setClientOptions(options);

        String axisOp = request.getSOAPBody().getFirstChild().getNodeName();

        try {
            final SOAPEnvelope saajEnvelope = request.getSOAPPart().getEnvelope();
            /* final org.apache.axis2.soap.SOAPEnvelope omEnvelope =
           ((SOAPEnvelopeImpl) saajEnvelope).getOMEnvelope();*/

            final Iterator attachmentIter = request.getAttachments();
            while (attachmentIter.hasNext()) {
                System.err.println("########### Att=" + attachmentIter.next());
            }

//            final OMElement omEnvelope = ((SOAPEnvelopeImpl) saajEnvelope).getOMEnvelope();

            //parse the omEnvelope element and stuff it with the attachment
            //specific omText nodes
//            insertAttachmentNodes(omEnvelope, request);

            //-------------- Send the Request -----------------------

            //Convert to Default OM Implementation(LLOM at the moment) before calling Call.invokeBlocking
            OMElement result =
                    call.invokeBlocking(axisOp,
                                        SAAJUtil.toOMSOAPEnvelope(request.getSOAPPart().getDocumentElement()));

            //-------------- Handle the response --------------------
            SOAPEnvelopeImpl responseEnv =
                    new SOAPEnvelopeImpl(SAAJUtil.toDOOMSOAPEnvelope((org.apache.axis2.soap.SOAPEnvelope) result));

            SOAPMessageImpl sMsg = new SOAPMessageImpl(responseEnv);
//            extractAttachmentNodes(result, sMsg);
            return sMsg;
        }
        catch (AxisFault af) {
            throw new SOAPException(af);
        }
    }

    /**
     * Closes this <CODE>SOAPConnection</CODE> object.
     *
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public void close() throws SOAPException {
        //TODO: Method implementation

    }

    /**
     * This method recursively stuffs the OMElement with appropriate OMText nodes
     * that are prepared out of attachment contents whereever those attachments are referenced
     *
     * @param omEnvelope
     * @param soapMsg
     */
    private void insertAttachmentNodes(OMElement omEnvelope, SOAPMessage soapMsg) throws SOAPException {
//    private void insertAttachmentNodes(org.apache.axis2.soap.SOAPEnvelope omEnvelope,
//                                       SOAPMessage soapMsg) throws SOAPException {

        Iterator childIter = omEnvelope.getChildElements();
        while (childIter.hasNext()) {
            OMElement child = (OMElement) childIter.next();
            //check if there is an href attribute
            OMAttribute hrefAttr = child.getAttribute(new QName("href"));
            String hrefContentId = validateHref(hrefAttr);

            System.err.println("########## hrefContentId=" + hrefContentId);
            if (hrefContentId != null) {//This is an omEnvelope referencing an attachment!
                child.build();
                OMText omText = getOMTextForReferencedAttachment(hrefContentId,
                                                                 soapMsg,
                                                                 (DocumentImpl) ((ElementImpl) child).getOwnerDocument());

//                child.removeAttribute(hrefAttr); //y did SAAJ1 implementors remove the attribute???
                child.addChild(omText);
            } else { //possibly there can be references in the children of this omEnvelope
                //so recurse through.
                insertAttachmentNodes(child, soapMsg);
            }
        }
    }

    /**
     * The method recursively looks for the binary text nodes and adds them as attachment
     * to soapMessage at the same time removing them from soapEnv and putting appropriate
     * contentId
     *
     * @param element
     * @param soapMsg
     */
    private void extractAttachmentNodes(OMElement element, SOAPMessage soapMsg) {
        Iterator childIter = element.getChildren();
        while (childIter.hasNext()) {
            OMNode child = (OMNode) childIter.next();
            if (child instanceof OMText) {
                OMText binaryNode = (OMText) child;
                DataHandler actualDH = (DataHandler) binaryNode.getDataHandler();
                if (actualDH != null) {
                    AttachmentPart ap = soapMsg.createAttachmentPart(actualDH);
                    String contentId = SessionUtils2.generateSessionId();
                    ap.setContentId(contentId);
                    ap.setContentType(actualDH.getContentType());
                    OMElement parent = (OMElement) child.getParent();
                    OMAttribute attr =
                            DOOMAbstractFactory.getOMFactory().createOMAttribute("href",
                                                                                 null,
                                                                                 "cid:" + contentId);

                    parent.addAttribute(attr);
                    binaryNode.detach();
                    soapMsg.addAttachmentPart(ap);
                }
            } else {
                if (child instanceof OMElement) {
                    OMElement childElement = (OMElement) child;
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
        if (attr != null) {
            contentId = attr.getAttributeValue();
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
     *
     * @param contentId
     * @param soapMsg
     * @return OMText
     */
    private OMText getOMTextForReferencedAttachment(String contentId,
                                                    SOAPMessage soapMsg,
                                                    DocumentImpl doc) throws SOAPException {

        Iterator attachIter = soapMsg.getAttachments();
        while (attachIter.hasNext()) {
            AttachmentPart attachment = (AttachmentPart) attachIter.next();
            if (attachment.getContentId().equals(contentId)) {
                try {
                    return ((AttachmentPartImpl) attachment).getText(doc);
                } catch (Exception e) {
                    throw new SOAPException(e);
                }
            }
        }
        throw new SOAPException("No attachment found with the given contentID");
    }
}
