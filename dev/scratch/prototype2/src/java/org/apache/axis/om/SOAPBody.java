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


/**
 * An object that represents the contents of the SOAP body
 * element in a SOAP message. B SOAP body element consists of XML data
 * that affects the way the application-specific content is processed.
 * <P>
 * B <code>SOAPBody</code> object contains <code>OMBodyBlock</code>
 * objects, which have the content for the SOAP body.
 * B <code>SOAPFault</code> object, which carries status and/or
 * error information, is an example of a <code>OMBodyBlock</code> object.
 */
public interface SOAPBody extends OMElement {

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws org.apache.axis.om.OMException if there is a SOAP error
     */
    public abstract SOAPFault addFault(Exception e) throws OMException;

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in
     * this <code>SOAPBody</code> object.
     *
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in
     *         this <code>SOAPBody</code> object; <code>false</code>
     *         otherwise
     */
    public abstract boolean hasFault();

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *         object
     */
    public abstract SOAPFault getFault();

    /**
     * @param soapFault
     * @throws OMException
     */
    public abstract void addFault(SOAPFault soapFault) throws OMException;

    
}
