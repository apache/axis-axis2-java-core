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
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 11, 2004
 * Time: 10:53:06 AM
 * <p/>
 * This will help to create OM API object. This will ease the switching from one OM impl to another.
 */
public abstract class OMFactory {

    // -----------------------------   OM Attribute -----------------------------------------------------------------
    /**
     * @param localName
     * @param ns
     * @param value
     * @param parent
     * @return
     */
    public abstract OMAttribute createOMAttribute(String localName, OMNamespace ns, String value, OMElement parent);

    /**
     * @param localName
     * @param ns
     * @param value
     * @return
     */
    public abstract OMAttribute createOMAttribute(String localName, OMNamespace ns, String value);

    // --------------------------------------------------------------------------------------------------------------

    // -----------------------------   OM Element -----------------------------------------------------------------
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

    // --------------------------------------------------------------------------------------------------------------

    // -----------------------------   OM NamedNode -----------------------------------------------------------------

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

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM Namespace -----------------------------------------------------------------
    public abstract OMNamespace createOMNamespace(String uri, String prefix);

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM Node -----------------------------------------------------------------

    public abstract OMNode createOMNode(OMElement parent);

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM TextNode -----------------------------------------------------------------
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

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM OMBody -----------------------------------------------------------------
    /**
     * @param envelope
     * @return
     */
    public abstract OMBody createOMBody(OMEnvelope envelope);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract OMBody createOMBody(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM OMEnvelope -----------------------------------------------------------------

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract OMEnvelope createOMEnvelope(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);

    /**
     * @param localName
     * @param ns
     */
    public abstract OMEnvelope createOMEnvelope(String localName, OMNamespace ns);

    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM OMHeader -----------------------------------------------------------------
    /**
     * @param envelope
     */
    public abstract OMHeader createHeader(OMEnvelope envelope);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract OMHeader createHeader(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);


    // --------------------------------------------------------------------------------------------------------------
    // -----------------------------   OM OMHeaderBlock -----------------------------------------------------------------
    /**
     * @param localName
     * @param ns
     */
    public abstract OMHeaderBlock createOMHeaderBlock(String localName, OMNamespace ns);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public abstract OMHeaderBlock createOMHeaderBlock(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder);


    //make the constructor protected
    protected OMFactory() {
    }

    public static OMFactory newInstance(){
        return FactoryFinder.findFactory(null);
    }

}