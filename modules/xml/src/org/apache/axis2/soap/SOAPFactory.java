package org.apache.axis2.soap;

import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public interface SOAPFactory extends OMFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    /**
     * @param builder
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMXMLParserWrapper builder);

    /**
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope() throws SOAPProcessingException;

    /**
     * @param envelope
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) throws SOAPProcessingException;

    /**
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope, OMXMLParserWrapper builder);

    /**
     * @param localName
     * @param ns
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns, SOAPHeader parent) throws SOAPProcessingException;

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns, SOAPHeader parent, OMXMLParserWrapper builder) throws SOAPProcessingException;

    /**
     * @param parent
     * @param e
     * @return
     */
    public SOAPFault createSOAPFault(SOAPBody parent, Exception e) throws SOAPProcessingException;

    public SOAPFault createSOAPFault(SOAPBody parent) throws SOAPProcessingException;

    /**
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFault createSOAPFault(SOAPBody parent, OMXMLParserWrapper builder);

    /**
     * @param envelope
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope) throws SOAPProcessingException;

    /**
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope, OMXMLParserWrapper builder);

    /* ========================
       =  SOAPFaultCode       =
       ======================== */

    /**
     * Code eii under SOAPFault (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent) throws SOAPProcessingException;

    /**
     * Code eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent, OMXMLParserWrapper builder);


    /*========================
      =  SOAPFaultCodeValue  =
      ======================== */
    /**
     * Value eii under Code (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent) throws SOAPProcessingException;

    /**
     * Value eii under Code (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent, OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultSubCode    =
      ======================== */

    /**
     * SubCode eii under Value (parent)
     *
     * @param parent
     * @return
     */

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent) throws SOAPProcessingException;

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent, OMXMLParserWrapper builder);

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent) throws SOAPProcessingException;

    /**
     * SubCode eii under Value (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent, OMXMLParserWrapper builder);

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent) throws SOAPProcessingException;

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent, OMXMLParserWrapper builder);


    /*========================
      =  SOAPFaultReason     =
      ======================== */

    /**
     * Reason eii under SOAPFault (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent) throws SOAPProcessingException;

    /**
     * Reason eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent, OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultReasonText     =
      ======================== */

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent) throws SOAPProcessingException;

    /**
     * SubCode eii under SubCode (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent, OMXMLParserWrapper builder);


    /*========================
      =  SOAPFaultNode       =
      ======================== */

    /**
     * Node eii under SOAPFault (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent) throws SOAPProcessingException;

    /**
     * Node eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent, OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultRole       =
      ======================== */

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent) throws SOAPProcessingException;

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent, OMXMLParserWrapper builder);

    /*========================
      =  SOAPFaultDetail     =
      ======================== */

    /**
     * Detail eii under SOAPFault (parent)
     *
     * @param parent
     * @return
     */
    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent) throws SOAPProcessingException;

    /**
     * Role eii under SOAPFault (parent)
     *
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent, OMXMLParserWrapper builder);


    /**
     * Method getDefaultEnvelope
     *
     * @return
     */
    public SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException;

    public SOAPEnvelope getDefaultFaultEnvelope() throws SOAPProcessingException;
}
