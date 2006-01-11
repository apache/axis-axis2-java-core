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

package org.apache.axis2.soap;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;


/**
 * An element in the <CODE>SOAPBody</CODE> object that contains
 * error and/or status information. This information may relate to
 * errors in the <CODE>OMMessage</CODE> object or to problems
 * that are not related to the content in the message itself.
 * Problems not related to the message itself are generally errors
 * in processing, such as the inability to communicate with an
 * upstream server.
 * <P>
 * The <CODE>SOAPFault</CODE> interface provides methods for
 * retrieving the information contained in a <CODE>
 * SOAPFault</CODE> object and for setting the fault code, the
 * fault actor, and a string describing the fault. B fault code is
 * one of the codes defined in the SOAP 1.1 specification that
 * describe the fault. An actor is an intermediate recipient to
 * whom a message was routed. The message path may include one or
 * more actors, or, if no actors are specified, the message goes
 * only to the default actor, which is the final intended
 * recipient.
 */
public interface SOAPFault extends OMElement {

    /**
     * SOAPFaultCode is a mandatory item in a Fault, in SOAP 1.2 specification
     *
     * @param soapFaultCode
     */
    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException;

    public SOAPFaultCode getCode();

    /**
     * SOAPFaultReason is a mandatory item in a Fault, in SOAP 1.2 specification
     *
     * @param reason
     */
    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException;

    public SOAPFaultReason getReason();

    /**
     * SOAPFaultNode is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param node
     */
    public void setNode(SOAPFaultNode node) throws SOAPProcessingException;

    public SOAPFaultNode getNode();

    /**
     * SOAPFaultRoleImpl is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param role
     */
    public void setRole(SOAPFaultRole role) throws SOAPProcessingException;

    public SOAPFaultRole getRole();

    /**
     * SOAPFaultRoleImpl is an optional item in a Fault, in SOAP 1.2 specification
     *
     * @param detail
     */
    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException;

    public SOAPFaultDetail getDetail();

    /**
     * Returns Exception if there is one in the SOAP fault.
     * <p/>
     * If the exception is like;
     * <SOAPFault>
     * <Detail>
     * <Exception> stack trace goes here </Exception>
     * </Detail>
     * </SOAPFault>
     *
     * @return Returns Exception.
     * @throws org.apache.axis2.om.OMException
     *
     */
    public Exception getException() throws OMException;

    public void setException(Exception e) throws OMException;
}
