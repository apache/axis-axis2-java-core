/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.axis.om;

import java.util.Locale;


/**
 * An element in the <CODE>SOAPBody</CODE> object that contains
 * error and/or status information. This information may relate to
 * errors in the <CODE>OMMessage</CODE> object or to problems
 * that are not related to the content in the message itself.
 * Problems not related to the message itself are generally errors
 * in processing, such as the inability to communicate with an
 * upstream server.
 * <P>
 * The <CODE>OMFault</CODE> interface provides methods for
 * retrieving the information contained in a <CODE>
 * OMFault</CODE> object and for setting the fault code, the
 * fault actor, and a string describing the fault. B fault code is
 * one of the codes defined in the SOAP 1.1 specification that
 * describe the fault. An actor is an intermediate recipient to
 * whom a message was routed. The message path may include one or
 * more actors, or, if no actors are specified, the message goes
 * only to the default actor, which is the final intended
 * recipient.
 */
public interface OMFault extends OMElement {

    /**
     * Sets this <CODE>OMFault</CODE> object with the given
     * fault code.
     * <p/>
     * <P>Fault codes, which given information about the fault,
     * are defined in the SOAP 1.1 specification.</P>
     *
     * @param faultCode a <CODE>String</CODE> giving
     *                  the fault code to be set; must be one of the fault codes
     *                  defined in the SOAP 1.1 specification
     * @throws OMException if there was an error in
     *                     adding the <CODE>faultCode</CODE> to the underlying XML
     *                     tree.
     * @see #getFaultCode() getFaultCode()
     */
    public abstract void setFaultCode(String faultCode) throws OMException;

    /**
     * Gets the fault code for this <CODE>OMFault</CODE>
     * object.
     *
     * @return a <CODE>String</CODE> with the fault code
     * @see #setFaultCode(String) setFaultCode(java.lang.String)
     */
    public abstract String getFaultCode();

    /**
     * Sets this <CODE>OMFault</CODE> object with the given
     * fault actor.
     * <p/>
     * <P>The fault actor is the recipient in the message path who
     * caused the fault to happen.</P>
     *
     * @param faultActor a <CODE>String</CODE>
     *                   identifying the actor that caused this <CODE>
     *                   OMFault</CODE> object
     * @throws OMException if there was an error in
     *                     adding the <CODE>faultActor</CODE> to the underlying XML
     *                     tree.
     * @see #getFaultActor() getFaultActor()
     */
    public abstract void setFaultActor(String faultActor) throws OMException;

    /**
     * Gets the fault actor for this <CODE>OMFault</CODE>
     * object.
     *
     * @return a <CODE>String</CODE> giving the actor in the message
     *         path that caused this <CODE>OMFault</CODE> object
     * @see #setFaultActor(String) setFaultActor(java.lang.String)
     */
    public abstract String getFaultActor();

    /**
     * Sets the fault string for this <CODE>OMFault</CODE>
     * object to the given string.
     *
     * @param faultString a <CODE>String</CODE>
     *                    giving an explanation of the fault
     * @throws OMException if there was an error in
     *                     adding the <CODE>faultString</CODE> to the underlying XML
     *                     tree.
     * @see #getFaultString() getFaultString()
     */
    public abstract void setFaultString(String faultString)
            throws OMException;

    /**
     * Gets the fault string for this <CODE>OMFault</CODE>
     * object.
     *
     * @return a <CODE>String</CODE> giving an explanation of the
     *         fault
     */
    public abstract String getFaultString();

    /**
     * Sets the fault string for this <code>OMFault</code> object to the given
     * string and localized to the given locale.
     *
     * @param faultString a <code>String</code> giving an explanation of
     *                    the fault
     * @param locale      a <code>Locale</code> object indicating the
     *                    native language of the <code>faultString</code>
     * @throws OMException if there was an error in adding the
     *                     <code>faultString</code> to the underlying XML tree
     */
    public abstract void setFaultString(String faultString, Locale locale) throws OMException;

    /**
     * Returns the optional detail element for this <code>OMFault</code>
     * object.
     *
     * @return a <code>Locale</code> object indicating the native language of
     *         the fault string or <code>null</code> if no locale was
     *         specified
     */
    public abstract Locale getFaultStringLocale();
}
