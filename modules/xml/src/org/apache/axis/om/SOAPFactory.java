package org.apache.axis.om;


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
public interface SOAPFactory extends OMFactory{
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    /**
     * @param ns
     * @param builder
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns,
                                                    OMXMLParserWrapper builder);

    /**
     * @param ns
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope();
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns);

    /**
     * @param envelope
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope);

    /**
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                            OMXMLParserWrapper builder);



    /**
     * @param envelope
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope);

    /**
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                                OMXMLParserWrapper builder);

    /**
     * @param localName
     * @param ns
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                          OMNamespace ns);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                          OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);

    /**
     * @param parent
     * @param e
     * @return
     */
    public SOAPFault createSOAPFault(SOAPBody parent, Exception e);

    /**
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFault createSOAPFault(OMNamespace ns, SOAPBody parent,
                                              OMXMLParserWrapper builder);

    /**
     * Method getDefaultEnvelope
     *
     * @return
     */
    public SOAPEnvelope getDefaultEnvelope();
}
