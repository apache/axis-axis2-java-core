package org.apache.axis.om;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * This will help to create OM API object. This will ease the switching from one OM impl to another.
 */
public abstract class OMFactory {

    //    /**
//     * @param parent
//     * @param object
//     * This is used to construct the elements from an Object
//     * @return the parent itself
//     */
//   public abstract OMElement createOMElement(OMElement parent,Object object);

    /**
     * @param parent
     * @return
     */
    public abstract OMElement createOMElement(OMElement parent);

    /**
     * @param localName
     * @param ns
     * @return
     */
    public abstract OMElement createOMElement(String localName, OMNamespace ns);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract OMElement createOMElement(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);


    /**
     * @param localName
     * @param ns
     * @param parent
     * @return
     */
    public abstract OMNamedNode createOMNamedNode(String localName, OMNamespace ns, OMElement parent);

    /**
     * @param parent
     * @return
     */
    public abstract OMNamedNode createOMNamedNode(OMElement parent);

    public abstract OMNamespace createOMNamespace(String uri, String prefix);


    public abstract OMNode createOMNode(OMElement parent);

    /**
     * @param parent
     * @param text
     * @return
     */
    public abstract OMText createText(OMElement parent, String text);

    /**
     * @param s
     * @return
     */
    public abstract OMText createText(String s);

    /**
     * @param envelope
     * @return
     */
    public abstract SOAPBody createSOAPBody(SOAPEnvelope envelope);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract SOAPBody createSOAPBody(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);


    /**
     * @param ns
     * @param builder
     * @return
     */
    public abstract SOAPEnvelope createSOAPEnvelope(OMNamespace ns, OMXMLParserWrapper builder);

    /**
     * @param ns
     */
    public abstract SOAPEnvelope createSOAPEnvelope(OMNamespace ns);

    /**
     * @param envelope
     */
    public abstract SOAPHeader createSOAPHeader(SOAPEnvelope envelope);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract SOAPHeader createSOAPHeader(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);


    /**
     * @param localName
     * @param ns
     */
    public abstract SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);

    /**
     * @param parent
     * @return
     */
    public abstract SOAPFault createSOAPFault(SOAPBody parent, Exception e);


    /**
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract SOAPFault createSOAPFault(OMNamespace ns, SOAPBody parent, OMXMLParserWrapper builder);


    //make the constructor protected
    protected OMFactory() {
    }

    public static OMFactory newInstance() {
        return FactoryFinder.findFactory(null);
    }

    public abstract SOAPEnvelope getDefaultEnvelope();

}