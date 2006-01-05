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

import org.apache.axis2.fault.FaultCode;
import org.apache.axis2.fault.FaultReasonList;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPHeader;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
 *           SOAP1.2              SOAP1.1
 *           node                 faultactor
 *           reason(0).text       faultstring
 *           faultcode.value      faultcode
 *           faultcode.subcode    (discarded)
 *           detail               detail
 *           role                 (discarded)
 *           </pre>
 */
public class AxisFault extends RemoteException {

	private static final long serialVersionUID = -374933082062124907L;

	/**
     * Contains the faultcode
     */
    private FaultCode faultCode = new FaultCode();

    /**
     * our failt reasons
     */
    private FaultReasonList reasons = new FaultReasonList();

    /**
     * assume headers are not used very often
     */
    private List headers = new ArrayList(0);
    private OMElement detail;

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
     * An incoming SOAPFault
     */
    private SOAPFault soapFault;

    /**
     * Make an AxisFault from an incoming SOAPFault
     *
     * @param fault that caused the failure
     */
    public AxisFault(SOAPFault fault) {
        soapFault = fault;
        init(soapFault);
    }

    /**
     * @param message
     */
    public AxisFault(String message) {
        super(message);
        addReason(message);
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
     * Add a header to the list of fault headers
     *
     * @param header to add.
     */
    public void addHeader(SOAPHeader header) {
        headers.add(header);
    }

    /**
     * Add a reason for the fault in the empty "" language
     *
     * @param text text message
     */
    public void addReason(String text) {
        addReason(text, "");
    }

    /**
     * Add a reason for the fault
     *
     * @param text     text message
     * @param language language
     */
    public void addReason(String text, String language) {
        reasons.add(text, language);
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
     * Initialise from a SOAPFault. This is how incoming fault messages
     * get turned into AxisFaults.
     *
     * @param fault incoming fault
     */
    private void init(SOAPFault fault) {
        SOAPFaultCode faultcodesource = fault.getCode();

        faultCode = new FaultCode(faultcodesource);
        detail = fault.getDetail();
        fault.getNode();
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

    public String getFaultCode() {
        return faultCode.getValueString();
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
        faultCode.setValue(soapFaultCode);
    }

    public void setFaultCode(String soapFaultCode) {
        faultCode.setValueString(soapFaultCode);
    }

    /**
     * Set the faulting node uri. SOAP1.2
     */
    public void setNodeURI(String nodeURI) {
        this.nodeURI = nodeURI;
    }
}
