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


package org.apache.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * An exception which maps cleanly to a SOAP fault.
 * This is a base class for exceptions which are mapped to faults.
 *
 * @see <a href="http://www.w3.org/TR/2003/REC-soap12-part1-20030624/#soapfault">
 *      SOAP1.2 specification</a>
 * @see <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383507">SOAP1.1 Faults</a>
 *      <p/>
 *      SOAP faults contain
 *      <ol>
 *      <li>A fault string
 *      <li>A fault code
 *      <li>A fault actor
 *      <li>Fault details; an xml tree of fault specific elements
 *      </ol>
 *      <p/>
 *      As SOAP1.2 faults are a superset of SOAP1.1 faults, this type holds soap1.2 fault information. When
 *      a SOAP1.1 fault is created, spurious information can be discarded.
 *      Mapping
 *      <pre>
 *                                                        SOAP1.2              SOAP1.1
 *                                                        node                 faultactor
 *                                                        reason(0).text       faultstring
 *                                                        faultcode.value      faultcode
 *                                                        faultcode.subcode    (discarded)
 *                                                        detail               detail
 *                                                        role                 (discarded)
 *                                                        </pre>
 */
public class AxisFault extends RemoteException {

    private static final long serialVersionUID = -374933082062124907L;

    /**
     * assume headers are not used very often
     */
    private List headers = new ArrayList(0);

    private List faultReasonList = new ArrayList(1);
    private QName faultCode;
    private String faultNode;
    private String faultRole;
    private OMElement detail;

    private Map faultElements;

    private String message;
    private Throwable cause;

    /**
     * If not null, the messageContext represents the fault as it
     * should be returned.  This is used by higher-level layers
     * that want to generate the message themselves so that
     * processing may take place before they return control (e.g. JAX-WS.)
     */
    private MessageContext faultMessageContext;
    
    /**
     * SOAP1.2: URI of faulting node. Null for unknown.
     * <p/>
     * The value of the Node element information item is the URI that
     * identifies the SOAP node that generated the fault.
     * SOAP nodes that do not act as the ultimate SOAP receiver MUST include this element
     * information item.
     * An ultimate SOAP receiver MAY include this element information item to
     * indicate explicitly that it generated the fault.
     */
    private String nodeURI;


    /**
     * @param message
     */
    public AxisFault(String message) {
        super(message);
        addReason(message);
    }

    /**
     * These are the absolute minimum to construct a meaningful SOAPFault from user's information
     *
     * @param faultCode   - fault code of the message as a QName
     * @param faultReason - the reason for the fault. The language will be defaulted to 'en'
     * @param cause
     */
    public AxisFault(QName faultCode, String faultReason, Throwable cause) {
        this(faultReason, cause);
        setFaultCode(faultCode);
    }

    public AxisFault(QName faultCode, String faultReason, String faultNode, String faultRole, OMElement faultDetail) {
        this(faultReason, faultCode);
        this.faultNode = faultNode;
        this.faultRole = faultRole;
        setDetail(faultDetail);
    }

    /**
     * This is just a convenience method for the user. If you set these, do not use other methods
     * in this class to get and set things.
     * Any of the parameters can be null
     *
     * @param soapFaultCode
     * @param soapFaultReason
     * @param soapFaultNode
     * @param soapFaultRole
     */
    public AxisFault(SOAPFaultCode soapFaultCode, SOAPFaultReason soapFaultReason,
                     SOAPFaultNode soapFaultNode, SOAPFaultRole soapFaultRole, SOAPFaultDetail soapFaultDetail) {
        initializeValues(soapFaultCode, soapFaultReason, soapFaultNode, soapFaultRole, soapFaultDetail);
    }
    
    public AxisFault(SOAPFault fault) { 
        initializeValues(fault);
    }
    
    public AxisFault(SOAPFault fault, MessageContext faultCtx) {
        initializeValues(fault);
        faultMessageContext = faultCtx;
    }
    
    private void initializeValues(SOAPFault fault) {
        if (fault != null) {
            initializeValues(fault.getCode(), fault.getReason(), fault.getNode(), 
                    fault.getRole(), fault.getDetail());
        }        
    }

