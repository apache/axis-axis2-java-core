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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.util.IDGenerator;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class SOAPConnectionImpl extends SOAPConnection {

    /**
     * Attribute which keeps track of whether this connection has been closed
     */
    private boolean closed = false;

    private ServiceClient serviceClient;

	private static final Log log = LogFactory.getLog(SOAPConnectionImpl.class);

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
     * @throws javax.xml.soap.SOAPException if there is a SOAP error,
     *                                      or this SOAPConnection is already closed
     */
    public SOAPMessage call(SOAPMessage request, Object endpoint) throws SOAPException {

        if (closed) {
            throw new SOAPException("SOAPConnection closed");
        }

        // initialize URL
        URL url;
        try {
            url = (endpoint instanceof URL) ? (URL) endpoint : new URL(endpoint.toString());
        } catch (MalformedURLException e) {
            throw new SOAPException(e);
        }

        // initialize and set Options
        Options options = new Options();
        options.setTo(new EndpointReference(url.toString()));

        // initialize the Sender
        OperationClient opClient;
        try {
            serviceClient = new ServiceClient();
            opClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        } catch (AxisFault e) {
            throw new SOAPException(e);
        }
        opClient.setOptions(options);

        if (request.countAttachments() != 0) { // SOAPMessage with attachments
            opClient.getOptions().setProperty(Constants.Configuration.ENABLE_MTOM,
                                              Constants.VALUE_TRUE);
            return handleSOAPMessage(request, opClient);
        } else { // simple SOAPMessage
            return handleSOAPMessage(request, opClient);
        }
    }

    /**
     * Closes this <CODE>SOAPConnection</CODE> object.
     *
     * @throws javax.xml.soap.SOAPException if there is a SOAP error,
     *                                      or this SOAPConnection is already closed
     */
    public void close() throws SOAPException {
        if (serviceClient != null) {
            try {
                serviceClient.cleanup();
            } catch (AxisFault axisFault) {
                throw new SOAPException(axisFault.getMessage());
            }
        }
        if (closed) {
            throw new SOAPException("SOAPConnection Closed");
        }
        closed = true;
    }

    private SOAPMessage handleSOAPMessage(SOAPMessage request,
                                          OperationClient opClient) throws SOAPException {

        MessageContext requestMsgCtx = new MessageContext();
        try {
            requestMsgCtx.setEnvelope(toOMSOAPEnvelope(request));
            opClient.addMessageContext(requestMsgCtx);
            opClient.execute(true);

            MessageContext msgCtx = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            //TODO: get attachments

            return getSOAPMessage(msgCtx.getEnvelope());
        } catch (AxisFault e) {
            throw new SOAPException(e);
        }
    }

    /**
     * This method handles the conversion of an OM SOAP Envelope to a SAAJ SOAPMessage
     *
     * @param respOMSoapEnv
     * @return the SAAJ SOAPMessage
     * @throws SOAPException If an exception occurs during this conversion
     */
    private SOAPMessage getSOAPMessage(org.apache.axiom.soap.SOAPEnvelope respOMSoapEnv)
            throws SOAPException {

        System.err.println("####### respOMSoapEnv=" + respOMSoapEnv);

        // Create the basic SOAP Message
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage response = mf.createMessage();
        SOAPPart sPart = response.getSOAPPart();
        javax.xml.soap.SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();
        SOAPHeader header = env.getHeader();

        //Convert all header blocks
        for (Iterator hbIter = respOMSoapEnv.getHeader().examineAllHeaderBlocks();
             hbIter.hasNext();) {

            // Converting a single OM SOAP HeaderBlock to a SAAJ SOAP HeaderBlock
            org.apache.axiom.soap.SOAPHeaderBlock hb =
                    (org.apache.axiom.soap.SOAPHeaderBlock) hbIter.next();
            final QName hbQName = hb.getQName();
            final SOAPHeaderElement headerEle =
                    header.addHeaderElement(env.createName(hbQName.getLocalPart(),
                                                           hbQName.getPrefix(),
                                                           hbQName.getNamespaceURI()));
            for (Iterator attribIter = hb.getAllAttributes(); attribIter.hasNext();) {
                OMAttribute attr = (OMAttribute) attribIter.next();
                final QName attrQName = attr.getQName();
                headerEle.addAttribute(env.createName(attrQName.getLocalPart(),
                                                      attrQName.getPrefix(),
                                                      attrQName.getNamespaceURI()),
                                       attr.getAttributeValue());
            }
            final String role = hb.getRole();
            if (role != null) {
                headerEle.setActor(role);
            }
            headerEle.setMustUnderstand(hb.getMustUnderstand());

            toSAAJElement(headerEle, hb, response);
        }

        // Convert the body
        toSAAJElement(body, respOMSoapEnv.getBody(), response);

        return response;
    }

    /**
     * Converts an OMNode into a SAAJ SOAPElement
     *
     * @param saajEle
     * @param omNode
     * @param saajSOAPMsg
     * @throws SOAPException If conversion fails
     */
    private void toSAAJElement(SOAPElement saajEle,
                               OMNode omNode,
                               javax.xml.soap.SOAPMessage saajSOAPMsg) throws SOAPException {

        if (omNode instanceof OMText) {
            return; // simply return since the text has already been added to saajEle
        }

        if (omNode instanceof OMElement) {
            OMElement omEle = (OMElement) omNode;
            for (Iterator childIter = omEle.getChildren(); childIter.hasNext();) {
                OMNode omChildNode = (OMNode) childIter.next();
                SOAPElement saajChildEle = null;

                if (omChildNode instanceof OMText) {
                    // check whether the omtext refers to an attachment

                    final OMText omText = (OMText) omChildNode;
                    if (omText.isOptimized()) { // is this an attachment?
                        final DataHandler datahandler = (DataHandler) omText.getDataHandler();
                        AttachmentPart attachment = saajSOAPMsg.createAttachmentPart(datahandler);
                        final String id = IDGenerator.generateID();
                        attachment.setContentId(id);
                        attachment.setContentType(datahandler.getContentType());
                        saajSOAPMsg.addAttachmentPart(attachment);

                        saajEle.addAttribute(saajSOAPMsg.getSOAPPart().getEnvelope().createName("href"),
                                             "cid:" + id);
                    } else {
                        saajChildEle = saajEle.addTextNode(omText.getText());
                    }
                } else if (omChildNode instanceof OMElement) {
                    OMElement omChildEle = (OMElement) omChildNode;
                    final QName omChildQName = omChildEle.getQName();
                    saajChildEle =
                            saajEle.addChildElement(omChildQName.getLocalPart(),
                                                    omChildQName.getPrefix(),
                                                    omChildQName.getNamespaceURI());
                    for (Iterator attribIter = omChildEle.getAllAttributes();
                         attribIter.hasNext();) {
                        OMAttribute attr = (OMAttribute) attribIter.next();
                        final QName attrQName = attr.getQName();
                        saajChildEle.addAttribute(saajSOAPMsg.getSOAPPart().getEnvelope().
                                createName(attrQName.getLocalPart(),
                                           attrQName.getPrefix(),
                                           attrQName.getNamespaceURI()),
                                                  attr.getAttributeValue());
                    }
                }

                // go down the tree adding child elements, till u reach a leaf(i.e. text element)
                toSAAJElement(saajChildEle, omChildNode, saajSOAPMsg);
            }
        }
    }

    /**
     * Converts a SAAJ SOAPMessage to an OM SOAPEnvelope
     *
     * @param saajSOAPMsg
     * @return
     * @throws SOAPException
     */
    protected org.apache.axiom.soap.SOAPEnvelope toOMSOAPEnvelope(SOAPMessage saajSOAPMsg)
            throws SOAPException {

        final org.apache.axiom.soap.SOAPEnvelope omSOAPEnv =
                SAAJUtil.toOMSOAPEnvelope(saajSOAPMsg.getSOAPPart().getDocumentElement());
        System.err.println("#### req OM Soap Env=" + omSOAPEnv);

        Map attachmentMap = new HashMap();
        final Iterator attachments = saajSOAPMsg.getAttachments();
        while (attachments.hasNext()) {
            final AttachmentPart attachment = (AttachmentPart) attachments.next();
            if (attachment.getContentId() == null ||
                attachment.getContentId().trim().length() == 0) {
                attachment.setContentId(IDGenerator.generateID());
            }
            if (attachment.getDataHandler() == null) {
                throw new SOAPException("Attachment with NULL DataHandler");
            }
            attachmentMap.put(attachment.getContentId(), attachment);
        }

        insertAttachmentNodes(attachmentMap, omSOAPEnv);

//        printOMSOAPEnvelope(omSOAPEnv);

        return omSOAPEnv;
    }

    /**
     * Inserts the attachments in the proper places
     *
     * @param attachments
     * @param omEnvelope
     * @throws SOAPException
     */
    private void insertAttachmentNodes(Map attachments,
                                       OMElement omEnvelope) throws SOAPException {

        Iterator childIter = omEnvelope.getChildElements();
        while (childIter.hasNext()) {
            OMElement child = (OMElement) childIter.next();
            final OMAttribute hrefAttr = child.getAttribute(new QName("href"));
            String contentID = getContentID(hrefAttr);

            if (contentID != null) {//This is an omEnvelope referencing an attachment
                child.build();
                AttachmentPart ap = ((AttachmentPart) attachments.get(contentID.trim()));
                OMText text = new OMTextImpl(ap.getDataHandler(), true,
                        omEnvelope.getOMFactory());
                child.removeAttribute(hrefAttr);
                child.addChild(text);
            } else {

                //possibly there can be references in the children of this omEnvelope
                //so recurse through.
                insertAttachmentNodes(attachments, child);
            }
        }

        //TODO: Need to handle no referenced attachments
    }

    /**
     * This method checks the value of attribute and if it is a valid CID then
     * returns the contentID (with cid: prefix stripped off) or else returns null.
     * A null return value can be assumed that this attribute is not an attachment
     * referencing attribute
     *
     * @return the ContentID
     */
    private String getContentID(OMAttribute attr) {
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

    /* private void printOMSOAPEnvelope(final org.apache.axiom.soap.SOAPEnvelope omSOAPEnv) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            omSOAPEnv.serialize(baos);
            log.info("---------------------------------------------------------------------------");
            log.info(baos);
            log.info("---------------------------------------------------------------------------");
            System.err.println("---------------------------------------------------------------------------");
            System.err.println(baos);
            System.err.println("---------------------------------------------------------------------------");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private String printSAAJSOAPMessage(final SOAPMessage msg) throws SOAPException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);
        String responseStr = baos.toString();

        System.out.println("\n\n----------------------SAAJ Message-------------------------\n" +
                           responseStr);
        System.out.println("-------------------------------------------------------\n\n");
        return responseStr;
    }*/
}
