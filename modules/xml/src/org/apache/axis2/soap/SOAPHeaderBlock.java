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
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

/**
 * <P>An object representing the contents in the SOAP header part of the SOAP
 * envelope. The immediate children of a <CODE> SOAPHeader</CODE> object can be
 * represented only as <CODE> SOAPHeaderBlock</CODE> objects.</P> <P>B
 * <CODE>SOAPHeaderBlock</CODE> object can have other <CODE>OMElement</CODE>
 * objects as its children.</P>
 */
public interface SOAPHeaderBlock extends OMElement {
    /**
     * Sets the actor associated with this <CODE> SOAPHeaderBlock</CODE> object
     * to the specified actor.
     *
     * @param roleURI a <CODE>String</CODE> giving the URI of the actor to set
     * @throws java.lang.IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getRole() getRole()
     */
    public abstract void setRole(String roleURI);

    /**
     * Returns the uri of the actor associated with this <CODE>
     * SOAPHeaderBlock</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the actor
     * @see #setRole(java.lang.String) setRole(java.lang.String)
     */
    public abstract String getRole();

    /**
     * Sets the mustUnderstand attribute for this <CODE> SOAPHeaderBlock</CODE>
     * object to be on or off. <P>If the mustUnderstand attribute is on, the
     * actor who receives the <CODE>SOAPHeaderBlock</CODE> must process it
     * correctly. This ensures, for example, that if the <CODE>
     * SOAPHeaderBlock</CODE> object modifies the message, that the message is
     * being modified correctly.</P>
     *
     * @param mustUnderstand <CODE>true</CODE> to set the mustUnderstand
     *                       attribute on; <CODE>false</CODE> to turn if off
     * @throws java.lang.IllegalArgumentException
     *          if there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    public abstract void setMustUnderstand(boolean mustUnderstand);

    public abstract void setMustUnderstand(String mustUnderstand) throws SOAPProcessingException;

    /**
     * Returns whether the mustUnderstand attribute for this
     * <CODE>SOAPHeaderBlock</CODE> object is turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of this
     *         <CODE>SOAPHeaderBlock</CODE> object is turned on;
     *         <CODE>false</CODE> otherwise
     */
    public abstract boolean getMustUnderstand() throws SOAPProcessingException;


    public abstract boolean isProcessed();

    /**
     * We need to know whether all the mustUnderstand headers have been
     * processed by the node. This will done by a specific validation handler at
     * the end of the execution chain. For this all the handlers who process a
     * particular header block must explicitly say that he processesd the header
     * by calling setProcessed()
     */
    public abstract void setProcessed();

}