    private void initializeValues(SOAPFaultCode soapFaultCode, 
                                  SOAPFaultReason soapFaultReason, 
                                  SOAPFaultNode soapFaultNode, 
                                  SOAPFaultRole soapFaultRole,
                                  SOAPFaultDetail soapFaultDetail) {
        if (faultElements == null) {
            // assuming that most of the times fault code, fault string and fault details are set
            faultElements = new HashMap(3);
        }
        setToElementsListIfNotNull(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
        setToElementsListIfNotNull(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, soapFaultReason);
        setToElementsListIfNotNull(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME, soapFaultNode);
        setToElementsListIfNotNull(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, soapFaultRole);
        setToElementsListIfNotNull(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, soapFaultDetail);

        if (soapFaultReason != null) {
            message = soapFaultReason.getFirstSOAPText().getText();
        }

        if (soapFaultDetail != null) {
            OMElement exceptionElement = soapFaultDetail.getFirstChildWithName(
                    new QName(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY));
            if (exceptionElement != null && exceptionElement.getText() != null) {
                cause = new Exception(exceptionElement.getText());
            }

            // setting the first child element of the fault detail as this.detail
            this.detail = soapFaultDetail.getFirstElement();

        }

        if(soapFaultCode != null && soapFaultCode.getValue() != null) {
            faultCode = soapFaultCode.getValue().getTextAsQName();
        }
    }
     
    private void setToElementsListIfNotNull(String soapFaultElementName, OMElement soapFaultElement) {
        if (soapFaultElement != null) {
            faultElements.put(soapFaultElementName, soapFaultElement);
        }
    }

    /**
     * construct a fault from an exception
     * TODO: handle AxisFaults or SOAPFaultException implementations differently?
     *
     * @param cause
     */
    public AxisFault(Throwable cause) {
        this((cause != null)
                ? cause.getMessage()
                : null, cause);
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP Fault
     */
    public AxisFault(String messageText, String faultCode) {
        this(messageText);
        setFaultCode(faultCode);
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP Fault
     */
    public AxisFault(String messageText, QName faultCode) {
        this(messageText);
        setFaultCode(faultCode);
    }

    /**
     * @param message
     * @param cause
     */
    public AxisFault(String message, Throwable cause) {
        super(message, cause);

        if (message != null) {
            addReason(message);
        }
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP Fault
     * @param cause       - this will appear under the Detail information item of SOAP Fault
     */
    public AxisFault(String messageText, QName faultCode, Throwable cause) {
        this(messageText, cause);
        setFaultCode(faultCode);
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP Fault
     * @param cause       - this will appear under the Detail information item of SOAP Fault
     */
    public AxisFault(String messageText, String faultCode, Throwable cause) {
        this(messageText, cause);
        setFaultCode(faultCode);
    }

    /**
     * Create an AxisFault by providing a textual message and a MessageContext
     * that contains the actual fault representation.
     * 
     * @param message A string that's really only useful for logging.
     * @param faultMessageContext
     */
    public AxisFault(String message, MessageContext faultMessageContext) {
      super(message);
      this.faultMessageContext = faultMessageContext;
    }
    
    /**
     * Add a header to the list of fault headers
     *
     * @param header to add.
     */
    public void addHeader(SOAPHeaderBlock header) {
        headers.add(header);
    }

    /**
     * Add a reason for the fault in the empty "" language
     *
     * @param text text message
     */
    public void addReason(String text) {
        faultReasonList.add(new FaultReason(text, ""));
    }

    /**
     * Add a reason for the fault
     *
     * @param text     text message
     * @param language language
     */
    public void addReason(String text, String language) {
        faultReasonList.add(new FaultReason(text, language));
    }

    /**
     * Returns the first fault reason, if available. If not found, returns null.
     *
     * @return faultReason
     */
    public String getReason() {
        if (faultReasonList.size() >= 1) {
            return ((FaultReason) faultReasonList.get(0)).getText();
        }

        return null;
    }

    /**
     * Iterate over all of the headers
     *
     * @return iterator
     */
    public ListIterator headerIterator() {
        return headers.listIterator();
    }

    /**
     * Get at the headers. Useful for java1.5 iteration.
     *
     * @return the headers for this fault
     */
    public List headers() {
        return headers;
    }

    /**
     * Make an AxisFault based on a passed Exception.  If the Exception is
     * already an AxisFault, simply use that.  Otherwise, wrap it in an
     * AxisFault.  If the Exception is an InvocationTargetException (which
     * already wraps another Exception), get the wrapped Exception out from
     * there and use that instead of the passed one.
     *
     * @param e the <code>Exception</code> to build a fault for
     * @return an <code>AxisFault</code> representing <code>e</code>
     */
    public static AxisFault makeFault(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException) e).getTargetException();

            if (t instanceof Exception) {
                e = (Exception) t;
            }
        }

        if (e instanceof AxisFault) {
            return (AxisFault) e;
        }

        return new AxisFault(e);
    }

    /**
     * Get the current fault detail
     *
     * @return om element
     */
    public OMElement getDetail() {
        return detail;
    }

    public QName getFaultCode() {
        return faultCode;
    }

    /**
     * @return SOAPFaultCode if, user has set a {@link SOAPFaultCode} element when constructing the
     *         {@link #AxisFault(org.apache.axiom.soap.SOAPFaultCode, org.apache.axiom.soap.SOAPFaultReason, org.apache.axiom.soap.SOAPFaultNode, org.apache.axiom.soap.SOAPFaultRole, org.apache.axiom.soap.SOAPFaultDetail) AxisFault}
     */
    public SOAPFaultCode getFaultCodeElement() {
        return (SOAPFaultCode) (faultElements != null ? faultElements.get(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME) : null);
    }

    /**
     * @return SOAPFaultCode if, user has set a {@link SOAPFaultReason} element when constructing the
     *         {@link #AxisFault(org.apache.axiom.soap.SOAPFaultCode, org.apache.axiom.soap.SOAPFaultReason, org.apache.axiom.soap.SOAPFaultNode, org.apache.axiom.soap.SOAPFaultRole, org.apache.axiom.soap.SOAPFaultDetail) AxisFault}
     */
    public SOAPFaultReason getFaultReasonElement() {
        return (SOAPFaultReason) (faultElements != null ? faultElements.get(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME) : null);
    }

    /**
     * @return SOAPFaultCode if, user has set a {@link SOAPFaultNode} element when constructing the
     *         {@link #AxisFault(org.apache.axiom.soap.SOAPFaultCode, org.apache.axiom.soap.SOAPFaultReason, org.apache.axiom.soap.SOAPFaultNode, org.apache.axiom.soap.SOAPFaultRole, org.apache.axiom.soap.SOAPFaultDetail) AxisFault}
     */
    public SOAPFaultNode getFaultNodeElement() {
        return (SOAPFaultNode) (faultElements != null ? faultElements.get(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME) : null);
    }

    /**
     * @return SOAPFaultCode if, user has set a {@link SOAPFaultRole} element when constructing the
     *         {@link #AxisFault(org.apache.axiom.soap.SOAPFaultCode, org.apache.axiom.soap.SOAPFaultReason, org.apache.axiom.soap.SOAPFaultNode, org.apache.axiom.soap.SOAPFaultRole, org.apache.axiom.soap.SOAPFaultDetail) AxisFault}
     */
    public SOAPFaultRole getFaultRoleElement() {
        return (SOAPFaultRole) (faultElements != null ? faultElements.get(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME) : null);
    }

    /**
     * @return SOAPFaultCode if, user has set a {@link SOAPFaultDetail} element when constructing the
     *         {@link #AxisFault(org.apache.axiom.soap.SOAPFaultCode, org.apache.axiom.soap.SOAPFaultReason, org.apache.axiom.soap.SOAPFaultNode, org.apache.axiom.soap.SOAPFaultRole, org.apache.axiom.soap.SOAPFaultDetail) AxisFault}
     */
    public SOAPFaultDetail getFaultDetailElement() {
        return (SOAPFaultDetail) (faultElements != null ? faultElements.get(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME) : null);
    }

    /**
     * Get the faulting node uri.
     * SOAP1.2
     *
     * @return URI as a string or null
     */
    public String getNodeURI() {
        return nodeURI;
    }

    /**
     * Set the entire detail element of the fault
     *
     * @param detail
     */
    public void setDetail(OMElement detail) {
        this.detail = detail;
    }

    public void setFaultCode(QName soapFaultCode) {
        this.faultCode = soapFaultCode;
    }

    public void setFaultCode(String soapFaultCode) {
        // TODO: is it really safe to assume that the passed string is always the localpart?
        // What if someone passes soapenv:Sender?
        faultCode = new QName(soapFaultCode);
    }

    /**
     * Set the faulting node uri. SOAP1.2
     */
    public void setNodeURI(String nodeURI) {
        this.nodeURI = nodeURI;
    }


    public Map getFaultElements() {
        return faultElements;
    }

    public String getFaultNode() {
        return faultNode;
    }

    public String getFaultRole() {
        return faultRole;
    }

    public String getMessage() {
        return message != null ? message : super.getMessage();
    }

    public Throwable getCause() {
        return cause != null ? cause : super.getCause();
    }

    /**
     * Returns the MessageContext representation of the fault if the fault
     * was created by providing that.  
     * 
     * @return The MessageContext representing the fault message or null if the
     * fault was not created with MessageContext representation.
     */
    public MessageContext getFaultMessageContext() {
      return faultMessageContext;
    }
    
    class FaultReason {

        /**
         * Language of the reason.
         * xml:lang="en" "en-GB" or just ""
         */
        private String language = "";

        /**
         * env:reasontext
         */
        private String text;

        public FaultReason() {
        }

        public FaultReason(String text, String language) {
            this.text = text;
            this.language = language;
        }

        /**
         * Returns a string representation of the object.
         *
         * @return the text value
         */
        public String toString() {
            return text;
        }

        public String getLanguage() {
            return language;
        }

        public String getText() {
            return text;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
